package com.opsbeach.analytics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.analytics.core.specification.IdSpecifications;
import com.opsbeach.analytics.dto.MetricsDto;
import com.opsbeach.analytics.dto.SlaMeterDto;
import com.opsbeach.analytics.entity.Metrics;
import com.opsbeach.analytics.repository.MetricsRepository;
import com.opsbeach.sharedlib.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricsService {
    
    private final MetricsRepository metricsRepository;

    private final IdSpecifications<Metrics> metricsSpecifications;

    public List<MetricsDto> getAll() {
        var metricsList = metricsRepository.findAll(metricsSpecifications.findByClientId().and(metricsSpecifications.findByDeleted(Boolean.FALSE)));
        return !ObjectUtils.isEmpty(metricsList) ? metricsList.stream().map(metricsList.get(0)::toDto).toList() : List.of();
    }

    public List<SlaMeterDto> getTicketSlaMeter(LocalDateTime from, LocalDateTime to) {
        return metricsRepository.findTicketSlaMeter(SecurityUtil.getClientId(), from, to);
    }
}
