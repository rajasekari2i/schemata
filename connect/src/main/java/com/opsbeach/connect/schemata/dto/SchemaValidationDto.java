package com.opsbeach.connect.schemata.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SchemaValidationDto {
    
    Boolean status;
    List<String> errorMessages;
    String changes;
    Map<String, Map<String, List<String>>> errorMap;
}
