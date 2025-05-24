package com.opsbeach.connect.workday.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.workday.entity.Employee;
import com.opsbeach.connect.workday.entity.Pillar;
import com.opsbeach.connect.workday.entity.Team;
import com.opsbeach.connect.workday.repository.EmployeeRepository;
import com.opsbeach.connect.workday.repository.PillarRepository;
import com.opsbeach.connect.workday.repository.TeamRepository;

public class WorkdayServiceTest {
    
    @InjectMocks
    private WorkdayService workdayService;

    @Mock
    private PillarRepository pillarRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void addTest() {
        List<Pillar> pillars = new ArrayList<>();
        pillars.add(Pillar.builder().id(1L).clientId(1L).name("Analytics").build());
        pillars.add(Pillar.builder().id(2L).clientId(1L).name("Data Engineering").build());
        when(pillarRepository.saveAll(ArgumentMatchers.<List<Pillar>>any())).thenReturn(pillars);

        List<Team> teams = new ArrayList<>();
        teams.add(Team.builder().id(1L).clientId(1L).name("Streaming").pillarId(pillars.get(0).getId()).build());
        teams.add(Team.builder().id(2L).clientId(1L).name("Data Engineering").pillarId(pillars.get(1).getId()).build());
        when(teamRepository.saveAll(ArgumentMatchers.<List<Team>>any())).thenReturn(teams);

        var employee = Employee.builder().id(1L).clientId(1L).name("Kesavan_01").designation("CEO").teamId(teams.get(0).getId()).teamName(teams.get(0).getName()).build();
        when(employeeRepository.save(ArgumentMatchers.<Employee>any())).thenReturn(employee);

        List<Employee> employees = new ArrayList<>();
        employees.add(Employee.builder().id(1L).clientId(1L).name("Kesavan_02").designation("Manager").teamId(teams.get(0).getId()).teamName(teams.get(0).getName()).managerId(employee.getId()).managerName(employee.getName()).managerDesignation(employee.getDesignation()).build());
        employees.add(Employee.builder().id(2L).clientId(1L).name("Kesavan_03").designation("Manager").teamId(teams.get(1).getId()).teamName(teams.get(1).getName()).managerId(employee.getId()).managerName(employee.getName()).managerDesignation(employee.getDesignation()).build());
        employees.add(Employee.builder().id(3L).clientId(1L).name("Kesavan_04").designation("Manager").teamId(teams.get(1).getId()).teamName(teams.get(1).getName()).managerId(employee.getId()).managerName(employee.getName()).managerDesignation(employee.getDesignation()).build());
        when(employeeRepository.saveAll(ArgumentMatchers.<List<Employee>>any())).thenReturn(employees);
        assertEquals("Organization Created Successfully", workdayService.add());
    }
}
