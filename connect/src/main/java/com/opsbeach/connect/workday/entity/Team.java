package com.opsbeach.connect.workday.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.workday.dto.TeamDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Teams table
 * </p>
 */
@Entity
@Table
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Team extends BaseModel {
    
    private String name;

    private String description;
    @Column(name = "pillar_id")
    private Long pillarId;
    @Column(name = "team_channel")
    private String teamChannel;
    @Column(name = "cost_center_id")
    private Long costCenterId;
    @Column(name = "parent_team_id")
    private Long parentTeamId;
    @Column(name = "manager_id")
    private Long managerId;

    private String status;

    public TeamDto toDto(Team team) {
        return TeamDto.builder().clientId(team.getClientId())
                            .id(team.getId())
                            .name(team.getName())
                            .description(team.getDescription())
                            .pillarId(team.getPillarId())
                            .teamChannel(team.getTeamChannel())
                            .costCenterId(team.getCostCenterId())
                            .parentTeamId(team.getParentTeamId())
                            .managerId(team.getManagerId())
                            .status(team.getStatus())
                            .build();
    }
}
