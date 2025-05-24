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
import com.opsbeach.connect.ticket.dto.TicketJiraDto;
import com.opsbeach.connect.ticket.entity.TicketJira;
import com.opsbeach.connect.ticket.repository.TicketJiraRepository;

public class TicketJiraServiceTest {
    
    @InjectMocks
    private TicketJiraService ticketJiraService;

    @Mock
    private TicketJiraRepository ticketJiraRepository;

    @Spy
    private IdSpecifications<TicketJira> tSpecifications;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private TicketJiraDto getDto() {
        return TicketJiraDto.builder().id(1L).build();
    }

    @Test
    public void addAllTest() {
        var dto = getDto();
        var ticketJiraDtos = List.of(dto);
        var ticketJiras = List.of(dto.toDomin(dto));
        when(ticketJiraRepository.saveAll(ArgumentMatchers.<List<TicketJira>>any())).thenReturn(ticketJiras);
        var response = ticketJiraService.addAll(ticketJiraDtos);
        assertEquals(dto.getId(), response.get(0).getId());
        assertEquals(null, ticketJiraService.addAll(null));
    }

    @Test
    public void getByKeyTest() {
        var ticketJiraDto = getDto();
        when(ticketJiraRepository.findOne(ArgumentMatchers.<Specification<TicketJira>>any())).thenReturn(Optional.of(ticketJiraDto.toDomin(ticketJiraDto)));
        assertEquals(ticketJiraDto.getId(), ticketJiraService.getByKey("1", 1L).getId());
        when(ticketJiraRepository.findOne(ArgumentMatchers.<Specification<TicketJira>>any())).thenReturn(Optional.empty());
        assertNull(ticketJiraService.getByKey("1", 1L));
    }
}
