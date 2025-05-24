package com.opsbeach.connect.github.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.github.dto.DomainDto;
import com.opsbeach.connect.github.service.DomainService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/domain")
@RequiredArgsConstructor
public class DomainController {
    
    private final DomainService domainService;

    @GetMapping("/{id}")
    public SuccessResponse<DomainDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(domainService.get(id));
    }

    @GetMapping
    public SuccessResponse<List<DomainDto>> getAll(@RequestParam(name = "clientRepoId", required = false) Long clientRepoId) {
        return SuccessResponse.statusOk(domainService.getAll(clientRepoId));
    }
}
