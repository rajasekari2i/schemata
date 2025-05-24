package com.opsbeach.connect.workday.dto;

import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.workday.entity.Pillar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PillarDto extends BaseDto {

    @Setter
    private String name;

    private String description;

    private String status;

    public Pillar toDomain(PillarDto pillarDto) {
        return Pillar.builder().clientId(pillarDto.getClientId())
                               .id(pillarDto.getId())
                               .name(pillarDto.getName())
                               .description(pillarDto.getDescription())
                               .status(pillarDto.getStatus())
                               .build();
    }
}
