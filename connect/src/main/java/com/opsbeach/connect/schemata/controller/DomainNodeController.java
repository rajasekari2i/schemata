package com.opsbeach.connect.schemata.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.schemata.entity.DomainNode;
import com.opsbeach.connect.schemata.service.DomainNodeService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/schema/domain")
public class DomainNodeController {
    
    private final DomainNodeService domainNodeService;

    @PostMapping("/org/{id}")
    public SuccessResponse<DomainNode> add(@RequestBody DomainNode domain, @PathVariable("id") Long orgId) {
        return SuccessResponse.statusCreated(domainNodeService.add(domain, orgId));
    }

    @GetMapping("/{id}")
    public SuccessResponse<DomainNode> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(domainNodeService.get(id));
    }

    @GetMapping("/org/{id}")
    public SuccessResponse<List<DomainNode>> getAll(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(domainNodeService.getAll(id));
    }
}
