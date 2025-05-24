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

import com.opsbeach.connect.metrics.dto.MetricsDto;
import com.opsbeach.connect.metrics.service.MetricsService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;
    
    @Transactional
    @PostMapping
    public SuccessResponse<MetricsDto> add(@RequestBody @Valid MetricsDto metricsDto) {
        return SuccessResponse.statusCreated(metricsService.add(metricsDto));
    }

    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<MetricsDto> get(@PathVariable Long id) {
        return SuccessResponse.statusOk(metricsService.get(id));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<MetricsDto>> getAll() {
        return SuccessResponse.statusOk(metricsService.getAll());
    }

    @Transactional
    @PutMapping
    public SuccessResponse<MetricsDto> update(@RequestBody @Valid MetricsDto metricsDto) {
        return SuccessResponse.statusOk(metricsService.update(metricsDto));
    }
}
