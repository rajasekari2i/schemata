package com.opsbeach.connect.ticket.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.enums.TicketSeverity;
import com.opsbeach.connect.ticket.enums.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticket extends BaseModel {

    @Column(name = "ticket_created_by")
    private String ticketCreatedBy;

    @Column(name = "ticket_updated_by")
    private String ticketUpdatedBy;

    @Column(name = "askob_message_id")
    private Long askobMessageId;

    @Column(name = "canonical_id")
    private String canonicalId;

    @Enumerated(EnumType.STRING)
    private TicketSeverity severity;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Column(name = "assigned_to")
    private String assignedTo;

    private String title;

    private String description;

    public TicketDto toDto(Ticket ticket) {
        return TicketDto.builder().id(ticket.getId())
                                  .clientId(ticket.getClientId())
                                  .createdAt(ticket.getCreatedAt())
                                  .updatedAt(ticket.getUpdatedAt())
                                  .ticketCreatedBy(ticket.getTicketCreatedBy())
                                  .ticketUpdatedBy(ticket.getTicketUpdatedBy())
                                  .assignedTo(ticket.getAssignedTo())
                                  .canonicalId(ticket.getCanonicalId())
                                  .severity(ticket.getSeverity())
                                  .askobMessageId(ticket.getAskobMessageId())
                                  .status(ticket.getStatus())
                                  .title(ticket.getTitle())
                                  .description(ticket.getDescription())
                                  .build();
    }
}
