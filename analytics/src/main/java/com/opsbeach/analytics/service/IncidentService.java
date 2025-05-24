package com.opsbeach.analytics.service;

import com.opsbeach.analytics.dto.IncidentDto;
import com.opsbeach.analytics.entity.Incident;
import com.opsbeach.analytics.repository.IncidentRepository;

import lombok.RequiredArgsConstructor;
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

    public IncidentDto add(IncidentDto incidentDto) {
        Incident incident = incidentDto.toDomain(incidentDto);
        return incident.toDto(addModel(incident));
    }

    public Incident addModel(Incident incident) {
        return incidentRepository.save(incident);
    }
}
