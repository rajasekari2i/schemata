package com.opsbeach.connect.metrics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.metrics.dto.MetricsDto;
import com.opsbeach.connect.metrics.entity.Metrics;
import com.opsbeach.connect.metrics.repository.MetricsRepository;
import com.opsbeach.connect.pagerduty.entity.Incident;
import com.opsbeach.connect.pagerduty.entity.IncidentLogEntry;
import com.opsbeach.connect.pagerduty.service.IncidentLogEntryService;
import com.opsbeach.connect.pagerduty.service.IncidentService;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.entity.TicketAudit;
import com.opsbeach.connect.ticket.enums.TicketStatus;
import com.opsbeach.connect.ticket.repository.TicketAuditRepository;
import com.opsbeach.connect.ticket.service.TicketService;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

public class MetricsServiceTest {
    
    @InjectMocks
    private MetricsService metricsService;

    @Mock
    private MetricsRepository metricsRepository;

    @Mock
    private ResponseMessage responseMessage;

    @Spy
    private IdSpecifications<Metrics> metricsSpecifications;

    @Mock
    private TaskService taskService;

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketAuditRepository ticketAuditRepository;

    @Mock
    private IncidentService incidentService;

    @Mock
    private IncidentLogEntryService incidentLogEntryService;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void getAllTest() {
        List<Metrics> metricsList = List.of(Metrics.builder().serviceId("1").build());
        when(metricsRepository.findAll()).thenReturn(metricsList);
        assertEquals(1, metricsService.getAll().size());
        when(metricsRepository.findAll()).thenReturn(List.of());
        assertEquals(0, metricsService.getAll().size());
    }

    @Test
    public void addAllTest() {
        assertEquals(List.of(), metricsService.addAll(List.of()));
        List<Metrics> metricsList = List.of(Metrics.builder().serviceId("1").build());
        var metricsDtoList = List.of(metricsList.get(0).toDto(metricsList.get(0)));
        when(metricsRepository.saveAll(ArgumentMatchers.<List<Metrics>>any())).thenReturn(metricsList);
        assertEquals(1, metricsService.addAll(metricsDtoList).size());
    }

    @Test
    public void getBySourceIdTest() {
        assertNull(metricsService.getBySourceId("1", 1L));
        var metrics = Metrics.builder().sourceId("1").build();
        when(metricsRepository.findOne(ArgumentMatchers.<Specification<Metrics>>any())).thenReturn(Optional.of(metrics));
        var response = metricsService.getBySourceId("1", 1L);
        assertEquals("1", response.getSourceId());
    }

    @Test
    public void updateTest() {
        var metricsDto = MetricsDto.builder().serviceId("1").source(ServiceType.JIRA).source(ServiceType.PAGER_DUTY).id(1L).build();
        var metrics = metricsDto.toDomin(metricsDto);
            when(metricsRepository.findById(1L)).thenReturn(Optional.of(metrics));
            when(metricsRepository.save(ArgumentMatchers.any(Metrics.class))).thenReturn(metrics);
        var response = metricsService.update(metricsDto);
        assertEquals(metrics.getServiceId(), response.getServiceId());
    }

