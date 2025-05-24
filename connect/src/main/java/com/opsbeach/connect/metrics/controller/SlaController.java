package com.opsbeach.connect.metrics.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.metrics.dto.SlaDto;
import com.opsbeach.connect.metrics.service.SlaService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/sla")
@RequiredArgsConstructor
public class SlaController {
    
    private final SlaService slaService;

    @Transactional
    @PostMapping
    public SuccessResponse<SlaDto> add(@RequestBody @Valid SlaDto slaDto) {
        return SuccessResponse.statusCreated(slaService.add(slaDto));
    }

    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<SlaDto> get(@PathVariable Long id) {
        return SuccessResponse.statusOk(slaService.get(id));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<SlaDto>> getAll() {
        return SuccessResponse.statusOk(slaService.getAll());
    }

    @Transactional
    @PutMapping
    public SuccessResponse<SlaDto> update(@RequestBody @Valid SlaDto slaDto) {
        return SuccessResponse.statusOk(slaService.update(slaDto));
    }
}
