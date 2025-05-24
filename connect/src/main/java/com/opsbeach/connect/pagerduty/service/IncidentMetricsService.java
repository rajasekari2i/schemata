package com.opsbeach.connect.pagerduty.service;

import java.util.List;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.pagerduty.entity.IncidentMetrics;
import com.opsbeach.connect.pagerduty.repository.IncidentMetricsRepository;
import com.opsbeach.sharedlib.utils.DateUtil;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidentMetricsService {

    private final IncidentMetricsRepository incidentMetricsRepository;

    private final IdSpecifications<IncidentMetrics> incidentMetricsSpecifications;

    public List<IncidentMetrics> getAllNotResolved(Long clientId) {
        Specification<IncidentMetrics> bSpecification = incidentMetricsSpecifications.findByClientId(clientId).and(incidentMetricsSpecifications.resolvedIsNull()).and(incidentMetricsSpecifications.greaterThanIncidentMetricsCreatedAt(DateUtil.currentDateTimeUTC().minusMonths(3))).and(incidentMetricsSpecifications.findByDeleted(Boolean.FALSE));
        return incidentMetricsRepository.findAll(bSpecification);
    }

    public List<IncidentMetrics> addAll(List<IncidentMetrics> incidentMetrics) {
        return incidentMetricsRepository.saveAll(incidentMetrics);
    }

    public List<IncidentMetrics> getAllResolved(Long clientId) {
        Specification<IncidentMetrics> bSpecification = incidentMetricsSpecifications.findByClientId(clientId).and(incidentMetricsSpecifications.resolvedIsNotNull()).and(incidentMetricsSpecifications.greaterThanIncidentMetricsCreatedAt(DateUtil.currentDateTimeUTC().minusMonths(3))).and(incidentMetricsSpecifications.findByDeleted(Boolean.FALSE));
        return incidentMetricsRepository.findAll(bSpecification);
    }
    
}
