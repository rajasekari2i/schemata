package com.opsbeach.connect.schemata.validate;

import java.util.List;

public record Result(Long id, String name, Status status, List<String> errorMessages) {
    
}
