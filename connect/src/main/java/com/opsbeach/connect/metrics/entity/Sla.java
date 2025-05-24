package com.opsbeach.connect.metrics.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.metrics.dto.SlaDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Table
@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Sla extends BaseModel {
    
    @Enumerated(EnumType.STRING)
    private ServiceType type;

    @Column(name = "sla_time")
    private long slaTime;  // time should be in seconds.

    public SlaDto toDto(Sla sla) {
        return SlaDto.builder().id(sla.getId())
                               .clientId(sla.getClientId())
                               .type(sla.getType())
                               .slaTime(sla.getSlaTime())
                               .build();
    }
}
