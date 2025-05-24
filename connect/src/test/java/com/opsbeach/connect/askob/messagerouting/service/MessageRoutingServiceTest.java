package com.opsbeach.connect.askob.messagerouting.service;

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

import com.opsbeach.connect.askob.message.service.AskobMessageService;
import com.opsbeach.connect.askob.messagerouting.dto.MessageRoutingDto;
import com.opsbeach.connect.askob.messagerouting.entity.MessageRouting;
import com.opsbeach.connect.askob.messagerouting.repository.MessageRoutingRepository;
import com.opsbeach.connect.askob.workspace.service.AskobWorkspaceService;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

public class MessageRoutingServiceTest {

    @InjectMocks
    private MessageRoutingService messageRoutingService;

    @Mock
    private MessageRoutingRepository messageRoutingRepository;

    @Mock
    private AskobMessageService askobMessageService;

    @Mock
    private AskobWorkspaceService askobWorkspaceService;

    @Mock
    private ResponseMessage responseMessage;

    @Spy
    private IdSpecifications<MessageRouting> routingSpecifications;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private MessageRoutingDto getMessageRoutingDto() {
        return MessageRoutingDto.builder().id(1L).fromChannel("1").toChannel("2").build();
    }

    @Test
    public void getAllTest() {
        var messageRoutingDto = getMessageRoutingDto();
        var messageRoutings = List.of(messageRoutingDto.toDomin(messageRoutingDto));
            when(messageRoutingRepository.findAll(ArgumentMatchers.<Specification<MessageRouting>>any())).thenReturn(messageRoutings);
        assertEquals(1, messageRoutingService.getAll(null).size());
            when(messageRoutingRepository.findAll(ArgumentMatchers.<Specification<MessageRouting>>any())).thenReturn(List.of());
        assertEquals(0, messageRoutingService.getAll(1L).size());
    }
    
    @Test
    public void updateTest() {
        var messageRoutingDto = getMessageRoutingDto();
        var messageRouting = messageRoutingDto.toDomin(messageRoutingDto);
        when(messageRoutingRepository.findById(1L)).thenReturn(Optional.of(messageRouting));
        when(messageRoutingRepository.save(ArgumentMatchers.<MessageRouting>any())).thenReturn(messageRouting);
        var response = messageRoutingService.update(messageRoutingDto);
        assertEquals(messageRoutingDto.getToChannel(), response.getToChannel());
    }

    @Test
    public void deleteTest() {
        var messageRoutingDto = getMessageRoutingDto();
        var messageRouting = messageRoutingDto.toDomin(messageRoutingDto);
        when(messageRoutingRepository.findById(1L)).thenReturn(Optional.of(messageRouting));
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.MESSAGE_ROUTING), messageRoutingService.delete(1L));

        assertThrows(RecordNotFoundException.class, () -> { messageRoutingService.delete(2L); });
    }
}
