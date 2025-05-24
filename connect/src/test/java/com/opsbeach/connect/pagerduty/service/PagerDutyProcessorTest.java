package com.opsbeach.connect.pagerduty.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.Yaml;

import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.IncidentDto;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.IncidentMetricsDto;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.LogEntryDto;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.ServiceDto;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.IncidentDto.AlertCount;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.LogEntryDto.FieldDto;
import com.opsbeach.connect.pagerduty.entity.Incident;
import com.opsbeach.connect.pagerduty.entity.IncidentMetrics;
import com.opsbeach.connect.pagerduty.enums.IncidentStatus;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.utils.JsonUtil;

import net.minidev.json.JSONObject;

public class PagerDutyProcessorTest {

    @InjectMocks
    private PagerDutyProcessor pagerDutyProcessor;

    @Mock
    private TaskService taskService;

    @Mock
    private IncidentService incidentService;

    @Mock
    private PagerDutyServiceService pagerDutyServiceService;

    @Mock
    private IncidentMetricsService incidentMetricsService;

    @Mock
    private IncidentLogEntryService incidentLogEntryService;

    @Mock
    private ConnectService connectService;

    @Mock
    private App2AppService app2AppService;

    private Map<String, String> pagerduty = new HashMap<>();

    private Object userEmail;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @BeforeEach
    public void init() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(new File("src/test/resources/application-test.yaml"));
        Yaml yaml = new Yaml();
        Map<String, Map<String,String>> data = yaml.load(inputStream);
        pagerduty = data.get("pager-duty");
        userEmail = data.get("user-email");
    }

    private Map<String, String> headers = new HashMap<>();
    
    private Map<String, String> setHeaders(ConnectDto connectDto) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, connectDto.getAuthType().getKey().concat(connectDto.getAuthToken()));
        headers.putAll(JsonUtil.convertJsonToMap(connectDto.getHeaders()));
        return headers;
    }

    private TaskDto createTask(TaskType taskType, int limit, LocalDateTime lastSyncDate) {
        return TaskDto.builder().id(1L).taskType(taskType).serviceType(ServiceType.PAGER_DUTY).lastSyncDate(lastSyncDate)
                      .url(pagerduty.get("incidents-url")).clientId(1L).build();
    }

    private ConnectDto createConnect() {
        Map<String, String> httpHeaders = new HashMap<>();
        httpHeaders.put(HttpHeaders.ACCEPT, Constants.PAGER_DUTY_ACCEPT);
        httpHeaders.put(HttpHeaders.CONTENT_TYPE, Constants.PAGER_DUTY_CONTENT_TYPE);
        return ConnectDto.builder().headers(new JSONObject(headers).toJSONString()).authToken(pagerduty.get("token")).authType(AuthType.TOKEN).userEmail(userEmail.toString()).build();
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
    
    private PagerdutyResponseDto getResponseIncidents(int size) {
        var field = FieldDto.builder().id("1").type("type").summary("summary").build();
        int limit = 0;
        List<IncidentDto> list = new ArrayList<>();
        while(limit < size) {
            var dto = IncidentDto.builder().id(Integer.toString(limit+1)).createdAt(new Date()).service(field).firstTriggerLogEntry(field).escalationPolicy(field).status(IncidentStatus.ACKNOWLEDGED)
                                           .alertCounts(AlertCount.builder().all(1).triggered(1).resolved(1).build()).clientId(1L).build();
            list.add(dto);
            limit++;
        } 
        return PagerdutyResponseDto.builder().incidents(list).limit(2).build();        
    }

    private List<Incident> getIncidentDto(int size) {
        int limit = 0;
        List<Incident> list = new ArrayList<>();
        while(limit < size) {
            var dto = Incident.builder().id(1L).incidentId(Integer.toString(limit+1)).clientId(1L).build();
            list.add(dto);
            limit++;
        } 
        return list;
    }

    @Test
    public void addIncidentsTest() {
        var taskDto = createTask(TaskType.INCIDENTS, 3, null);
            when(taskService.get(1L)).thenReturn(taskDto);
        var connectDto = createConnect();
            when(connectService.get(taskDto.getConnectId())).thenReturn(connectDto);
        headers = setHeaders(connectDto);
        var url = uriBuilderString(taskDto.getUrl(), 100, 0, null, null, "all", true);
            when(incidentService.getAllResolved(anyLong())).thenReturn(getIncidentDto(1));
            when(incidentService.getAllNotResolved(anyLong())).thenReturn(getIncidentDto(2));
            when(app2AppService.httpGet(url, null, PagerdutyResponseDto.class)).thenReturn(getResponseIncidents(3));
        url = url.replace("offset=".concat("0"), "offset=".concat("3"));
            when(app2AppService.httpGet(url, null, PagerdutyResponseDto.class)).thenReturn(getResponseIncidents(1));
        var response = pagerDutyProcessor.addIncidents(1L);
        assertEquals(TaskType.INCIDENTS, response.getConnectorName());

        taskDto = createTask(TaskType.INCIDENTS, 2, LocalDateTime.now());
            when(taskService.get(1L)).thenReturn(taskDto);
            when(app2AppService.httpGet(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), eq(PagerdutyResponseDto.class))).thenReturn(getResponseIncidents(0));
        response = pagerDutyProcessor.addIncidents(1L);
        assertEquals(TaskType.INCIDENTS, response.getConnectorName());
    }

    private String uriBuilderString(String url, int limit, int offset, Boolean total) {
        var builder = UriComponentsBuilder.fromUriString(url)
                                          .queryParam("total", total)
                                          .queryParam("limit", limit)
                                          .queryParam("offset", offset);
        return builder.buildAndExpand().toUri().toString();
    }

    private PagerdutyResponseDto getResponseServices(int size) {
        var limit = 1;
        List<ServiceDto> list = new ArrayList<>();
        while(limit <= size) {
            list.add(ServiceDto.builder().id(Integer.toString(limit)).createdAt(new Date()).name("service").build());
            limit++;
        }
        return PagerdutyResponseDto.builder().services(list).limit(size).build();
    }

    private List<ServiceDto> getPagerDutyServices(int size) {
        var limit = 1;
        List<ServiceDto> list = new ArrayList<>();
        while(limit <= size) {
            list.add(ServiceDto.builder().id(Integer.toString(limit)).name("service").build());
            limit++;
        }
        return list;
    }

    @Test
    public void addServicesTest() {
        var taskDto = createTask(TaskType.SERVICES, 0, null);
            when(taskService.get(1L)).thenReturn(taskDto);
        var connectDto = createConnect();
            when(connectService.get(taskDto.getConnectId())).thenReturn(connectDto);
        var url = uriBuilderString(taskDto.getUrl(), 100, 0, true);
            when(app2AppService.httpGet(url, null, PagerdutyResponseDto.class)).thenReturn(getResponseServices(2));
        var response = pagerDutyProcessor.addServices(1L);
        assertEquals(TaskType.SERVICES, response.getConnectorName());

        taskDto = createTask(TaskType.SERVICES, 0, LocalDateTime.now().minusDays(1));
            when(taskService.get(1L)).thenReturn(taskDto);
            when(pagerDutyServiceService.getAll(anyLong())).thenReturn(getPagerDutyServices(2));
            when(app2AppService.httpGet(url, null, PagerdutyResponseDto.class)).thenReturn(getResponseServices(3));
        response = pagerDutyProcessor.addServices(1L);
        assertEquals(TaskType.SERVICES, response.getConnectorName());

            when(pagerDutyServiceService.getAll(anyLong())).thenReturn(getPagerDutyServices(3));
            when(app2AppService.httpGet(url, null, PagerdutyResponseDto.class)).thenReturn(getResponseServices(2));
        response = pagerDutyProcessor.addServices(1L);
        assertEquals(TaskType.SERVICES, response.getConnectorName());
    }

    private PagerdutyResponseDto getResponseBody(int size) {
        var limit = 1;
        List<IncidentMetricsDto> list = new ArrayList<>();
        while(limit <= size) {
            list.add(IncidentMetricsDto.builder().id(Integer.toString(limit)).createdAt(new Date()).clientId(1L).build());
            limit++;
        }
        return PagerdutyResponseDto.builder().data(list).limit(size).build();
    }

    private List<IncidentMetrics> getIncidentMetrics(int size) {
        var limit = 1;
        List<IncidentMetrics> list = new ArrayList<>();
        while(limit <= size) {
            list.add(IncidentMetrics.builder().id(1L).incidentId(Integer.toString(limit)).clientId(2L).build());
            limit++;
        }
        return list;
    }

    @Test
    public void addIncidentMetricsTest() {
        var taskDto = createTask(TaskType.INCIDENT_METRICS, 3, null);
            when(taskService.get(1L)).thenReturn(taskDto);
            when(app2AppService.httpPost(taskDto.getUrl(), null, PagerdutyResponseDto.class)).thenReturn(getResponseBody(2));
        var connectDto = createConnect();
            when(connectService.get(taskDto.getConnectId())).thenReturn(connectDto);
        var response = pagerDutyProcessor.addIncidentMetrics(1L);
        assertEquals(TaskType.INCIDENT_METRICS, response.getConnectorName());

        taskDto = createTask(TaskType.INCIDENT_METRICS, 4, LocalDateTime.now());
            when(taskService.get(1L)).thenReturn(taskDto);
            when(incidentMetricsService.getAllResolved(anyLong())).thenReturn(getIncidentMetrics(1));
            when(incidentMetricsService.getAllNotResolved(anyLong())).thenReturn(getIncidentMetrics(2));
            when(app2AppService.httpPost(taskDto.getUrl(), null, PagerdutyResponseDto.class)).thenReturn(getResponseBody(3));
        response = pagerDutyProcessor.addIncidentMetrics(1L);
        assertEquals(TaskType.INCIDENT_METRICS, response.getConnectorName());
            when(app2AppService.httpPost(taskDto.getUrl(), null, PagerdutyResponseDto.class)).thenReturn(getResponseBody(0));
        response = pagerDutyProcessor.addIncidentMetrics(1L);
        assertEquals(TaskType.INCIDENT_METRICS, response.getConnectorName());
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

    private PagerdutyResponseDto getLogEntryDtos(int size) {
        var limit = 1;
        List<LogEntryDto> list = new ArrayList<>();
        while(limit <= size) {
            list.add(LogEntryDto.builder().id(Integer.toString(limit)).createdAt(new Date()).summary("Log Entryy").build());
            limit++;
        }
        return PagerdutyResponseDto.builder().logEntries(list).limit(size).build();
    }

    @Test
    public void addLogEntryTest() {
        var taskDto = createTask(TaskType.INCIDENT_LOG_ENTRY, 2, null);
            when(taskService.get(1L)).thenReturn(taskDto);
        var connectDto = createConnect();
            when(connectService.get(taskDto.getConnectId())).thenReturn(connectDto);
        var url = uriBuilderString(taskDto.getUrl(), 100, 0, null, null, true);
            when(app2AppService.httpGet(url, null, PagerdutyResponseDto.class)).thenReturn(getLogEntryDtos(2));
        url = uriBuilderString(taskDto.getUrl(), 100, 2, null, null, true);
            when(app2AppService.httpGet(url, null, PagerdutyResponseDto.class)).thenReturn(getLogEntryDtos(1));
        var response = pagerDutyProcessor.addLogEntry(1L);
        assertEquals(TaskType.INCIDENT_LOG_ENTRY, response.getConnectorName());

        taskDto = createTask(TaskType.INCIDENT_LOG_ENTRY, 2, LocalDateTime.now());
            when(taskService.get(1L)).thenReturn(taskDto);
            when(app2AppService.httpGet(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), eq(PagerdutyResponseDto.class))).thenReturn(getLogEntryDtos(0));
        response = pagerDutyProcessor.addLogEntry(1L);
        assertEquals(TaskType.INCIDENT_LOG_ENTRY, response.getConnectorName());
    }
}
