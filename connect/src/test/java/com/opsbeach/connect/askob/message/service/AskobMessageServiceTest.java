package com.opsbeach.connect.askob.message.service;

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

import com.opsbeach.connect.askob.message.dto.AskobMessageDto;
import com.opsbeach.connect.askob.message.entity.AskobMessage;
import com.opsbeach.connect.askob.message.repository.AskobMessageRepository;
import com.opsbeach.connect.askob.routing.service.AskobRoutingService;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

public class AskobMessageServiceTest {

    @InjectMocks
    private AskobMessageService askobMessageService;

    @Mock
    private AskobMessageRepository askobMessageRepository;

    @Mock
    private AskobRoutingService askobRoutingService;

    @Spy
    private IdSpecifications<AskobMessage> messageSpecifications;

    @Mock
    private ResponseMessage responseMessage;
    
    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private AskobMessageDto getDto() {
        return AskobMessageDto.builder().id(1L).threadTs("1").messageTs("2").build();
    }

    @Test
    public void getAllTest() {
        var askobMessageDto = getDto();
        var askobMessages = List.of(askobMessageDto.toDomin(askobMessageDto));
            when(askobMessageRepository.findAll()).thenReturn(askobMessages);
        var response = askobMessageService.getAll();
        assertEquals(1, response.size());
        assertEquals(askobMessageDto.getThreadTs(), response.get(0).getThreadTs());
            when(askobMessageRepository.findAll()).thenReturn(null);
        assertEquals(0, askobMessageService.getAll().size());
    }

    @Test
    public void getByMessageTsTest() {
        assertThrows(RecordNotFoundException.class, () -> { askobMessageService.getByMessageTs("2"); });
        var askobMessageDto = getDto();
        var askobMessage = askobMessageDto.toDomin(askobMessageDto);
            when(askobMessageRepository.findOne(ArgumentMatchers.<Specification<AskobMessage>>any())).thenReturn(Optional.of(askobMessage));
        var response = askobMessageService.getByMessageTs("2");
        assertEquals(askobMessage.getMessageTs(), response.getMessageTs());
    } 

    @Test
    public void updateTest() {
        var askobMessageDto = getDto();
        var askobRouting = askobMessageDto.toDomin(askobMessageDto);
            when(askobMessageRepository.findById(anyLong())).thenReturn(Optional.of(askobRouting));
            when(askobMessageRepository.save(ArgumentMatchers.<AskobMessage>any())).thenReturn(askobRouting);
        var response = askobMessageService.update(askobMessageDto);
        assertEquals(askobMessageDto.getMessageTs(), response.getMessageTs());
    }

    @Test
    public void deleteTest() {
        var askobMessageDto = getDto();
        var askobRouting = askobMessageDto.toDomin(askobMessageDto);
            when(askobMessageRepository.findById(1L)).thenReturn(Optional.of(askobRouting));
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.ASKOB_MESSAGE), askobMessageService.delete(1L));

        assertThrows(RecordNotFoundException.class, () -> { askobMessageService.delete(2L); });
    }
}
