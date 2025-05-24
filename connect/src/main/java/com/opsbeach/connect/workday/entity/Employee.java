package com.opsbeach.connect.workday.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.workday.dto.EmployeeDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends BaseModel {
    
    private String name;

    private String designation;
    @Column(name = "team_id")
    private Long teamId;
    @Column(name = "team_name")
    private String teamName;
    @Column(name = "manager_id")
    private Long managerId;
    @Column(name = "manager_name")
    private String managerName;
    @Column(name = "manager_designation")
    private String managerDesignation;

    public EmployeeDto toDto(Employee employee) {
        return EmployeeDto.builder().clientId(employee.getClientId())
                                    .id(employee.getId()) 
                                    .name(employee.getName())
                                    .designation(employee.getDesignation())
                                    .teamId(employee.getTeamId())
                                    .teamName(employee.getTeamName())
                                    .managerId(employee.getManagerId())
                                    .managerName(employee.getManagerName())
                                    .managerDesignation(employee.getManagerDesignation())
                                    .build();
    }
}
