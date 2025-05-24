package com.opsbeach.connect.askob.workspace.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.askob.workspace.dto.AskobWorkspaceDto;
import com.opsbeach.connect.askob.workspace.service.AskobWorkspaceService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/askob/workspace")
@RequiredArgsConstructor
public class AskobWorkspaceController {
    
    private final AskobWorkspaceService askobWorkspaceService;

    @Transactional
    @PostMapping
    public SuccessResponse<AskobWorkspaceDto> add(@RequestBody @Valid AskobWorkspaceDto askobWorkspaceDto) {
        return SuccessResponse.statusCreated(askobWorkspaceService.add(askobWorkspaceDto));
    }

    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<AskobWorkspaceDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(askobWorkspaceService.get(id));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<AskobWorkspaceDto>> getAll(@RequestParam(required = false, name = "key") String key) {
        return SuccessResponse.statusOk(askobWorkspaceService.getAll(key));
    }

    @Transactional
    @PutMapping
    public SuccessResponse<AskobWorkspaceDto> update(@RequestBody @Valid AskobWorkspaceDto askobWorkspaceDto) {
        return SuccessResponse.statusOk(askobWorkspaceService.update(askobWorkspaceDto));
    }
}
