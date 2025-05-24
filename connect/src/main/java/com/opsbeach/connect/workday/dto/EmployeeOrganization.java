package com.opsbeach.connect.workday.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeOrganization {
    
    private EmployeeDto employee;

    private List<EmployeeOrganization> children;
    
}
