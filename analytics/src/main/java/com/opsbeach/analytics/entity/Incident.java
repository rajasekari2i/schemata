package com.opsbeach.analytics.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.opsbeach.analytics.core.BaseModel;
import com.opsbeach.analytics.dto.IncidentDto;

/**
 * <p>
 * Incident table
 * </p>
 */
@Entity
@Table
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Incident extends BaseModel {
    private String status;

    public IncidentDto toDto(Incident incident) {
        return IncidentDto.builder().clientId(incident.getClientId())
                .build();
    }
}