    @Test
    public void deleteTest() {
        var metricsDto = MetricsDto.builder().serviceId("1").source(ServiceType.PAGER_DUTY).id(1L).build();
        var metrics = metricsDto.toDomin(metricsDto);
            when(metricsRepository.findById(1L)).thenReturn(Optional.of(metrics));
            when(metricsRepository.save(ArgumentMatchers.any(Metrics.class))).thenReturn(metrics);
        var response = metricsService.delete(1L);
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.METRICS), response);

        assertThrows(RecordNotFoundException.class, () -> { metricsService.delete(2L); });
    }

    private List<TicketDto> getTickets() {
        List<TicketDto> tickets = new ArrayList<>();
        tickets.add(TicketDto.builder().id(1L).build());
        tickets.add(TicketDto.builder().id(2L).build());
        return tickets;
    }

    private List<TicketDto> getFiftyTickets() {
        List<TicketDto> tickets = new ArrayList<>();
        for (int i=0; i<50; i++) {
            tickets.add(TicketDto.builder().id((long) i).build());
        }        
        return tickets;
    }

    private List<TicketAudit> getTicketLogEntries() {
        List<TicketAudit> logEntries = new ArrayList<>();
        logEntries.add(TicketAudit.builder().id(1L).createdAt(LocalDateTime.now().minusHours(1)).ticketId(1L).status(TicketStatus.NEW).build());
        logEntries.add(TicketAudit.builder().id(2L).createdAt(LocalDateTime.now()).ticketId(1L).status(TicketStatus.PENDING).build());
        logEntries.add(TicketAudit.builder().id(3L).createdAt(LocalDateTime.now().plusHours(1)).ticketId(2L).status(TicketStatus.PENDING).build());
        logEntries.add(TicketAudit.builder().id(4L).createdAt(LocalDateTime.now().plusHours(2)).ticketId(2L).status(TicketStatus.SOLVED).build());
        return logEntries;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ticketMetricsComputationTest() {
        var taskDto = TaskDto.builder().id(1L).clientId(1L).build();
            when(taskService.get(anyLong())).thenReturn(taskDto);
        var tickets = getTickets();
            when(ticketService.getByPage(1, 50, taskDto.getClientId())).thenReturn(tickets);
        var logEntries = getTicketLogEntries();
            when(ticketAuditRepository.findAll(ArgumentMatchers.<Specification<TicketAudit>>any(), ArgumentMatchers.<Sort>any())).thenReturn(logEntries);
        metricsService.ticketMetricsComputation(1L);

        taskDto.setLastSyncDate(LocalDateTime.now());
            when(taskService.get(anyLong())).thenReturn(taskDto);
            when(ticketService.getByAfterUpdateAtAndPage(1, 50, taskDto.getLastSyncDate(), taskDto.getClientId())).thenReturn(getFiftyTickets(), List.of());
        var metrics = Metrics.builder().id(1L).firstReplyTime(50).sourceCreatedAt(LocalDateTime.now().minusDays(1)).sourceId("2").build();
            when(metricsRepository.findOne(ArgumentMatchers.<Specification<Metrics>>any())).thenReturn(Optional.of(metrics));
        metricsService.ticketMetricsComputation(1L);
    }

    private List<Incident> getIncidents() {
        List<Incident> incidents = new ArrayList<>();
        incidents.add(Incident.builder().id(1L).build());
        incidents.add(Incident.builder().id(2L).build());
        return incidents;
    }

    private List<Incident> getFiftyIncidents() {
        List<Incident> incidents = new ArrayList<>();
        for (int i=0; i<50; i++) {
            incidents.add(Incident.builder().id((long) i).build());
        }        
        return incidents;
    }

    private List<IncidentLogEntry> getIncidentLogEntries() {
        List<IncidentLogEntry> logEntries = new ArrayList<>();
        logEntries.add(IncidentLogEntry.builder().id(1L).entryCreatedAt(LocalDateTime.now().minusHours(1)).incidentId("1").type("notify_log_entry").build());
        logEntries.add(IncidentLogEntry.builder().id(2L).entryCreatedAt(LocalDateTime.now()).incidentId("1").type("acknowledge_log_entry").build());
        logEntries.add(IncidentLogEntry.builder().id(3L).entryCreatedAt(LocalDateTime.now().plusHours(1)).incidentId("2").type("acknowledge_log_entry").build());
        logEntries.add(IncidentLogEntry.builder().id(4L).entryCreatedAt(LocalDateTime.now().plusHours(1)).incidentId("2").type("resolve_log_entry").build());
        logEntries.add(IncidentLogEntry.builder().id(5L).entryCreatedAt(LocalDateTime.now().plusHours(1)).incidentId("3").type("assign_log_entry").build());
        logEntries.add(IncidentLogEntry.builder().id(6L).entryCreatedAt(LocalDateTime.now().plusHours(1)).incidentId("4").type("notify_log_entry").build());
        return logEntries;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void incidentMetricsComputationTest() {
        var taskDto = TaskDto.builder().id(1L).clientId(1L).build();
            when(taskService.get(anyLong())).thenReturn(taskDto);
        var incidents = getIncidents();
            when(incidentService.getByPage(1, 50, taskDto.getClientId())).thenReturn(incidents);
        var logEntries = getIncidentLogEntries();
            when(incidentLogEntryService.getByListofIncidentIds(anyList(), anyLong())).thenReturn(logEntries);
        metricsService.incidentMetricsComputation(1L);

        taskDto.setLastSyncDate(LocalDateTime.now());
            when(taskService.get(anyLong())).thenReturn(taskDto);
            when(incidentService.getByAfterUpdateAtAndPage(1, 50, taskDto.getLastSyncDate(), taskDto.getClientId())).thenReturn(getFiftyIncidents(), List.of());
        var metrics = Metrics.builder().id(1L).firstReplyTime(0).sourceCreatedAt(LocalDateTime.now().minusDays(1)).sourceId("2").build();
            when(metricsRepository.findOne(ArgumentMatchers.<Specification<Metrics>>any())).thenReturn(Optional.of(metrics));
        metricsService.incidentMetricsComputation(1L);
    }
}
