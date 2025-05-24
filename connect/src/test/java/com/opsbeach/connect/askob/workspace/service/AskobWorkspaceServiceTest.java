package com.opsbeach.connect.askob.workspace.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.askob.workspace.dto.AskobWorkspaceDto;
import com.opsbeach.connect.askob.workspace.entity.AskobWorkspace;
import com.opsbeach.connect.askob.workspace.repository.AskobWorkspaceRepository;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

public class AskobWorkspaceServiceTest {

    @InjectMocks
    private AskobWorkspaceService askobWorkspaceService;

    @Mock
    private AskobWorkspaceRepository askobWorkspaceRepository;

    @Spy
    private IdSpecifications<AskobWorkspace> workspaceSpecifications;

    @Mock
    private ResponseMessage responseMessage;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private AskobWorkspaceDto getDto() {
        return AskobWorkspaceDto.builder().id(1L).key("1").name("test").build();    
    }

    @Test
    public void getAllTest() {
        var askobWorkspaceDto = getDto();
        List<AskobWorkspace> askobWorkspaces = List.of(askobWorkspaceDto.toDomin(askobWorkspaceDto));
            when(askobWorkspaceRepository.findAll(ArgumentMatchers.<Specification<AskobWorkspace>>any())).thenReturn(askobWorkspaces);
        var response = askobWorkspaceService.getAll(null);
        assertEquals(1, response.size());
        assertEquals(askobWorkspaces.get(0).getName(), response.get(0).getName());
            when(askobWorkspaceRepository.findAll(ArgumentMatchers.<Specification<AskobWorkspace>>any())).thenReturn(List.of());
        assertEquals(0, askobWorkspaceService.getAll("key").size());
    }

    @Test
    public void updateTest() {
        var askobWorkspaceDto = getDto();
        var askobWorkspace = askobWorkspaceDto.toDomin(askobWorkspaceDto);
            when(askobWorkspaceRepository.findById(1L)).thenReturn(Optional.of(askobWorkspace));
            when(askobWorkspaceRepository.save(ArgumentMatchers.<AskobWorkspace>any())).thenReturn(askobWorkspace);
        var response = askobWorkspaceService.update(askobWorkspaceDto);
        assertEquals(askobWorkspaceDto.getName(), response.getName());
    }

    @Test
    public void deleteTest() {
        var askobWorkspaceDto = getDto();
        var askobWorkspace = askobWorkspaceDto.toDomin(askobWorkspaceDto);
            when(askobWorkspaceRepository.findById(1L)).thenReturn(Optional.of(askobWorkspace));
            when(askobWorkspaceRepository.save(ArgumentMatchers.<AskobWorkspace>any())).thenReturn(askobWorkspace);
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.ASKOB_WORKSPACE), askobWorkspaceService.delete(1L));

        assertThrows(RecordNotFoundException.class, () -> { askobWorkspaceService.get(2L); });
    }
}
