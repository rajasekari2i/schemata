package com.opsbeach.connect.pagerduty.dto;

import java.time.LocalDateTime;

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseDto {
    
    private LocalDateTime lastSyncDateTime;

    private ServiceType connectorType;

    private TaskType connectorName;

    private Object entity;
}
