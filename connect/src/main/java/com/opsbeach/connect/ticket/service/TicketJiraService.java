package com.opsbeach.connect.ticket.service;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.ticket.dto.TicketJiraDto;
import com.opsbeach.connect.ticket.entity.TicketJira;
import com.opsbeach.connect.ticket.repository.TicketJiraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketJiraService {
    
    private final TicketJiraRepository ticketJiraRepository;

    private final IdSpecifications<TicketJira> ticketJiraSpecifications;

    public List<TicketJiraDto> addAll(List<TicketJiraDto> ticketJiraDtos) {
        if (!ObjectUtils.isEmpty(ticketJiraDtos)) {
            var ticketJiras = ticketJiraDtos.stream().map(ticketJiraDtos.get(0)::toDomin).toList();
            ticketJiraRepository.saveAll(ticketJiras);
            return ticketJiras.stream().map(ticketJiras.get(0)::toDto).toList();
        }
        return ticketJiraDtos;
    }

    public TicketJiraDto getByKey(String key, Long clientId) {
        var specification = ticketJiraSpecifications.findByClientId(clientId).and(ticketJiraSpecifications.findByKey(key));
        var ticketJira = ticketJiraRepository.findOne(specification).orElse(null);
        return ticketJira != null ? ticketJira.toDto(ticketJira) : null;
    }
}
