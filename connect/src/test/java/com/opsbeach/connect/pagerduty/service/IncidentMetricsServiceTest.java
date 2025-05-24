package com.opsbeach.connect.pagerduty.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.pagerduty.entity.IncidentMetrics;
import com.opsbeach.connect.pagerduty.repository.IncidentMetricsRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class IncidentMetricsServiceTest {

    @InjectMocks 
    private IncidentMetricsService incidentMetricsService;

    @Mock
    private IncidentMetricsRepository incidentMetricsRepository;

    @Spy
    private IdSpecifications<IncidentMetrics> incidentMetricsSpecifications;
    
    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }   

    private List<IncidentMetrics> getMetricsList() {
        return List.of(IncidentMetrics.builder().incidentId("1").clientId(1L).build());
    }

    @Test
    public void getAllNotResolvedTest() {
        var metrics = getMetricsList();
        when(incidentMetricsRepository.findAll(ArgumentMatchers.<Specification<IncidentMetrics>>any())).thenReturn(metrics);
        var response = incidentMetricsService.getAllNotResolved(1L);
        assertEquals(metrics.get(0).getIncidentId(), response.get(0).getIncidentId());
    }

    @Test
    public void addAllTest() {
        var metrics = getMetricsList();
        when(incidentMetricsRepository.saveAll(ArgumentMatchers.<List<IncidentMetrics>>any())).thenReturn(metrics);
        var response = incidentMetricsService.addAll(metrics);
        assertEquals(metrics.get(0).getIncidentId(), response.get(0).getIncidentId());
    }

    @Test 
    public void getAllResolvedTest() {
        var metrics = getMetricsList();
        when(incidentMetricsRepository.findAll(ArgumentMatchers.<Specification<IncidentMetrics>>any())).thenReturn(metrics);
        var response = incidentMetricsService.getAllResolved(1L);
        assertEquals(metrics.get(0).getIncidentId(), response.get(0).getIncidentId());
    }
}
