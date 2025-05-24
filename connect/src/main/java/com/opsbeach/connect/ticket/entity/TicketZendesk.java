package com.opsbeach.connect.ticket.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.ticket.dto.TicketZendeskDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ticket_zendesk")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TicketZendesk extends BaseModel {
    @Column(name = "ticket_id")
    private String ticketId;

    private String priority;

    private String status;

    private String summary;

    private String description;

    private String type;

    @Column(name = "ticket_created")
    private LocalDateTime ticketCreated;

    @Column(name = "ticket_updated")
    private LocalDateTime ticketUpdated;

    @Column(name = "requester_id")
    private String requesterId;

    @Column(name = "submitter_id")
    private String submitterId;

    @Column(name = "assignee_id")
    private String assigneeId;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "has_incidents")
    private Boolean hasIncidents;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    public TicketZendeskDto toDto(TicketZendesk ticketZendesk) {
        return TicketZendeskDto.builder().id(ticketZendesk.getId())
                                         .clientId(ticketZendesk.getClientId())
                                         .ticketId(ticketZendesk.getTicketId())
                                         .priority(ticketZendesk.getPriority())
                                         .status(ticketZendesk.getStatus())
                                         .summary(ticketZendesk.getSummary())
                                         .description(ticketZendesk.getDescription())
                                         .type(ticketZendesk.getType())
                                         .ticketCreated(ticketZendesk.getTicketCreated())
                                         .ticketUpdated(ticketZendesk.getTicketUpdated())
                                         .requesterId(ticketZendesk.getRequesterId())
                                         .submitterId(ticketZendesk.getSubmitterId())
                                         .assigneeId(ticketZendesk.getAssigneeId())
                                         .organizationId(ticketZendesk.getOrganizationId())
                                         .groupId(ticketZendesk.getGroupId())
                                         .isPublic(ticketZendesk.getIsPublic())
                                         .hasIncidents(ticketZendesk.getHasIncidents())
                                         .dueAt(ticketZendesk.getDueAt())
                                         .build();
    }
}
