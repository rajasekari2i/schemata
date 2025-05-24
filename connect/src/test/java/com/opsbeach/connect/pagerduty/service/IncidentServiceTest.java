package com.opsbeach.connect.pagerduty.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.pagerduty.entity.Incident;
import com.opsbeach.connect.pagerduty.enums.IncidentStatus;
import com.opsbeach.connect.pagerduty.repository.IncidentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class IncidentServiceTest {
 
    @InjectMocks
    private IncidentService incidentService;

    @Mock
    private IncidentRepository incidentRepository;

    @Spy
    private IdSpecifications<Incident> incidentSpecifications;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }   
    
    private List<Incident> getIncidents() {
        return List.of(Incident.builder().incidentId("1").incidentCreatedAt(LocalDateTime.now())
                                         .status(IncidentStatus.ACKNOWLEDGED.name()).clientId(1L).build());
    }

    @Test
    public void addAllTest() {
        var incidents = getIncidents();        
        when(incidentRepository.saveAll(ArgumentMatchers.<List<Incident>>any())).thenReturn(incidents);
        var response = incidentService.addAll(incidents);
        assertEquals(incidents.get(0).getStatus(), response.get(0).getStatus());
    }

    @Test
    public void getAllResolvedTest() {
        var incidents = getIncidents();
        when(incidentRepository.findAll(ArgumentMatchers.<Specification<Incident>>any())).thenReturn(incidents);
        var response = incidentService.getAllResolved(1L);
        assertEquals(incidents.get(0).getStatus(), response.get(0).getStatus());
    }

    @Test
    public void getAllNotResolvedTest() {
        var incidents = getIncidents();
        when(incidentRepository.findAll(ArgumentMatchers.<Specification<Incident>>any())).thenReturn(incidents);
        var response = incidentService.getAllNotResolved(1L);
        assertEquals(incidents.get(0).getStatus(), response.get(0).getStatus());
    }

    @Test
    public void getByPageTest() {
        var incidents = getIncidents();
        var pageIncidents = new PageImpl<>(incidents);
        when(incidentRepository.findAll(ArgumentMatchers.<Specification<Incident>>any(), ArgumentMatchers.<Pageable>any())).thenReturn(pageIncidents);
        var response = incidentService.getByPage(1, 2, 1L);
        assertEquals(incidents.get(0).getStatus(), response.get(0).getStatus());
    }

    @Test
    public void getByAfterUpdateAtAndPageTest() {
        var incidents = getIncidents();
        var pageIncidents = new PageImpl<>(incidents);
        when(incidentRepository.findAll(ArgumentMatchers.<Specification<Incident>>any(), ArgumentMatchers.<Pageable>any())).thenReturn(pageIncidents);
        var response = incidentService.getByAfterUpdateAtAndPage(1, 2, null, 4L);
        assertEquals(incidents.get(0).getStatus(), response.get(0).getStatus());
    }
}
