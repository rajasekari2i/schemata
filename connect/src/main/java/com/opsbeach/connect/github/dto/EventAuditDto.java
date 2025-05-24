package com.opsbeach.connect.github.dto;

import com.opsbeach.connect.github.entity.EventAudit;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EventAuditDto {

    Long id;
    Long clientId;
    String clientName;
    EventAudit.Type type;
    Long eventId;
    EventAudit.Status status;
    String error;
    String initiatedBy;

    public EventAudit toDomain(EventAuditDto eventAuditDto) {

        return EventAudit.builder().id(eventAuditDto.getId())
                                       .clientId(eventAuditDto.getClientId())
                                       .clientName(eventAuditDto.getClientName())
                                       .type(eventAuditDto.getType())
                                       .eventId(eventAuditDto.getEventId())
                                       .status(eventAuditDto.getStatus())
                                       .error(eventAuditDto.getError())
                                       .initiatedBy(eventAuditDto.getInitiatedBy())
                                       .build();
    }
}
