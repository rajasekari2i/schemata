package com.opsbeach.connect.ticket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.ticket.dto.TicketZendeskDto;
import com.opsbeach.connect.ticket.entity.TicketZendesk;
import com.opsbeach.connect.ticket.repository.TicketZendeskRepository;

public class TicketZendeskServiceTest {

    @InjectMocks
    private TicketZendeskService ticketZendeskService;

    @Mock
    private TicketZendeskRepository ticketZendeskRepository;

    @Spy
    private IdSpecifications<TicketZendesk> tIdSpecifications;
    
    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private TicketZendeskDto getDto() {
        return TicketZendeskDto.builder().id(1L).build();
    }

    @Test
    public void addAllTest() {
        var dto = getDto();
        var ticketJiraDtos = List.of(dto);
        var ticketJiras = List.of(dto.toDomin(dto));
        when(ticketZendeskRepository.saveAll(ArgumentMatchers.<List<TicketZendesk>>any())).thenReturn(ticketJiras);
        var response = ticketZendeskService.addAll(ticketJiraDtos);
        assertEquals(dto.getId(), response.get(0).getId());
        assertEquals(null, ticketZendeskService.addAll(null));
        assertEquals(List.of(), ticketZendeskService.addAll(List.of()));
    }

    @Test
    public void getByticketIdTest() {
        var ticketJiraDto = getDto();
        when(ticketZendeskRepository.findOne(ArgumentMatchers.<Specification<TicketZendesk>>any())).thenReturn(Optional.of(ticketJiraDto.toDomin(ticketJiraDto)));
        assertEquals(ticketJiraDto.getId(), ticketZendeskService.getByticketId("1", 1L).getId());
        when(ticketZendeskRepository.findOne(ArgumentMatchers.<Specification<TicketZendesk>>any())).thenReturn(Optional.empty());
        assertNull(ticketZendeskService.getByticketId("1", 1L));
    }
}
