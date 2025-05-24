package com.opsbeach.connect.ticket.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.ticket.dto.TicketActionDto;
import com.opsbeach.connect.ticket.entity.TicketAction;
import com.opsbeach.connect.ticket.enums.TicketType;
import com.opsbeach.connect.ticket.repository.TicketActionRepository;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketActionService {
    
    private final TicketActionRepository ticketActionRepository;

    private final ResponseMessage responseMessage;

    // create ticket action from ticket created response from ticket service.
    public TicketActionDto add(String ticketActionType, Long ticketId, String refrenceId) {
        var ticketAction = TicketAction.builder().ticketId(ticketId).type(TicketType.valueOf(ticketActionType)).referenceId(refrenceId).build();
        ticketActionRepository.save(ticketAction);
        return ticketAction.toDto(ticketAction);
    }

    public TicketActionDto get(Long id) {
        var ticketAction = ticketActionRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.TICKET_ACTION)));
        return ticketAction.toDto(ticketAction);
    }

    public List<TicketActionDto> getAll() {
        var ticketActions = ticketActionRepository.findAll();
        return !ticketActions.isEmpty() ? ticketActions.stream().map(ticketActions.get(0)::toDto).collect(Collectors.toList()) : List.of();
    }
}
