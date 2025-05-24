package com.opsbeach.connect.workday.controller;

import java.util.List;

import javax.validation.Valid;

import com.opsbeach.connect.workday.dto.EmployeeDto;
import com.opsbeach.connect.workday.service.EmployeeService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {

    public final EmployeeService employeeService;
    
    @Transactional
    @PostMapping 
    public SuccessResponse<EmployeeDto> add(@RequestBody @Valid EmployeeDto employeeDto) {
        return SuccessResponse.statusCreated(employeeService.add(employeeDto));
    }

    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<EmployeeDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(employeeService.get(id));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<EmployeeDto>> getAll() {
        return SuccessResponse.statusOk(employeeService.getAll());
    }

    @Transactional
    @PutMapping
    public SuccessResponse<EmployeeDto> update(@RequestBody @Valid EmployeeDto employeeDto) {
        return SuccessResponse.statusOk(employeeService.update(employeeDto));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public SuccessResponse<String> delete(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(employeeService.delete(id));
    }

    @Transactional
    @GetMapping("/tree")
    public SuccessResponse<Object> getEmployeeOrganization() {
        return SuccessResponse.statusOk(employeeService.getEmployeeOrganization());
    }
}
