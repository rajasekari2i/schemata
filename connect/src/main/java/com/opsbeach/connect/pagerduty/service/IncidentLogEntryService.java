package com.opsbeach.connect.pagerduty.service;

import java.util.List;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.LogEntryDto;
import com.opsbeach.connect.pagerduty.entity.IncidentLogEntry;
import com.opsbeach.connect.pagerduty.repository.IncidentLogEntryRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidentLogEntryService {

    private final IncidentLogEntryRepository incidentLogEntryRepository;

    private final IdSpecifications<IncidentLogEntry> logEntrySpecifications;

    private static final String ENTRY_CREATED_AT = "entryCreatedAt";

    public List<IncidentLogEntry> addAll(List<LogEntryDto> logEntryDtos) {
        List<IncidentLogEntry> entries = !ObjectUtils.isEmpty(logEntryDtos) ? logEntryDtos.stream().map(logEntryDtos.get(0)::toDomin).collect(Collectors.toList()) : List.of();
        return incidentLogEntryRepository.saveAll(entries);
    }

    public List<IncidentLogEntry> getByListofIncidentIds(List<String> incidentIds, Long clientId) {
        var specifications = logEntrySpecifications.findByClientId(clientId).and(logEntrySpecifications.findIncidentLogEntryByIncidentId(incidentIds)).and(logEntrySpecifications.findByDeleted(Boolean.FALSE));
        return incidentLogEntryRepository.findAll(specifications, Sort.by(Sort.Direction.ASC, ENTRY_CREATED_AT));
    }
}
