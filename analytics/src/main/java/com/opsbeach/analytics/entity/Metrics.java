package com.opsbeach.analytics.entity;

import java.time.LocalDateTime;

import com.opsbeach.analytics.core.BaseModel;
import com.opsbeach.analytics.core.enums.IncidentStatus;
import com.opsbeach.analytics.core.enums.ServiceType;
import com.opsbeach.analytics.dto.MetricsDto;

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
public class Metrics extends BaseModel {
    
    @Enumerated(EnumType.STRING)
    private ServiceType source;

    private String sourceId;

    private LocalDateTime sourceCreatedAt;

    private String serviceId;

    private long timeToAcknowledge;

    private long firstReplyTime;

    private long timeToResolve;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    public MetricsDto toDto(Metrics metrics) {
        return MetricsDto.builder().id(metrics.getId())
                                   .clientId(metrics.getClientId())
                                   .serviceId(metrics.getServiceId())
                                   .source(metrics.getSource())
                                   .timeToAcknowledge(metrics.getTimeToAcknowledge())
                                   .firstReplyTime(metrics.getFirstReplyTime())
                                   .timeToResolve(metrics.getTimeToResolve())
                                   .status(metrics.getStatus())
                                   .build();
    }
}