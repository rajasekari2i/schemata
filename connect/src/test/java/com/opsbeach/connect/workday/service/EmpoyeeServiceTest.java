package com.opsbeach.connect.workday.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.workday.dto.EmployeeDto;
import com.opsbeach.connect.workday.dto.TeamDto;
import com.opsbeach.connect.workday.entity.Employee;
import com.opsbeach.connect.workday.repository.EmployeeRepository;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class EmpoyeeServiceTest {
    
    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Spy
    private IdSpecifications<Employee> employeeSpecifications;

    @Mock
    private ResponseMessage responseMessage;

    @Mock
    private TeamService teamService;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void employeeTreeTest() {
        var employees = getEmployees();
        when(employeeRepository.findAll()).thenReturn(employees);
        var employeeTreeResponse = employeeService.getEmployeeOrganization();
        assertEquals(2, employeeTreeResponse.size());
        assertEquals(2, employeeTreeResponse.get(0).getChildren().size());
        assertEquals(1, employeeTreeResponse.get(0).getChildren().get(0).getChildren().size());

        assertEquals(employeeTreeResponse.get(0).getEmployee().getName(), employees.get(0).getName());
        assertEquals(employeeTreeResponse.get(1).getEmployee().getId(), employees.get(4).getId());
        assertEquals(employeeTreeResponse.get(0).getChildren().get(0).getEmployee().getName(), employees.get(1).getName());        
        assertEquals(employeeTreeResponse.get(0).getChildren().get(0).getChildren().get(0).getEmployee().getManagerId(), employees.get(2).getManagerId());  
        assertEquals(employeeTreeResponse.get(0).getChildren().get(1).getEmployee().getName(), employees.get(3).getName());      
    }

    private List<Employee> getEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(Employee.builder().id(1L).name("arun").build());
        employees.add(Employee.builder().id(2L).name("varun").managerId(1L).build());
        employees.add(Employee.builder().id(3L).name("gowtham").managerId(2L).build());
        employees.add(Employee.builder().id(4L).name("deepak").managerId(1L).build());
        employees.add(Employee.builder().id(5L).name("manoj").build());
        return employees;
    }

    private Employee getEmployee() {
        return Employee.builder().id(1L).name("arun").teamId(1L).teamName("streaming").managerId(2L)
                                 .managerName("varun").managerDesignation("manager").build();
    }

    @Test
    public void addTest() {
        var employee = Employee.builder().id(1L).name("arun").teamId(1L).teamName("streaming").build();
        var employeeDto = employee.toDto(employee);
        TeamDto teamDto = TeamDto.builder().id(1L).name("streaming").build();
        when(teamService.getByName(employeeDto.getTeamName())).thenReturn(teamDto);
        when(employeeRepository.save(ArgumentMatchers.<Employee>any())).thenReturn(employee);
        var response = employeeService.add(employeeDto);
        assertEquals(employeeDto.getId(), response.getId());
    }

    @Test
    public void getTestPass() {
        Employee employee = getEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        var employeeDto = employee.toDto(employee);
        var response = employeeService.get(1L);
        assertEquals(employeeDto.getId(), response.getId());
        assertEquals(employeeDto.getName(), response.getName());
    }

    @Test
    public void getTestFail() {
        assertThrows(RecordNotFoundException.class, () -> {
            employeeService.get(1L);
        });
    }

    @Test
    public void getAllTest() {
        var employees = getEmployees();
            when(employeeRepository.findAll()).thenReturn(employees);
        var employeeDtos = employees.stream().map(employees.get(0)::toDto).collect(Collectors.toList());
        var response = employeeService.getAll();
        assertEquals(employeeDtos.size(), response.size());
        assertEquals(employeeDtos.get(0).getId(), response.get(0).getId());
        assertEquals(employeeDtos.get(1).getId(), response.get(1).getId());
        assertEquals(employeeDtos.get(2).getName(), response.get(2).getName());
        assertNotEquals(employeeDtos.get(0).getId(), response.get(4).getId());
        assertNotEquals(employeeDtos.get(2).getName(), response.get(3).getName());
            when(employeeRepository.findAll()).thenReturn(null);
        assertEquals(0, employeeService.getAll().size());
    }

    @Test 
    public void updateTest() {
        Employee employee = getEmployee();
        EmployeeDto employeeDto = employee.toDto(employee);
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        TeamDto teamDto = TeamDto.builder().id(1L).name("streaming").build();
        when(teamService.getByName(employeeDto.getTeamName())).thenReturn(teamDto);
        Employee manager = Employee.builder().id(employee.getManagerId()).name(employee.getManagerName()).designation(employee.getManagerDesignation()).build();
        when(employeeRepository.findById(employeeDto.getManagerId())).thenReturn(Optional.of(manager));
        when(employeeRepository.save(Mockito.any(Employee.class))).thenReturn(employee);
        var response = employeeService.update(employeeDto);
        assertEquals(employeeDto.getId(), response.getId());
        assertEquals(employeeDto.getManagerDesignation(), response.getManagerDesignation());
    }

    @Test
    public void deleteTest() {
        Employee employee = getEmployee();
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.DELETED, "Employee"), employeeService.delete(employee.getId()));
    }
}
