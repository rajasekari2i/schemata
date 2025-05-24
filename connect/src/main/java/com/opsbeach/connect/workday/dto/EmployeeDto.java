package com.opsbeach.connect.workday.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.workday.entity.Employee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDto extends BaseDto {
    
    private String name;

    private String designation;

    @Setter
    @JsonProperty("team_id")
    private Long teamId;

    @JsonProperty("team_name")
    private String teamName;

    @Setter
    @JsonProperty("manager_id")
    private Long managerId;

    @Setter
    @JsonProperty("manager_name")
    private String managerName;

    @Setter
    @JsonProperty("manager_designation")
    private String managerDesignation;

    public Employee toDomain(EmployeeDto employeeDto) {
        return Employee.builder().clientId(employeeDto.getClientId())
                                 .id(employeeDto.getId())
                                 .name(employeeDto.getName())
                                 .designation(employeeDto.getDesignation())
                                 .teamId(employeeDto.getTeamId())
                                 .teamName(employeeDto.getTeamName())
                                 .managerId(employeeDto.getManagerId())
                                 .managerName(employeeDto.getManagerName())
                                 .managerDesignation(employeeDto.getManagerDesignation())
                                 .build();
    }
}
