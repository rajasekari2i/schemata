package com.opsbeach.connect.pagerduty.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.pagerduty.dto.ResponseDto;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.LogEntryDto;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.ServiceDto;
import com.opsbeach.connect.pagerduty.dto.incidentmetrics.Filter;
import com.opsbeach.connect.pagerduty.dto.incidentmetrics.RequestBody;
import com.opsbeach.connect.pagerduty.entity.Incident;
import com.opsbeach.connect.pagerduty.entity.IncidentMetrics;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.utils.DateUtil;
import com.opsbeach.sharedlib.utils.JsonUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * <p>
 * Performs Pagerduty integration.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PagerDutyProcessor {

    private final IncidentService incidentService;

    private final TaskService taskService;

    private final PagerDutyServiceService pagerDutyServiceService;

    private final App2AppService app2AppService;

    private final IncidentMetricsService incidentMetricsService;

    private final IncidentLogEntryService incidentLogEntryService;

    private final ConnectService connectService;

    private ServiceType serviceType = ServiceType.PAGER_DUTY;

    private Map<String, String> setHeaders(ConnectDto connectDto) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, connectDto.getAuthType().getKey().concat(connectDto.getAuthToken()));
        headers.putAll(JsonUtil.convertJsonToMap(connectDto.getHeaders()));
        return headers;
    }

    private String uriBuilderString(String url, int limit, int offset, String since, String until, String dateRange, Boolean total) {
        var builder = UriComponentsBuilder.fromUriString(url)
                                          .queryParam("limit", limit)
                                          .queryParam("offset", offset)
                                          .queryParam("since", since)
                                          .queryParam("until", until)
                                          .queryParam("date_range", dateRange)
                                          .queryParam("total", total);
        return builder.buildAndExpand().toUri().toString();
    }

    private String uriBuilderString(String url, int limit, int offset, Boolean total) {
        var builder = UriComponentsBuilder.fromUriString(url)
                                          .queryParam("total", total)
                                          .queryParam("limit", limit)
                                          .queryParam("offset", offset);
        return builder.buildAndExpand().toUri().toString();
    }

    private String uriBuilderString(String url, int limit, int offset, String since, String until, Boolean total) {
        var builder = UriComponentsBuilder.fromUriString(url)
                                            .queryParam("limit", limit)
                                            .queryParam("offset", offset)
                                            .queryParam("total", total);
        if(!ObjectUtils.isEmpty(since) && !ObjectUtils.isEmpty(until)) {
            builder = builder.queryParam("since", since)
                             .queryParam("until", until);
        }
        return builder.buildAndExpand().toUri().toString();
    }

    private List<Incident> filterIncident(PagerdutyResponseDto responseIncidents, List<String> resolvedIncidentIds, Map<String, Incident> incidentMap, Long clientId) {
        var incidentDtos = responseIncidents.getIncidents().stream()
                                            .filter(incidentDto -> !resolvedIncidentIds.contains(incidentDto.getId()))
                                            .map(incidentDto -> {
                                                    if(!ObjectUtils.isEmpty(incidentMap.get(incidentDto.getId()))) {
                                                        incidentDto.setDbId(incidentMap.get(incidentDto.getId()).getId());
                                                    }
                                                    incidentDto.setClientId(clientId);
                                                    return incidentDto;
                                            }).collect(Collectors.toList());
        return !ObjectUtils.isEmpty(incidentDtos) ? incidentDtos.stream().map(incidentDtos.get(0)::toDomain).collect(Collectors.toList()) : List.of();
    }

    private List<IncidentMetrics> filterIncidentMetrics(PagerdutyResponseDto responseBody, List<String> resolvedIncidentIds, Map<String, IncidentMetrics> incidentMetricsMap, Long clientId) {
        var incidentMetricsDtos = responseBody.getData().stream()
                                              .filter(incidentMetricsDto -> !resolvedIncidentIds.contains(incidentMetricsDto.getId()))
                                              .map(incidentMetricsDto -> {
                                                  if(!ObjectUtils.isEmpty(incidentMetricsMap.get(incidentMetricsDto.getId()))) {
                                                      incidentMetricsDto.setDbId(incidentMetricsMap.get(incidentMetricsDto.getId()).getId());
                                                  }
                                                  incidentMetricsDto.setClientId(clientId);
                                                  return incidentMetricsDto;
                                              }).collect(Collectors.toList());                                                
        return !ObjectUtils.isEmpty(incidentMetricsDtos) ? incidentMetricsDtos.stream().map(incidentMetricsDtos.get(0)::toDomain).collect(Collectors.toList()) : List.of();
    }

    private ResponseDto sendResponse(LocalDateTime date, ServiceType serviceType, TaskType taskType) {
        return ResponseDto.builder().lastSyncDateTime(date).connectorName(taskType).connectorType(serviceType).build();
    }

    @Transactional
    public ResponseDto addIncidents(Long taskId) {
        var taskDto = taskService.get(taskId);
        taskService.taskValidation(taskDto, TaskType.INCIDENTS, ServiceType.PAGER_DUTY);
        var connectDto = connectService.get(taskDto.getConnectId());
        var httpHeaders = setHeaders(connectDto);
        int limit = 0; 
        int offset = 0;
        String since = (!ObjectUtils.isEmpty(taskDto.getLastSyncDate())) ? DateUtil.currentDateTimeUTC().minusMonths(3).toString() : null;
        String until = (!ObjectUtils.isEmpty(taskDto.getLastSyncDate())) ? DateUtil.currentDateTimeUTC().toString() : null;
        String dateRange = (ObjectUtils.isEmpty(taskDto.getLastSyncDate())) ? "all" : null;
        List<String> resolvedIncidentIds = incidentService.getAllResolved(taskDto.getClientId()).stream().map(Incident::getIncidentId).collect(Collectors.toList());
        Map<String, Incident> incidentMap = incidentService.getAllNotResolved(taskDto.getClientId()).stream().collect(Collectors.toMap(Incident::getIncidentId, Function.identity()));
        List<Incident> incidents = new ArrayList<>();
        String url = uriBuilderString(taskDto.getUrl(), 100, offset, since, until, dateRange, true);
        HttpEntity<Object> entity = app2AppService.setHeaders(httpHeaders, null);
        do {
            PagerdutyResponseDto responseIncidents = app2AppService.httpGet(url, entity, PagerdutyResponseDto.class);
            incidents.addAll(filterIncident(responseIncidents, resolvedIncidentIds, incidentMap, taskDto.getClientId()));
            limit = responseIncidents.getIncidents().size();
            url = url.replace("offset=".concat(Integer.toString(offset)), "offset=".concat(Integer.toString(offset+limit)));
            offset = offset + limit;
        } while(limit == 100);
        incidentService.addAll(incidents);
        taskDto.setLastSyncDate(ObjectUtils.isEmpty(incidents) ? taskDto.getLastSyncDate() : incidents.get(incidents.size() - 1).getIncidentCreatedAt());
        taskService.update(taskDto);
        return sendResponse(DateUtil.currentDateTimeUTC(), serviceType, TaskType.INCIDENTS);
    }

    @Transactional
    public ResponseDto addServices(Long taskId) {
        var taskDto = taskService.get(taskId);
        taskService.taskValidation(taskDto, TaskType.SERVICES, ServiceType.PAGER_DUTY);
        var connectDto = connectService.get(taskDto.getConnectId());
        var httpHeaders = setHeaders(connectDto);
        Long clientId = taskDto.getClientId();
        int limit = 0;
        int offset = 0;
        Map<String, ServiceDto> serviceDtoMap = pagerDutyServiceService.getAll(clientId).stream().collect(Collectors.toMap(ServiceDto::getId, Function.identity()));
        List<ServiceDto> serviceDtos = new ArrayList<>();
        do {
            PagerdutyResponseDto responseServices = app2AppService.httpGet(uriBuilderString(taskDto.getUrl(), 100, offset, true), app2AppService.setHeaders(httpHeaders, null), PagerdutyResponseDto.class);
            if(ObjectUtils.isEmpty(taskDto.getLastSyncDate())) {
                taskDto.setLastSyncDate(DateUtil.convertDatetoLocalDateTimeUTC(responseServices.getServices().get(0).getCreatedAt()));
            }
            responseServices.getServices().stream().forEach(serviceDto -> {
                                                    if(!ObjectUtils.isEmpty(serviceDtoMap.get(serviceDto.getId()))) {
                                                        serviceDto.setDbId(serviceDtoMap.get(serviceDto.getId()).getDbId());
                                                        serviceDtoMap.remove(serviceDto.getId());
                                                    }
                                                    serviceDto.setClientId(clientId);
                                                    serviceDtos.add(serviceDto);
                                                    if(taskDto.getLastSyncDate().isBefore(DateUtil.convertDatetoLocalDateTimeUTC(serviceDto.getCreatedAt()))) {
                                                        taskDto.setLastSyncDate(DateUtil.convertDatetoLocalDateTimeUTC(serviceDto.getCreatedAt()));
                                                    }
                                                });
            limit = responseServices.getServices().size();
            offset = offset + limit;
        } while(limit == 100);
        if (!serviceDtoMap.isEmpty()) {
            List<Long> deletedIds = new ArrayList<>();
            serviceDtoMap.forEach((key, value) -> deletedIds.add(value.getDbId()) );
            pagerDutyServiceService.deleteAllByIds(deletedIds);
        }                                       
        pagerDutyServiceService.addAll(serviceDtos);
        taskService.update(taskDto);
        return sendResponse(DateUtil.currentDateTimeUTC(), serviceType, TaskType.SERVICES);
    }

    @Transactional
    public ResponseDto addIncidentMetrics(Long taskId) {
        var taskDto = taskService.get(taskId);
        taskService.taskValidation(taskDto, TaskType.INCIDENT_METRICS, ServiceType.PAGER_DUTY);
        var connectDto = connectService.get(taskDto.getConnectId());
        var httpHeaders = setHeaders(connectDto);
        httpHeaders.put(Constants.X_EARLY_ACCESS, Constants.ANALYTICS_V2);
        int limit = 0;
        LocalDateTime createdAtStart = (!ObjectUtils.isEmpty(taskDto.getLastSyncDate())) ? DateUtil.currentDateTimeUTC().minusMonths(3) : null;
        LocalDateTime createdAtEnd = DateUtil.currentDateTimeUTC();
        List<String> resolvedIncidentIds = incidentMetricsService.getAllResolved(taskDto.getClientId()).stream().map(IncidentMetrics::getIncidentId).collect(Collectors.toList());
        Map<String, IncidentMetrics> incidentMetricsMap = incidentMetricsService.getAllNotResolved(taskDto.getClientId()).stream().collect(Collectors.toMap(IncidentMetrics::getIncidentId, Function.identity()));
        List<IncidentMetrics> incidentMetrics = new ArrayList<>();
        var filter = Filter.builder().createdAtStart(createdAtStart).createdAtEnd(createdAtEnd).build();
        var requestBody = RequestBody.builder().filters(filter).order("asc").limit(1000).build();
        String url = taskDto.getUrl();
        do {
            filter.setCreatedAtStart(createdAtStart);
            requestBody.setFilters(filter);
            PagerdutyResponseDto responseBody = app2AppService.httpPost(url, app2AppService.setHeaders(httpHeaders, requestBody), PagerdutyResponseDto.class);
            incidentMetrics.addAll(filterIncidentMetrics(responseBody, resolvedIncidentIds, incidentMetricsMap, taskDto.getClientId()));
            limit = responseBody.getData().size();
            createdAtStart = limit == 0 ? null : DateUtil.convertDatetoLocalDateTimeUTC(responseBody.getData().get(limit-1).getCreatedAt()) .plusSeconds(1);
        } while(limit == 1000);
        incidentMetricsService.addAll(incidentMetrics);
        taskDto.setLastSyncDate(ObjectUtils.isEmpty(incidentMetrics) ? taskDto.getLastSyncDate() : incidentMetrics.get(incidentMetrics.size() - 1).getMetricsCreatedAt());
        taskService.update(taskDto);
        return sendResponse(DateUtil.currentDateTimeUTC(), serviceType, TaskType.INCIDENT_METRICS);
    }

    @Transactional
    public ResponseDto addLogEntry(Long taskId) {
        var taskDto = taskService.get(taskId);
        taskService.taskValidation(taskDto, TaskType.INCIDENT_LOG_ENTRY, ServiceType.PAGER_DUTY);
        var connectDto = connectService.get(taskDto.getConnectId());
        var httpHeaders = setHeaders(connectDto);
        int limit = 0;
        int offset = 0;
        String since = (!ObjectUtils.isEmpty(taskDto.getLastSyncDate())) ? taskDto.getLastSyncDate().plusSeconds(1).toString() : null;
        String until = (!ObjectUtils.isEmpty(taskDto.getLastSyncDate())) ? DateUtil.currentDateTimeUTC().toString() : null;
        List<LogEntryDto> logEntryDtos = new ArrayList<>();
        String url = uriBuilderString(taskDto.getUrl(), 100, offset, since, until, true);
        HttpEntity<Object> entity = app2AppService.setHeaders(httpHeaders, null);
        do {
            PagerdutyResponseDto responseLogEntry = app2AppService.httpGet(url, entity, PagerdutyResponseDto.class);
            if(ObjectUtils.isEmpty(taskDto.getLastSyncDate())) {
                taskDto.setLastSyncDate(DateUtil.convertDatetoLocalDateTimeUTC(responseLogEntry.getLogEntries().get(0).getCreatedAt()));
            }
            responseLogEntry.getLogEntries().forEach(logEntryDto -> {
                logEntryDto.setClientId(taskDto.getClientId());
                logEntryDtos.add(logEntryDto);
                if(taskDto.getLastSyncDate().isBefore(DateUtil.convertDatetoLocalDateTimeUTC(logEntryDto.getCreatedAt()))){
                    taskDto.setLastSyncDate(DateUtil.convertDatetoLocalDateTimeUTC(logEntryDto.getCreatedAt()));
                }
            });
            limit = responseLogEntry.getLogEntries().size();
            url = url.replace("offset=".concat(Integer.toString(offset)), "offset=".concat(Integer.toString(offset+limit)));
            offset = offset + limit;
        } while(limit == 100);
        incidentLogEntryService.addAll(logEntryDtos);
        taskService.update(taskDto);
        return sendResponse(DateUtil.currentDateTimeUTC(), serviceType, TaskType.INCIDENT_LOG_ENTRY);
    }
}
