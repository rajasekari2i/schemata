package com.opsbeach.connect.ticket.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.ticket.dto.TicketZendeskDto;
import com.opsbeach.connect.ticket.entity.TicketZendesk;
import com.opsbeach.connect.ticket.repository.TicketZendeskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketZendeskService {
    
    private final TicketZendeskRepository ticketZendeskRepository;

    private final IdSpecifications<TicketZendesk> ticketZendeskSpecifications;

    public List<TicketZendeskDto> addAll(List<TicketZendeskDto> ticketZendeskDtos) {
        if (!ObjectUtils.isEmpty(ticketZendeskDtos)) {
            var ticketZendesks = ticketZendeskDtos.stream().map(ticketZendeskDtos.get(0)::toDomin).toList();
            ticketZendeskRepository.saveAll(ticketZendesks);
            return ticketZendesks.stream().map(ticketZendesks.get(0)::toDto).toList();
        }
        return ticketZendeskDtos;
    }

    public TicketZendeskDto getByticketId(String ticketId, Long clientId) {
        var specification = ticketZendeskSpecifications.findByClientId(clientId).and(ticketZendeskSpecifications.findByTicketId(ticketId));
        var ticketZendesk = ticketZendeskRepository.findOne(specification).orElse(null);
        return ticketZendesk != null ? ticketZendesk.toDto(ticketZendesk) : null;
    }
}
