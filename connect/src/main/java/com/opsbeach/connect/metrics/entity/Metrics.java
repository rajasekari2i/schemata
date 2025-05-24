package com.opsbeach.connect.metrics.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.metrics.dto.MetricsDto;
import com.opsbeach.connect.pagerduty.enums.IncidentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Metrics extends BaseModel {
    @Enumerated(EnumType.STRING)
    private ServiceType source;
    @Column(name = "source_id")
    private String sourceId;
    @Column(name = "source_created_at")
    private LocalDateTime sourceCreatedAt;
    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "time_to_acknowledge")
    private long timeToAcknowledge;
    @Column(name = "first_reply_time")
    private long firstReplyTime;
    @Column(name = "time_to_resolve")
    private long timeToResolve;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    public MetricsDto toDto(Metrics metrics) {
        return MetricsDto.builder().id(metrics.getId())
                                   .clientId(metrics.getClientId())
                                   .createdAt(metrics.getCreatedAt())
                                   .updatedAt(metrics.getUpdatedAt())
                                   .serviceId(metrics.getServiceId())
                                   .source(metrics.getSource())
                                   .sourceId(metrics.getSourceId())
                                   .sourceCreatedAt(metrics.getSourceCreatedAt())
                                   .timeToAcknowledge(metrics.getTimeToAcknowledge())
                                   .firstReplyTime(metrics.getFirstReplyTime())
                                   .timeToResolve(metrics.getTimeToResolve())
                                   .status(metrics.getStatus())
                                   .build();
    }
}
