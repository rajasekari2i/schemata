package com.opsbeach.connect.ticket.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.opsbeach.connect.askob.message.service.AskobMessageService;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.jira.service.JiraService;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.ticket.dto.TicketActionDto;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.entity.Ticket;
import com.opsbeach.connect.ticket.entity.TicketAudit;
import com.opsbeach.connect.ticket.enums.ActionType;
import com.opsbeach.connect.ticket.repository.TicketAuditRepository;
import com.opsbeach.connect.ticket.repository.TicketRepository;
import com.opsbeach.connect.zendesk.service.ZendeskService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.utils.Base62Util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {
    
    private final TicketRepository ticketRepository;

    private final AskobMessageService askobMessageService;

    private final IdSpecifications<Ticket> ticketSpecifications;

    private final ResponseMessage responseMessage;

    private final TicketAuditRepository ticketAuditRepository;

    private static final String UPDATED_AT = "updatedAt";

    private static final String KEY = "key";
    
    private static final String TICKET = "ticket";
    
    private static final String ID = "id";
    
    private final TaskService taskService;

    private final JiraService jiraService;

    private final ZendeskService zendeskService;

    private final TicketActionService ticketActionService;

    private Random random =  new Random();

    //create ticket from askob followed by create ticket audit
    public TicketDto add(TicketDto ticketDto) {
        ticketDto.setCanonicalId(canonicalId());
        var ticketAudit = ticketToLogEntry(ticketDto);
        var ticket = ticketRepository.save(ticketDto.toDomin(ticketDto));
        
        // save audit immediately after ticket created...
        ticketAudit.setTicketId(ticket.getId());
        ticketAuditRepository.save(ticketAudit);
        
        return ticket.toDto(ticket);
    }

    private String canonicalId() {
        Ticket ticket;
        String encodeStd;
        do {
            var num = SecurityUtil.getClientId() + System.currentTimeMillis() + random.nextLong(10000);
            encodeStd = Base62Util.base62Encode(num);
            ticket = ticketRepository.findOne(ticketSpecifications.findTicketByCanonicalId(encodeStd)).orElse(null);
        } while (ticket != null);
        return encodeStd;
    }

    // create ticket in client respective ticketing application and add entry in ticket_action.
    public String action(ActionType actionType, TicketDto ticketDto) {
        switch (actionType) {
            case CREATE_TICKET: {
                                    var taskDto = taskService.getByTypes(List.of(ServiceType.JIRA, ServiceType.ZENDESK), TaskType.CREATE_TICKET);
                                    var ticketResponse = createTicket(ticketDto, taskDto);
                                    var action = addTicketAction(ticketResponse, ticketDto.getId(), taskDto.getServiceType());
                                    return responseMessage.getSuccessMessage(SuccessCode.CREATED, "Ticket created with reference Id: "+action.getReferenceId());
                                }
                                
            case EMAIL:
                    // send email
                break;

            case SMS:
                    // send SMS
                break;
        }
        return null;
    }

    private JsonNode createTicket(TicketDto ticketDto, TaskDto taskDto) {
        var serviceType = taskDto.getServiceType();
        switch(serviceType) {
            case JIRA -> { return jiraService.addTicket(ticketDto, taskDto); }
            case ZENDESK -> { return zendeskService.createTicket(ticketDto, taskDto); }
            default -> throw new InvalidDataException(ErrorCode.INVALID_SERVICE_TYPE, responseMessage.getErrorMessage(ErrorCode.INVALID_SERVICE_TYPE, serviceType.name()));
        }       
    }

    private TicketActionDto addTicketAction(JsonNode ticketResponse, Long ticketId, ServiceType serviceType) {
        switch(serviceType) {
            case JIRA -> { return ticketActionService.add(serviceType.name(), ticketId, ticketResponse.get(KEY).asText()); }
            case ZENDESK -> { return ticketActionService.add(serviceType.name(), ticketId, ticketResponse.get(TICKET).get(ID).asText()); }
            default -> throw new InvalidDataException(ErrorCode.INVALID_SERVICE_TYPE, responseMessage.getErrorMessage(ErrorCode.INVALID_SERVICE_TYPE, serviceType.name()));
        }
    }

    private TicketAudit ticketToLogEntry(TicketDto ticketDto) {
        return TicketAudit.builder().clientId(ticketDto.getClientId())
                                       .assignedTo(ticketDto.getAssignedTo())
                                       .canonicalId(ticketDto.getCanonicalId())
                                       .ticketCreatedBy(ticketDto.getTicketCreatedBy())
                                       .ticketUpdatedBy(ticketDto.getTicketUpdatedBy())
                                       .askobMessageId(ticketDto.getAskobMessageId())
                                       .severity(ticketDto.getSeverity())
                                       .status(ticketDto.getStatus())
                                       .title(ticketDto.getTitle())
                                       .description(ticketDto.getDescription())
                                       .build();
    }

    public TicketDto get(Long id) {
        var ticket = ticketRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.TICKET)));
        return ticket.toDto(ticket);
    }

    public List<TicketDto> getAll() {
        var tickets = ticketRepository.findAll();
        return !tickets.isEmpty() ? tickets.stream().map(tickets.get(0)::toDto).collect(Collectors.toList()) : List.of();
    }

    public TicketDto update(TicketDto ticketDto) {
        var canonicalId = get(ticketDto.getId()).getCanonicalId();
        askobMessageService.get(ticketDto.getAskobMessageId());
        ticketDto.setClientId(SecurityUtil.getClientId());
        ticketDto.setCanonicalId(canonicalId);
        var ticketAudit = ticketToLogEntry(ticketDto);
        var ticket = ticketRepository.save(ticketDto.toDomin(ticketDto));
        // save log entry immediately after ticket created...
        ticketAudit.setTicketId(ticket.getId());
        ticketAuditRepository.save(ticketAudit);
        return ticket.toDto(ticket);
    }

    public List<TicketDto> getByPage(int page, int size, Long clientId) {
        var specification = ticketSpecifications.findByClientId(clientId);
        var tickets = ticketRepository.findAll(specification, PageRequest.of(page-1, size, Sort.by(Sort.Direction.ASC, UPDATED_AT)));
        var ticket = tickets.getContent();
        return !ticket.isEmpty() ? ticket.stream().map(ticket.get(0)::toDto).toList() : List.of();
    }

    public List<TicketDto> getByAfterUpdateAtAndPage(int page, int size, LocalDateTime updatedAt, Long clientId) {
        var specification = ticketSpecifications.findByClientId(clientId).and(ticketSpecifications.greaterThanUpdatedAt(updatedAt));
        var tickets = ticketRepository.findAll(specification, PageRequest.of(page-1, size, Sort.by(Sort.Direction.ASC, UPDATED_AT)));
        var ticket = tickets.getContent();
        return !ticket.isEmpty() ? ticket.stream().map(ticket.get(0)::toDto).toList() : List.of();
    }
}
