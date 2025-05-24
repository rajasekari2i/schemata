package com.opsbeach.connect.workday.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opsbeach.connect.workday.entity.Employee;
import com.opsbeach.connect.workday.entity.Pillar;
import com.opsbeach.connect.workday.entity.Team;
import com.opsbeach.connect.workday.repository.EmployeeRepository;
import com.opsbeach.connect.workday.repository.PillarRepository;
import com.opsbeach.connect.workday.repository.TeamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkdayService {
    
    private final PillarRepository pillarRepository;

    private final TeamRepository teamRepository;

    private final EmployeeRepository employeeRepository;

    //create entire organization (pillar, teams and employees)
    public String add() {

        List<Pillar> pillars = new ArrayList<>();
        pillars.add(Pillar.builder().name("Analytics").build());
        pillars.add(Pillar.builder().name("Data Engineering").build());
        pillarRepository.saveAll(pillars);

        List<Team> teams = new ArrayList<>();
        teams.add(Team.builder().name("Streaming").pillarId(pillars.get(0).getId()).build());
        teams.add(Team.builder().name("Data Engineering").pillarId(pillars.get(1).getId()).build());
        teamRepository.saveAll(teams);

        var employee = Employee.builder().name("Kesavan_01").designation("CEO").teamId(teams.get(0).getId()).teamName(teams.get(0).getName()).build();
        employeeRepository.save(employee);

        List<Employee> employees = new ArrayList<>();
        employees.add(Employee.builder().name("Kesavan_02").designation("Manager").teamId(teams.get(0).getId()).teamName(teams.get(0).getName()).managerId(employee.getId()).managerName(employee.getName()).managerDesignation(employee.getDesignation()).build());
        employees.add(Employee.builder().name("Kesavan_03").designation("Manager").teamId(teams.get(1).getId()).teamName(teams.get(1).getName()).managerId(employee.getId()).managerName(employee.getName()).managerDesignation(employee.getDesignation()).build());
        employees.add(Employee.builder().name("Kesavan_04").designation("Manager").teamId(teams.get(1).getId()).teamName(teams.get(1).getName()).managerId(employee.getId()).managerName(employee.getName()).managerDesignation(employee.getDesignation()).build());
        employeeRepository.saveAll(employees);
       
        List<Employee> employees1 = new ArrayList<>();
        employees1.add(Employee.builder().name("Kesavan_05").designation("Developer").teamId(teams.get(0).getId()).teamName(teams.get(0).getName()).managerId(employees.get(0).getId()).managerName(employees.get(0).getName()).managerDesignation(employees.get(0).getDesignation()).build());
        employees1.add(Employee.builder().name("Kesavan_06").designation("Developer").teamId(teams.get(0).getId()).teamName(teams.get(0).getName()).managerId(employees.get(0).getId()).managerName(employees.get(0).getName()).managerDesignation(employees.get(0).getDesignation()).build());
        employees1.add(Employee.builder().name("Kesavan_07").designation("Developer").teamId(teams.get(1).getId()).teamName(teams.get(1).getName()).managerId(employees.get(1).getId()).managerName(employees.get(1).getName()).managerDesignation(employees.get(1).getDesignation()).build());
        employeeRepository.saveAll(employees1);

        List<Employee> employees2 = new ArrayList<>();
        employees2.add(Employee.builder().name("Kesavan_08").designation("Developer").teamId(teams.get(1).getId()).teamName(teams.get(1).getName()).managerId(employees1.get(2).getId()).managerName(employees1.get(2).getName()).managerDesignation(employees1.get(2).getDesignation()).build());
        employees2.add(Employee.builder().name("Kesavan_09").designation("Tester").teamId(teams.get(0).getId()).teamName(teams.get(0).getName()).managerId(employees1.get(0).getId()).managerName(employees1.get(0).getName()).managerDesignation(employees1.get(0).getDesignation()).build());
        employees2.add(Employee.builder().name("Kesavan_10").designation("Tester").teamId(teams.get(0).getId()).teamName(teams.get(0).getName()).managerId(employees1.get(1).getId()).managerName(employees1.get(1).getName()).managerDesignation(employees1.get(1).getDesignation()).build());
        employees2.add(Employee.builder().name("Kesavan_11").designation("Tester").teamId(teams.get(1).getId()).teamName(teams.get(1).getName()).managerId(employees1.get(2).getId()).managerName(employees1.get(2).getName()).managerDesignation(employees1.get(2).getDesignation()).build());
        employeeRepository.saveAll(employees2);

        employee = Employee.builder().name("Kesavan_12").designation("Tester").teamId(teams.get(1).getId()).teamName(teams.get(1).getName()).managerId(employees2.get(0).getId()).managerName(employees2.get(0).getName()).managerDesignation(employees2.get(0).getDesignation()).build();
        employeeRepository.save(employee);

        return "Organization Created Successfully";
    }
}
