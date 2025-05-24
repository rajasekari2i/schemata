package com.opsbeach.connect.ticket.dto;

import java.time.LocalDateTime;

import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.ticket.entity.TicketZendesk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TicketZendeskDto extends BaseDto {
    
    private String ticketId;

    private String priority;

    private String status;

    private String summary;

    private String description;

    private String type;

    private LocalDateTime ticketCreated;

    private LocalDateTime ticketUpdated;

    private String requesterId;

    private String submitterId;

    private String assigneeId;

    private String organizationId;

    private String groupId;
    
    private Boolean isPublic;

    private Boolean hasIncidents;

    private LocalDateTime dueAt;

    public TicketZendesk toDomin(TicketZendeskDto ticketZendeskDto) {
        return TicketZendesk.builder().id(ticketZendeskDto.getId())
                                      .clientId(ticketZendeskDto.getClientId())
                                      .ticketId(ticketZendeskDto.getTicketId())
                                      .priority(ticketZendeskDto.getPriority())
                                      .status(ticketZendeskDto.getStatus())
                                      .summary(ticketZendeskDto.getSummary())
                                      .description(ticketZendeskDto.getDescription())
                                      .type(ticketZendeskDto.getType())
                                      .ticketCreated(ticketZendeskDto.getTicketCreated())
                                      .ticketUpdated(ticketZendeskDto.getTicketUpdated())
                                      .requesterId(ticketZendeskDto.getRequesterId())
                                      .submitterId(ticketZendeskDto.getSubmitterId())
                                      .assigneeId(ticketZendeskDto.getAssigneeId())
                                      .organizationId(ticketZendeskDto.getOrganizationId())
                                      .groupId(ticketZendeskDto.getGroupId())
                                      .isPublic(ticketZendeskDto.getIsPublic())
                                      .hasIncidents(ticketZendeskDto.getHasIncidents())
                                      .dueAt(ticketZendeskDto.getDueAt())
                                      .build();
    }
}
