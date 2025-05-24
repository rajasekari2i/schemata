package com.opsbeach.analytics.dto;

import com.opsbeach.analytics.core.BaseDto;
import com.opsbeach.analytics.entity.Incident;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDto extends BaseDto {
    private String status;

    public Incident toDomain(IncidentDto incidentDto) {
        return Incident.builder().clientId(incidentDto.getClientId())
                .build();
    }
}
