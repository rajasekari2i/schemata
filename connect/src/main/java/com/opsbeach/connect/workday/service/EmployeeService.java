package com.opsbeach.connect.workday.service;

import java.util.List;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.workday.dto.EmployeeDto;
import com.opsbeach.connect.workday.dto.EmployeeOrganization;
import com.opsbeach.connect.workday.entity.Employee;
import com.opsbeach.connect.workday.repository.EmployeeRepository;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final TeamService teamService;

    private final ResponseMessage responseMessage;

    public EmployeeDto add(EmployeeDto employeeDto) {
        var teamDto = teamService.getByName(employeeDto.getTeamName());
        employeeDto.setManagerDesignation(null);
        employeeDto.setManagerName(null);
        if(!ObjectUtils.isEmpty(employeeDto.getManagerId())) {
            var managerDto = get(employeeDto.getManagerId());
            employeeDto.setManagerId(managerDto.getId());
            employeeDto.setManagerName(managerDto.getName());
            employeeDto.setManagerDesignation(managerDto.getDesignation());
        }
        employeeDto.setTeamId(teamDto.getId());
        var employee = employeeDto.toDomain(employeeDto);
        return employee.toDto(employeeRepository.save(employee));
    }

    public EmployeeDto get(Long id) {
        var employee = employeeRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.EMPLOYEE)));
        return employee.toDto(employee);
    }

    public List<EmployeeDto> getAll() {
        var employees = employeeRepository.findAll();
        return ObjectUtils.isEmpty(employees) ? List.of() : employees.stream().map(employees.get(0)::toDto).collect(Collectors.toList());   
    }

    public EmployeeDto update(EmployeeDto employeeDto) {
        get(employeeDto.getId());
        return add(employeeDto);
    }

    public String delete(Long id) {
        var employeeDto = get(id);
        var employee = employeeDto.toDomain(employeeDto);
        employee.setIsDeleted(Boolean.TRUE);
        employeeRepository.save(employee);
        return responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.EMPLOYEE);
    }

    public List<EmployeeOrganization> getEmployeeOrganization() {
        var employees = employeeRepository.findAll();
        return employees.stream().filter(employee -> ObjectUtils.isEmpty(employee.getManagerId()))
                                 .map(employee -> {
            var employeeDto = employee.toDto(employee);
            var employeeOrganization = new EmployeeOrganization();
            employeeOrganization.setEmployee(employeeDto);
            employeeOrganization.setChildren(getEmployeeOrganization(employeeDto.getId(), employees));
            return employeeOrganization;
        }).collect(Collectors.toList());
    }

    private List<EmployeeOrganization> getEmployeeOrganization(Long id, List<Employee> employees) {
        var childrens = employees.stream().filter(employee -> id.equals(employee.getManagerId())).collect(Collectors.toList());
        return childrens.stream().map(employee -> {
            var employeeDto = employee.toDto(employee);
            var employeeOrganization = new EmployeeOrganization();
            employeeOrganization.setEmployee(employeeDto);
            employeeOrganization.setChildren(getEmployeeOrganization(employeeDto.getId(), employees));
            return employeeOrganization;
        }).collect(Collectors.toList());
    }
}
