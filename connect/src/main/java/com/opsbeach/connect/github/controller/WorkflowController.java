package com.opsbeach.connect.github.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.github.dto.WorkflowDto;
import com.opsbeach.connect.github.service.WorkflowService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;
    
    @PostMapping
    public SuccessResponse<WorkflowDto> add(@RequestBody WorkflowDto workflowDto) {
        return SuccessResponse.statusCreated(workflowService.add(workflowDto));
    }

    @GetMapping("/{id}")
    public SuccessResponse<WorkflowDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(workflowService.get(id));
    }

    @GetMapping
    public SuccessResponse<List<WorkflowDto>> getAll() {
        return SuccessResponse.statusOk(workflowService.getAll());
    }

    @PostMapping("/fields")
    public SuccessResponse<Object> saveAndRaisePr(@RequestBody @Valid WorkflowDto workflowDto, 
                                                  @RequestParam(name = "clientRepoId", required = false) Long clientRepoId) {
        return SuccessResponse.statusOk(workflowService.saveAndRaisePr(workflowDto, clientRepoId));
    }
}
