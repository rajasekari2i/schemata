package com.opsbeach.connect.github.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.github.dto.EventAuditDto;

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

@SuperBuilder
@Entity
@Table(name = "event_audit")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EventAudit extends BaseModel {
    @Enumerated(EnumType.STRING)
    private Type type;
    @Column(name = "client_name")
    private String clientName;
    @Setter
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "event_id")
    private Long eventId;
    @Setter
    private String error;
    @Column(name = "initiated_by")
    private String initiatedBy;

    public enum Status {
        PENDING(1), IN_PROGRESS(2), COMPLETED(3), ERROR(4);

        private final int key;

        Status(int key) {
            this.key = key;
        }
        public int getKey() {
            return this.key;
        }
    }

    public enum Type {
        REPOSITORY_INITIAL_PULL(1), CSV_FILE_UPLOAD(2);

        private final int key;

        Type(int key) {
            this.key = key;
        }
        public int getKey() {
            return this.key;
        }
    }

    public EventAuditDto toDto(EventAudit eventAudit) {

        return EventAuditDto.builder().id(eventAudit.getId())
                                          .clientId(eventAudit.getClientId())
                                          .clientName(eventAudit.getClientName())
                                          .type(eventAudit.getType())
                                          .status(eventAudit.getStatus())
                                          .error(eventAudit.getError())
                                          .initiatedBy(eventAudit.getInitiatedBy())
                                          .build();
    }
}
