package com.opsbeach.connect.workday.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.workday.entity.Team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TeamDto extends BaseDto {

    @Setter
    private String name;

    private String description;

    @Setter
    private Long pillarId;

    @JsonProperty("pillar_name")
    private String pillarName;

    @JsonProperty("team_channel")
    private String teamChannel;

    @JsonProperty("cost_center_id")
    private Long costCenterId;

    @JsonProperty("parent_team_id")
    private Long parentTeamId;

    @JsonProperty("manager_id")
    private Long managerId;

    private String status;

    public Team toDomain(TeamDto teamDto) {
        return Team.builder().clientId(teamDto.getClientId())
                             .id(teamDto.getId())
                             .name(teamDto.getName())
                             .description(teamDto.getDescription())
                             .pillarId(teamDto.getPillarId())
                             .teamChannel(teamDto.getTeamChannel())
                             .costCenterId(teamDto.getCostCenterId())
                             .parentTeamId(teamDto.getParentTeamId())
                             .managerId(teamDto.getManagerId())
                             .status(teamDto.getStatus())
                             .build();
    }
}
