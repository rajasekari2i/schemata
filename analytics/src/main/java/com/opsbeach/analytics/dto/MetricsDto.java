package com.opsbeach.analytics.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.analytics.core.BaseDto;
import com.opsbeach.analytics.core.enums.IncidentStatus;
import com.opsbeach.analytics.core.enums.ServiceType;
import com.opsbeach.analytics.entity.Metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MetricsDto extends BaseDto {
    
    private ServiceType source;

    private String sourceId;

    private LocalDateTime sourceCreatedAt;
    
    @JsonProperty("service_id")
    private String serviceId;

    @JsonProperty("time_to_acknowledge")
    private long timeToAcknowledge;

    @JsonProperty("first_reply_time")
    private long firstReplyTime;

    @JsonProperty("time_to_resolve")
    private long timeToResolve;

    private IncidentStatus status;

    public Metrics toDomin(MetricsDto metricsDto) {
        return Metrics.builder().id(metricsDto.getId())
                                .clientId(metricsDto.getClientId())
                                .serviceId(metricsDto.getServiceId())
                                .source(metricsDto.getSource())
                                .timeToAcknowledge(metricsDto.getTimeToAcknowledge())
                                .firstReplyTime(metricsDto.getFirstReplyTime())
                                .timeToResolve(metricsDto.getTimeToResolve())
                                .status(metricsDto.getStatus())
                                .build();
    }
}
