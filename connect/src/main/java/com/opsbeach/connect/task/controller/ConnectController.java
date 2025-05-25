package com.opsbeach.connect.task.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/connect")
@RequiredArgsConstructor
public class ConnectController {

    private final ConnectService connectService;

    @Transactional
    @PostMapping
    public SuccessResponse<ConnectDto> add(@RequestBody @Valid ConnectDto connectDto) {
        return SuccessResponse.statusCreated(connectService.connect(connectDto));
    }

    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<ConnectDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(connectService.get(id));
    }

    @Transactional
    @PutMapping
    public SuccessResponse<ConnectDto> update(@RequestBody @Valid ConnectDto connectDto) {
        return SuccessResponse.statusOk(connectService.update(connectDto));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<ConnectDto>> getAll() {
        return SuccessResponse.statusOk(connectService.getAll());
    }

    @Transactional
    @GetMapping("/services")
    public SuccessResponse<String[]> getAllServiceType() {
        return SuccessResponse.statusOk(connectService.getAllServiceType());
    }
    
    @GetMapping("/check")
    public SuccessResponse<Map<String, Long>> checkConnectByServiceType() {
        return SuccessResponse.statusOk(connectService.checkConnect());
    }

    @GetMapping("/check/{type}")
    public SuccessResponse<ConnectDto> checkGithubConnect(@PathVariable("type") ServiceType serviceType) {
        return SuccessResponse.statusOk(connectService.get(serviceType));
    }

    @PatchMapping("/{id}/repo-org")
    public SuccessResponse<String> addRepoOrganization(@PathVariable("id") Long id, @RequestParam("repoOrg") String repoOrganization) {
        return SuccessResponse.statusOk(connectService.addRepoOrganization(id, repoOrganization));
    }
}
