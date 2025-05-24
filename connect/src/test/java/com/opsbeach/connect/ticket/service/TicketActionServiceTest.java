package com.opsbeach.connect.ticket.service;

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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.ticket.entity.TicketAction;
import com.opsbeach.connect.ticket.enums.TicketType;
import com.opsbeach.connect.ticket.repository.TicketActionRepository;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.SecurityUtil;

public class TicketActionServiceTest {

    @InjectMocks
    private TicketActionService ticketActionService;

    @Mock
    private TicketActionRepository ticketActionRepository;

    @Mock
    private ResponseMessage responseMessage;

    @Spy
    private IdSpecifications<TicketAction> taskSpecifications;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }   

    private TicketAction getTicketAction() {
        return TicketAction.builder().clientId(SecurityUtil.getClientId()).ticketId(1L).type(TicketType.JIRA).referenceId("OP-404").build();
    }

    @Test
    public void addTest() {
        var ticketAction = getTicketAction();
        when(ticketActionRepository.save(ArgumentMatchers.<TicketAction>any())).thenReturn(ticketAction);
        var response = ticketActionService.add(TicketType.JIRA.name(), 1L, "OP-404");
        assertEquals(ticketAction.getType(), response.getType());
    }

    @Test
    public void getTest() {
        var ticketAction = getTicketAction();
        when(ticketActionRepository.findById(1L)).thenReturn(Optional.of(ticketAction));
        var response = ticketActionService.get(1L);
        assertEquals(ticketAction.getType(), response.getType());

        assertThrows(RecordNotFoundException.class, () -> { ticketActionService.get(2L); });
    }

    @Test
    public void getAllTest() {
        var ticketAction = getTicketAction();
        when(ticketActionRepository.findAll()).thenReturn(List.of(ticketAction));
        var response = ticketActionService.getAll();
        assertEquals(ticketAction.getTicketId(), response.get(0).getTicketId());
        assertEquals(1, response.size());
        when(ticketActionRepository.findAll()).thenReturn(List.of());
        assertEquals(0, ticketActionService.getAll().size());
    }
}
