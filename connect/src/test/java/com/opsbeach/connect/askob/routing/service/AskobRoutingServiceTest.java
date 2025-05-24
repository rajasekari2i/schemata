package com.opsbeach.connect.askob.routing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
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

import com.opsbeach.connect.askob.routing.dto.AskobRoutingDto;
import com.opsbeach.connect.askob.routing.entity.AskobRouting;
import com.opsbeach.connect.askob.routing.repository.AskobRoutingRepository;
import com.opsbeach.connect.askob.workspace.service.AskobWorkspaceService;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

public class AskobRoutingServiceTest {

    @InjectMocks
    private AskobRoutingService askobRoutingService;

    @Mock
    private AskobWorkspaceService askobWorkspaceService;

    @Mock
    private AskobRoutingRepository askobRoutingRepository;

    @Spy
    private IdSpecifications<AskobRouting> routingSpecifications;

    @Mock
    private ResponseMessage responseMessage;
 
    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private AskobRoutingDto getDto() {
        return AskobRoutingDto.builder().id(1L).channelOrigin("PM").channelTo("DB").originWorkspaceId(1L).toWorkspaceId(3L).build();
    }

    @Test
    public void getAllTest() {
        var askobRoutingDto = getDto();
        List<AskobRouting> askobRoutings = List.of(askobRoutingDto.toDomin(askobRoutingDto));
            when(askobRoutingRepository.findAll(ArgumentMatchers.<Specification<AskobRouting>>any())).thenReturn(askobRoutings);
        var response = askobRoutingService.getAll(null);
        assertEquals(1, response.size());
        assertEquals(askobRoutings.get(0).getChannelOrigin(), response.get(0).getChannelOrigin());
            when(askobRoutingRepository.findAll(ArgumentMatchers.<Specification<AskobRouting>>any())).thenReturn(List.of());
        assertEquals(0, askobRoutingService.getAll("OP2").size());
    }

    @Test
    public void updateTest() {
        var askobRoutingDto = getDto();
        var askobRouting = askobRoutingDto.toDomin(askobRoutingDto);
            when(askobRoutingRepository.findById(anyLong())).thenReturn(Optional.of(askobRouting));
            when(askobRoutingRepository.save(ArgumentMatchers.<AskobRouting>any())).thenReturn(askobRouting);
        var response = askobRoutingService.update(askobRoutingDto);
        assertEquals(askobRoutingDto.getChannelOrigin(), response.getChannelOrigin());
    }

    @Test
    public void deleteTest() {
        var askobRoutingDto = getDto();
        var askobRouting =  askobRoutingDto.toDomin(askobRoutingDto);
            when(askobRoutingRepository.findById(1L)).thenReturn(Optional.of(askobRouting));
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.DELETED, "Askob Routing"), askobRoutingService.delete(1L));
        assertThrows(RecordNotFoundException.class, () -> { askobRoutingService.delete(2L); });
    }
}
