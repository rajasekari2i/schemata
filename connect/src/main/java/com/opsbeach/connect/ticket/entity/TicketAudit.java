package com.opsbeach.connect.ticket.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.ticket.dto.TicketAuditDto;
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
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ticket_audit")
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TicketAudit extends BaseModel {

    @Column(name = "ticket_created_by")
    private String ticketCreatedBy;

    @Column(name = "ticket_updated_by")
    private String ticketUpdatedBy;

    @Setter
    @Column(name = "ticket_id")
    private Long ticketId;

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

    public TicketAuditDto toDto(TicketAudit ticketAudit) {
        return TicketAuditDto.builder().id(ticketAudit.getId())
                                          .clientId(ticketAudit.getClientId())
                                          .createdAt(ticketAudit.getCreatedAt())
                                          .updatedAt(ticketAudit.getUpdatedAt())
                                          .ticketCreatedBy(ticketAudit.getTicketCreatedBy())
                                          .ticketUpdatedBy(ticketAudit.getTicketUpdatedBy())
                                          .ticketId(ticketAudit.getTicketId())
                                          .assignedTo(ticketAudit.getAssignedTo())
                                          .canonicalId(ticketAudit.getCanonicalId())
                                          .askobMessageId(ticketAudit.getAskobMessageId())
                                          .severity(ticketAudit.getSeverity())
                                          .status(ticketAudit.getStatus())
                                          .title(ticketAudit.getTitle())
                                          .description(ticketAudit.getDescription())
                                          .build();
    }
}
