package com.opsbeach.connect.metrics.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.metrics.dto.MetricsDto;
import com.opsbeach.connect.metrics.entity.Metrics;
import com.opsbeach.connect.metrics.repository.MetricsRepository;
import com.opsbeach.connect.pagerduty.entity.Incident;
import com.opsbeach.connect.pagerduty.entity.IncidentLogEntry;
import com.opsbeach.connect.pagerduty.enums.IncidentStatus;
import com.opsbeach.connect.pagerduty.service.IncidentLogEntryService;
import com.opsbeach.connect.pagerduty.service.IncidentService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.entity.TicketAudit;
import com.opsbeach.connect.ticket.enums.TicketStatus;
import com.opsbeach.connect.ticket.repository.TicketAuditRepository;
import com.opsbeach.connect.ticket.service.TicketService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;
import com.opsbeach.sharedlib.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricsService {
    
    private final MetricsRepository metricsRepository;

    private final ResponseMessage responseMessage;

    private final IdSpecifications<Metrics> metricsSpecifications;

    private final IdSpecifications<TicketAudit> ticketAuditSpecifications;

    private final TaskService taskService;

    private final TicketService ticketService;

    private final TicketAuditRepository ticketAuditRepository;

    private final IncidentService incidentService;

    private final IncidentLogEntryService incidentLogEntryService;

    private static final String CREATED_AT = "createdAt";

    public MetricsDto add(MetricsDto metricsDto) {
        metricsDto.setClientId(SecurityUtil.getClientId());
        var metrics = metricsRepository.save(metricsDto.toDomin(metricsDto));
        return metrics.toDto(metrics);
    }

    public List<MetricsDto> addAll(List<MetricsDto> metricsDtos) {
        if (!ObjectUtils.isEmpty(metricsDtos)) {
            var metricsList = metricsDtos.stream().map(metricsDtos.get(0)::toDomin).collect(Collectors.toList());
            metricsRepository.saveAll(metricsList);
            return metricsList.stream().map(metricsList.get(0)::toDto).collect(Collectors.toList());
        }
        return metricsDtos;
    }

    public MetricsDto get(Long id) {
        var metrics = metricsRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.METRICS)));
        return metrics.toDto(metrics);
    }

    public MetricsDto getBySourceId(String sourceId, Long clientId) {
        var specifications = metricsSpecifications.findByClientId(clientId).and(metricsSpecifications.findMetricsBySourceId(sourceId));
        var metrics = metricsRepository.findOne(specifications).orElse(null);
        return !ObjectUtils.isEmpty(metrics) ? metrics.toDto(metrics) : null;
    }

    public List<MetricsDto> getAll() {
        var metricsList = metricsRepository.findAll();
        return !ObjectUtils.isEmpty(metricsList) ? metricsList.stream().map(metricsList.get(0)::toDto).collect(Collectors.toList()) : List.of();
    }

    public MetricsDto update(MetricsDto metricsDto) {
        get(metricsDto.getId());
        return add(metricsDto);
    }

    public String delete(Long id) {
        var metricsDto = get(id);
        var metrics = metricsDto.toDomin(metricsDto);
        metrics.setIsDeleted(Boolean.TRUE);
        metricsRepository.save(metrics);
        return responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.METRICS);
    }

    @Transactional
    //method to calculate the metrics of Tickets from Obie
    public void ticketMetricsComputation(Long taskId) {
        var taskDto = taskService.get(taskId);
        taskService.taskValidation(taskDto, TaskType.TICKET_METRICS, ServiceType.METRICS);
        boolean isFirstTime = ObjectUtils.isEmpty(taskDto.getLastSyncDate());   //very first time for a client running to calculate metrics
        int limit;
        int page = 1;
        Map<String, MetricsDto> metricsMap = new HashMap<>();
        do {
            var tickets = isFirstTime ? ticketService.getByPage(page, 50, taskDto.getClientId()) : ticketService.getByAfterUpdateAtAndPage(1, 50, taskDto.getLastSyncDate(), taskDto.getClientId());
            var ticketIds = tickets.stream().map(TicketDto::getId).toList();
            var ticketAudits = ticketAuditRepository.findAll(ticketAuditSpecifications.findByClientId(taskDto.getClientId()).and(ticketAuditSpecifications.findTicketAuditByListOfTicketId(ticketIds)).and(ticketAuditSpecifications.findByDeleted(Boolean.FALSE)), Sort.by(Sort.Direction.ASC, CREATED_AT));
            ticketAudits.stream().forEach(logEntry -> {
                var ticketId = logEntry.getTicketId().toString();
                if (ObjectUtils.isEmpty(metricsMap.get(ticketId))) {
                    var metricsDto = !isFirstTime ? getBySourceId(ticketId, taskDto.getClientId()) : null;
                    if (ObjectUtils.isEmpty(metricsDto)) {
                        metricsDto = MetricsDto.builder().clientId(taskDto.getClientId()).source(ServiceType.SLACK).sourceId(ticketId).sourceCreatedAt(logEntry.getCreatedAt()).status(IncidentStatus.TRIGGERED).build();
                        metricsMap.put(ticketId, metricsDto);
                    } else {
                        metricsMap.put(ticketId, ticketTimeComputation(metricsDto, logEntry));
                    }
                } else {
                    metricsMap.put(ticketId, ticketTimeComputation(metricsMap.get(ticketId), logEntry));
                }
            });
            if (!tickets.isEmpty()) {
                taskDto.setLastSyncDate(tickets.get(tickets.size() - 1).getUpdatedAt());
            }
            limit = tickets.size();
            page++;
        } while(limit == 50);
        var metricsDtos = new ArrayList<>(metricsMap.values());
        addAll(metricsDtos);
        taskService.update(taskDto);
    }

    private MetricsDto ticketTimeComputation(MetricsDto metricsDto, TicketAudit logEntry) {
        if (metricsDto.getFirstReplyTime() == 0) {
            metricsDto.setFirstReplyTime(ChronoUnit.SECONDS.between(metricsDto.getSourceCreatedAt(), logEntry.getCreatedAt()));
        } 
        if (logEntry.getStatus().equals(TicketStatus.PENDING)) {
            metricsDto.setTimeToAcknowledge(ChronoUnit.SECONDS.between(metricsDto.getSourceCreatedAt(), logEntry.getCreatedAt()));
            metricsDto.setStatus(IncidentStatus.ACKNOWLEDGED);
        }
        else if (logEntry.getStatus().equals(TicketStatus.SOLVED)) {
            metricsDto.setTimeToResolve(ChronoUnit.SECONDS.between(metricsDto.getSourceCreatedAt(), logEntry.getCreatedAt()));
            metricsDto.setStatus(IncidentStatus.RESOLVED);
        }
        return metricsDto;
    }

    @Transactional
    //method to calculate the metrics of Incidents from pagerduty
    public void incidentMetricsComputation(Long taskId) {
        var taskDto = taskService.get(taskId);
        taskService.taskValidation(taskDto, TaskType.INCIDENT_METRICS, ServiceType.METRICS);
        boolean isFirstTime = ObjectUtils.isEmpty(taskDto.getLastSyncDate());   //very first time for a client running to calculate metrics
        int limit;
        int page = 1;
        Map<String, MetricsDto> metricsMap = new HashMap<>();
        do {
            var incidents = isFirstTime ? incidentService.getByPage(page, 50, taskDto.getClientId()) : incidentService.getByAfterUpdateAtAndPage(1, 50, taskDto.getLastSyncDate(), taskDto.getClientId());
            var incidentIds = incidents.stream().map(Incident::getIncidentId).toList();
            var incidentLogEntrys = incidentLogEntryService.getByListofIncidentIds(incidentIds, taskDto.getClientId());
            incidentLogEntrys.stream().forEach(logEntry -> {
                var incidentId = logEntry.getIncidentId();
                if (ObjectUtils.isEmpty(metricsMap.get(incidentId))) {
                    var metricsDto = !isFirstTime ? getBySourceId(incidentId, taskDto.getClientId()) : null; 
                    if (ObjectUtils.isEmpty(metricsDto)) {
                        metricsDto = MetricsDto.builder().clientId(taskDto.getClientId()).source(ServiceType.PAGER_DUTY).sourceId(incidentId).sourceCreatedAt(logEntry.getEntryCreatedAt()).status(IncidentStatus.TRIGGERED).build();
                        metricsMap.put(incidentId, metricsDto);
                    } else {
                        metricsMap.put(incidentId, incidentTimeComputation(metricsDto, logEntry));
                    }
                } else {
                    metricsMap.put(incidentId, incidentTimeComputation(metricsMap.get(incidentId), logEntry));
                }
            });
            if (!incidents.isEmpty()) {
                taskDto.setLastSyncDate(incidents.get(incidents.size() - 1).getUpdatedAt());
            }
            limit = incidents.size();
            page++;
        } while(limit == 50);
        var metricsDtos = new ArrayList<>(metricsMap.values());
        addAll(metricsDtos);
        taskService.update(taskDto);
    }

    private MetricsDto incidentTimeComputation(MetricsDto metrics, IncidentLogEntry logEntry) {
        if (metrics.getFirstReplyTime() == 0 && !logEntry.getType().equals("assign_log_entry") && !logEntry.getType().equals("notify_log_entry")) {
            metrics.setFirstReplyTime(ChronoUnit.SECONDS.between(metrics.getSourceCreatedAt(), logEntry.getEntryCreatedAt()));
        }
        if (logEntry.getType().equals("acknowledge_log_entry")) {
            metrics.setTimeToAcknowledge(ChronoUnit.SECONDS.between(metrics.getSourceCreatedAt(), logEntry.getEntryCreatedAt()));
            metrics.setStatus(IncidentStatus.ACKNOWLEDGED);
        }
        else if (logEntry.getType().equals("resolve_log_entry")) {
            metrics.setTimeToResolve(ChronoUnit.SECONDS.between(metrics.getSourceCreatedAt(), logEntry.getEntryCreatedAt()));
            metrics.setStatus(IncidentStatus.RESOLVED);
        }
        return metrics;
    }
}
