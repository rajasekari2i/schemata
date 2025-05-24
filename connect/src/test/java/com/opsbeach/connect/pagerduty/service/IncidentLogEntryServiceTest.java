package com.opsbeach.connect.pagerduty.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.LogEntryDto;
import com.opsbeach.connect.pagerduty.entity.IncidentLogEntry;
import com.opsbeach.connect.pagerduty.repository.IncidentLogEntryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class IncidentLogEntryServiceTest {
    
    @InjectMocks
    private IncidentLogEntryService incidentLogEntryService;

    @Mock
    private IncidentLogEntryRepository incidentLogEntryRepository;

    @Spy
    private IdSpecifications<IncidentLogEntry> logEntrySpecifications;

   
    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }        

    @Test
    public void addAllTest() {
        assertEquals(0, incidentLogEntryService.addAll(null).size());
        var logEntryDtos = List.of(LogEntryDto.builder().clientId(1L).id("ab123").createdAt(new Date()).summary("Log entry test case").type("LogEntry").build());
        var logEntrys = logEntryDtos.stream().map(logEntryDtos.get(0)::toDomin).collect(Collectors.toList());
        when(incidentLogEntryRepository.saveAll(ArgumentMatchers.<List<IncidentLogEntry>>any())).thenReturn(logEntrys);
        var response = incidentLogEntryService.addAll(logEntryDtos);
        assertEquals(logEntrys.size(), response.size());
        assertEquals(logEntrys.get(0).getEntryId(), response.get(0).getEntryId());
        assertEquals(logEntrys.get(0).getEntryCreatedAt(), response.get(0).getEntryCreatedAt());
    }

    @Test
    public void getByListofIncidentIdsTest() {
        List<IncidentLogEntry> logEntrys = List.of(IncidentLogEntry.builder().id(1L).build());
        when(incidentLogEntryRepository.findAll(ArgumentMatchers.<Specification<IncidentLogEntry>>any(), ArgumentMatchers.<Sort>any())).thenReturn(logEntrys);
        var response = incidentLogEntryService.getByListofIncidentIds(List.of("e1"), 3L);
        assertEquals(logEntrys.get(0).getId(), response.get(0).getId());
    }
}
