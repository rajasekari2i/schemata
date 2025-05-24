package com.opsbeach.connect.pagerduty.service;

import java.time.LocalDateTime;
import java.util.List;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.pagerduty.entity.Incident;
import com.opsbeach.connect.pagerduty.enums.IncidentStatus;
import com.opsbeach.connect.pagerduty.repository.IncidentRepository;
import com.opsbeach.sharedlib.utils.DateUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Operations on Incident.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IdSpecifications<Incident> incidentIdSpecifications;

    private static final String UPDATED_AT = "updatedAt";

    public List<Incident> addAll(List<Incident> incidents) {
        return incidentRepository.saveAll(incidents);
    }

    public List<Incident> getAllResolved(Long clientId) {
        Specification<Incident> bSpecification = incidentIdSpecifications.findByClientId(clientId).and(incidentIdSpecifications.statusEqualTo(IncidentStatus.RESOLVED.toString())).and(incidentIdSpecifications.greaterThanIncidentCreatedAt(DateUtil.currentDateTimeUTC().minusMonths(3))).and(incidentIdSpecifications.findByDeleted(Boolean.FALSE));
        return incidentRepository.findAll(bSpecification);
    }

    public List<Incident> getAllNotResolved(Long clientId) {
        Specification<Incident> baseSpecifcation = incidentIdSpecifications.findByClientId(clientId).and(incidentIdSpecifications.statusNotEqualTo(IncidentStatus.RESOLVED.toString())).and(incidentIdSpecifications.greaterThanIncidentCreatedAt(DateUtil.currentDateTimeUTC().minusMonths(3))).and(incidentIdSpecifications.findByDeleted(Boolean.FALSE));
        return incidentRepository.findAll(baseSpecifcation);
    }

    public List<Incident> getByPage(int page, int size, Long clientId) {
        var specification = incidentIdSpecifications.findByClientId(clientId).and(incidentIdSpecifications.findByDeleted(Boolean.FALSE));
        var incidents = incidentRepository.findAll(specification, PageRequest.of(page -1, size, Sort.by(Sort.Direction.ASC, UPDATED_AT)));
        return incidents.getContent();
    }

    public List<Incident> getByAfterUpdateAtAndPage(int page, int size, LocalDateTime updatedAt, Long clientId) {
        var specification = incidentIdSpecifications.findByClientId(clientId).and(incidentIdSpecifications.greaterThanUpdatedAt(updatedAt)).and(incidentIdSpecifications.findByDeleted(Boolean.FALSE));
        var incidents = incidentRepository.findAll(specification, PageRequest.of(page-1, size, Sort.by(Sort.Direction.ASC, UPDATED_AT)));
        return incidents.getContent();        
    }
}
