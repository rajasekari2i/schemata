package com.opsbeach.connect.metrics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.metrics.entity.Sla;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SlaDto extends BaseDto {
    
    private ServiceType type;

    @JsonProperty("sla_time")
    private long slaTime;  // should save in seconds
 
    public Sla toDomin(SlaDto slaDto) {
        return Sla.builder().id(slaDto.getId())
                            .clientId(slaDto.getClientId())
                            .type(slaDto.getType())
                            .slaTime(slaDto.getSlaTime())
                            .build();
    }
}
