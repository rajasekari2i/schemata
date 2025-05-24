package com.opsbeach.connect.workday.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.workday.service.WorkdayService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/workday")
@RequiredArgsConstructor
public class WorkdayController {

    private final WorkdayService workdayService;
    
    @Transactional
    @PostMapping
    public SuccessResponse<String> add() {
        return SuccessResponse.statusCreated(workdayService.add());
    }
}
