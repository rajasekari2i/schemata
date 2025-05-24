package com.opsbeach.connect.github.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.github.dto.SchemaFileAuditDto;
import com.opsbeach.connect.github.service.SchemaFileAuditService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/schema-file-audit")
@RequiredArgsConstructor
public class SchemaFileAuditController {
    
    private final SchemaFileAuditService schemaFileAuditService;

    @GetMapping
    public SuccessResponse<List<SchemaFileAuditDto>> getAll(@RequestParam(name = "clientRepoId", required = false) Long clientRepoId) {
        return SuccessResponse.statusOk(schemaFileAuditService.getAll(clientRepoId));
    }
}
