package com.opsbeach.connect.workday.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.workday.dto.PillarDto;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
/**
 * <p>
 * Pillar table
 * </p>
 */
@Entity
@Table
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Pillar extends BaseModel {

    private String name;

    private String description;

    private String status;

    public PillarDto toDto(Pillar pillar) {
        return PillarDto.builder().clientId(pillar.getClientId())
                                  .id(pillar.getId())
                                  .name(pillar.getName())
                                  .description(pillar.getDescription())
                                  .status(pillar.getStatus())
                                  .build();
    }
}
