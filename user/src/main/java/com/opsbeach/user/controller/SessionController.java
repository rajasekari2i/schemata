package com.opsbeach.user.controller;

import com.opsbeach.sharedlib.dto.SessionDto;
import com.opsbeach.sharedlib.response.SuccessResponse;
import com.opsbeach.user.service.SessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(tags = "Session Controller")
@RestController
@RequestMapping("v1/session")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Transactional
    @PostMapping()
    @ApiOperation(value = "Add Jwt Token")
    public SuccessResponse<SessionDto> add(@RequestBody @Valid SessionDto sessionDto) {
        return SuccessResponse.statusCreated(sessionService.add(sessionDto));
    }

}
