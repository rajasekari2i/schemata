package com.opsbeach.analytics.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.analytics.dto.MetricsDto;
import com.opsbeach.analytics.dto.SlaMeterDto;
import com.opsbeach.analytics.service.MetricsService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {
    
    private final MetricsService metricsService;

    @GetMapping
    public SuccessResponse<List<MetricsDto>> getAll() {
        return SuccessResponse.statusOk(metricsService.getAll());
    }

    @GetMapping("sla-meter")
    public SuccessResponse<List<SlaMeterDto>> getSlaMeter(@RequestParam("from") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime from, 
                                                          @RequestParam("to") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime to) {
        return SuccessResponse.statusOk(metricsService.getTicketSlaMeter(from, to));
    }
}
