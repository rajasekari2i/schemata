package com.opsbeach.connect.github.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.github.dto.ActivityDto;
import com.opsbeach.connect.github.service.ActivityService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/activity")
public class ActivityController {
    
    private final ActivityService activityService;
    
    @PostMapping
    public SuccessResponse<ActivityDto> add(@RequestBody ActivityDto workflowDto) {
        return SuccessResponse.statusCreated(activityService.add(workflowDto));
    }

    @GetMapping("/{id}")
    public SuccessResponse<ActivityDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(activityService.get(id));
    }

    @GetMapping
    public SuccessResponse<List<ActivityDto>> findAllByWorkflowId(@RequestParam(name = "workflowId", required = false) Long workflowId) {
        return SuccessResponse.statusOk(activityService.findAllByWorkflowId(workflowId));
    }
}
