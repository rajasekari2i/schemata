package com.opsbeach.analytics.controller;

import com.opsbeach.analytics.dto.IncidentDto;
import com.opsbeach.analytics.service.IncidentService;
import com.opsbeach.sharedlib.response.SuccessResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "Incident Controller")
@RestController
@RequestMapping("v1/incident")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @Transactional
    @PostMapping()
    @ApiOperation(value = "Add Incident")
    public SuccessResponse<IncidentDto> add(@RequestBody @Valid IncidentDto incidentDto) {
        return SuccessResponse.statusCreated(incidentService.add(incidentDto));
    }

}
