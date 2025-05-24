package com.opsbeach.connect.github.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.github.dto.AutoCompleteModelDto;
import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.service.ModelService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/model")
@RequiredArgsConstructor
public class ModelController {
    
    private final ModelService modelService;

    @GetMapping
    public SuccessResponse<List<Model>> getAll(@RequestParam(name = "domainId", required = false) Long domainId,
                                               @RequestParam(name = "clientRepoId", required = false) Long clientRepoId,
                                               @RequestParam(name = "path", required = false) String path) {
        return SuccessResponse.statusOk(modelService.getAll(domainId, clientRepoId, path));
    }

    @GetMapping("/auto-complete")
    public SuccessResponse<List<AutoCompleteModelDto>> findByNameLike(@RequestParam("name") String name) {
        return SuccessResponse.statusOk(modelService.findByNameLike(name));
    }
}
