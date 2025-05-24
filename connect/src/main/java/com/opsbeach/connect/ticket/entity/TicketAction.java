package com.opsbeach.connect.ticket.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.ticket.dto.TicketActionDto;
import com.opsbeach.connect.ticket.enums.TicketType;

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
@Table(name = "ticket_action")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TicketAction extends BaseModel {
    @Column(name = "ticket_id")
    private Long ticketId;

    @Enumerated(EnumType.STRING)
    private TicketType type;

    @Column(name = "reference_id")
    private String referenceId;

    public TicketActionDto toDto(TicketAction ticketAction) {
        return TicketActionDto.builder().clientId(ticketAction.getClientId())
                                        .id(ticketAction.getId())
                                        .createdAt(ticketAction.getCreatedAt())
                                        .updatedAt(ticketAction.getUpdatedAt())
                                        .createdBy(ticketAction.getCreatedBy())
                                        .updatedBy(ticketAction.getUpdatedBy())
                                        .ticketId(ticketAction.getTicketId())
                                        .type(ticketAction.getType())
                                        .referenceId(ticketAction.getReferenceId())
                                        .build();
    }
}
