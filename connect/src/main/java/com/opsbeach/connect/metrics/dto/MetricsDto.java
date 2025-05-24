package com.opsbeach.connect.metrics.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.metrics.entity.Metrics;
import com.opsbeach.connect.pagerduty.enums.IncidentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MetricsDto extends BaseDto {
    
    private ServiceType source;

    @JsonProperty("source_id")
    private String sourceId;

    @JsonProperty("source_created_at")
    private LocalDateTime sourceCreatedAt;

    @JsonProperty("service_id")
    private String serviceId;

    @Setter
    @JsonProperty("time_to_acknowledge")
    private long timeToAcknowledge;

    @Setter
    @JsonProperty("first_reply_time")
    private long firstReplyTime;

    @Setter
    @JsonProperty("time_to_resolve")
    private long timeToResolve;

    @Setter
    private IncidentStatus status;

    public Metrics toDomin(MetricsDto metricsDto) {
        return Metrics.builder().id(metricsDto.getId())
                                .clientId(metricsDto.getClientId())
                                .createdAt(metricsDto.getCreatedAt())
                                .updatedAt(metricsDto.getUpdatedAt())
                                .serviceId(metricsDto.getServiceId())
                                .source(metricsDto.getSource())
                                .sourceId(metricsDto.getSourceId())
                                .sourceCreatedAt(metricsDto.getSourceCreatedAt())
                                .timeToAcknowledge(metricsDto.getTimeToAcknowledge())
                                .firstReplyTime(metricsDto.getFirstReplyTime())
                                .timeToResolve(metricsDto.getTimeToResolve())
                                .status(metricsDto.getStatus())
                                .build();
    }
}
