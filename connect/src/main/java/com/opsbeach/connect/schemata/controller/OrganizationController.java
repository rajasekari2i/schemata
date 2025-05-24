package com.opsbeach.connect.schemata.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.schemata.entity.Organization;
import com.opsbeach.connect.schemata.service.OrganizationService;
import com.opsbeach.sharedlib.response.SuccessResponse;
import com.opsbeach.sharedlib.utils.Constants;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/schema/organization")
@RequiredArgsConstructor
public class OrganizationController {
    
    public final OrganizationService organizationService;

    @PostMapping
    public SuccessResponse<String> add(@RequestParam("clientName") String clientName, HttpServletRequest httpServletRequest) {
        var clientId = httpServletRequest.getHeader(Constants.CLIENT_ID_HEADER);
        if (Objects.isNull(clientId)) return SuccessResponse.statusCreated("SUCCESS");
        return SuccessResponse.statusCreated(organizationService.add(Long.parseLong(clientId), clientName));
    }

    @GetMapping("/{id}")
    public SuccessResponse<Organization> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(organizationService.get(id));
    }

    @GetMapping
    public SuccessResponse<List<Organization>> getAll() {
        return SuccessResponse.statusOk(organizationService.getAll());
    }
}
