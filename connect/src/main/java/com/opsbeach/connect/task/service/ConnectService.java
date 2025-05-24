package com.opsbeach.connect.task.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.schemata.validate.Status;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.entity.Connect;
import com.opsbeach.connect.task.repository.ConnectRepository;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.exception.UnAuthorizedException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.service.App2AppService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectService {
    
    private final ConnectRepository connectRepository;

    private final TaskService taskService;

    private final App2AppService app2AppService;

    private final ResponseMessage responseMessage;

    private final IdSpecifications<Connect> connectSpecifications;

    @Value("${pager-duty.incidents-url}")
    private String listIncidentUrl;

    @Value("${pager-duty.services-url}")
    private String listServiceUrl;

    @Value("${pager-duty.incident-metrics-url}")
    private String listIncidentMetricsUrl;

    @Value("${pager-duty.log-entry-url}")
    private String listLogEntryUrl;

    @Value("${jira.create-ticket-uri}")
    private String jiraCreateTicketUri;

    @Value("${jira.get-tickets-uri}")
    private String jiraGetTicketsUri;

    @Value("${jira.get-project-uri}")
    private String jiraGetProjectUri;

    @Value("${zendesk.ticket-count-uri}")
    private String zendeskTicketCountUri;

    @Value("${zendesk.create-ticket-uri}")
    private String zendeskCreateTicketUri;

    @Value("${zendesk.get-tickets-uri}")
    private String zendeskGetTicketUri;

    @Value("${slack.post-message-url}")
    private String slackPostMessageUrl;

    public ConnectDto connect(ConnectDto connectDto) {
        addMetricsTasks();
        var serviceType = connectDto.getServiceType();
        switch (serviceType) {
            case PAGER_DUTY -> { connectDto = pagerdutyValidation(connectDto); addPagerdutyTask(connectDto.getId()); return connectDto; }
            case JIRA -> { connectDto = jiraValidation(connectDto); addJiraTask(connectDto); return connectDto; }
            case ZENDESK -> { connectDto = zendeskValidation(connectDto); addZendeskTask(connectDto); return connectDto; }
            case SLACK -> { connectDto = slackValidation(connectDto); addSlackTask(connectDto); return connectDto; }
            // case GITHUB -> { return add(connectDto); }
            default -> throw new InvalidDataException(ErrorCode.INVALID_SERVICE_TYPE, responseMessage.getErrorMessage(ErrorCode.INVALID_SERVICE_TYPE, serviceType.name()));
        }
    }

    public ConnectDto get(ServiceType serviceType) {
        var connect = connectRepository.findOne(connectSpecifications.findByServiceType(serviceType));
        if (connect.isEmpty()) throw new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND, serviceType.name()));
        return connect.get().toDto(connect.get());
    }

    public Optional<Connect> getModel(ServiceType serviceType, Long clientId) {
        return connectRepository.findByServiceTypeAndClientId(serviceType, clientId);
    }

    private void addMetricsTasks() {
        var taskDto = taskService.getByType(ServiceType.METRICS, TaskType.INCIDENT_METRICS);
        if (ObjectUtils.isEmpty(taskDto)) {
            taskService.add(TaskDto.builder().serviceType(ServiceType.METRICS).taskType(TaskType.INCIDENT_METRICS).build());
        }
        taskDto = taskService.getByType(ServiceType.METRICS, TaskType.TICKET_METRICS);
        if (ObjectUtils.isEmpty(taskDto)) {
            taskService.add(TaskDto.builder().serviceType(ServiceType.METRICS).taskType(TaskType.TICKET_METRICS).build());
        }
    }

    public ConnectDto get(Long id) {
        var connect = getModel(id);
        return connect.toDto(connect);
    }

    public Connect getModel(Long id) {
        return connectRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.CONNECT)));
    }

    public List<ConnectDto> getAll() {
        var connects = connectRepository.findAll();
        return !ObjectUtils.isEmpty(connects) ? connects.stream().map(connects.get(0)::toDto).collect(Collectors.toList()) : List.of();
    }

    public ConnectDto update(ConnectDto connectDto) {
        get(connectDto.getId());
        var serviceType = connectDto.getServiceType();
        switch (serviceType) {
            case PAGER_DUTY -> { return pagerdutyValidation(connectDto); }
            case JIRA -> { return jiraValidation(connectDto); }
            case GITHUB -> { 
                    var connect = connectRepository.save(connectDto.toDomin(connectDto)); 
                    return connect.toDto(connect); 
                }
            default -> throw new InvalidDataException(ErrorCode.INVALID_ISSUE_TYPE, "Invalid Type");
        }
    }

    public ServiceType[] getAllServiceType() {
        return ServiceType.values();
    }

    public String addRepoOrganization(Long id, String repoOrganization) {
        var connect = getModel(id);
        connect.setRepoOrganization(repoOrganization);
        connectRepository.save(connect);
        return Status.SUCCESS.name();
    }

    public Map<String, Long> checkConnect() {
        Map<String, Long> checkConnections = new HashMap<>();
        var connectMap = connectRepository.findAll().stream().collect(Collectors.toMap(Connect::getServiceType, Function.identity()));
        for (ServiceType type : getAllServiceType()) {
            if (ObjectUtils.isEmpty(connectMap.get(type))) {
                checkConnections.put(type.name(), null);
            } else {
                checkConnections.put(type.name(), connectMap.get(type).getId());
            }
        }
        return checkConnections;
    }

    public ConnectDto add(ConnectDto connectDto) {
        var connect = connectDto.toDomin(connectDto);
        connectRepository.save(connect);
        return connect.toDto(connect);
    }

    // token validation method for given PagerDuty connect details
    private ConnectDto pagerdutyValidation(ConnectDto connectDto) {
        Map<String, String> httpHeaders = new HashMap<>();
        httpHeaders.put(HttpHeaders.ACCEPT, Constants.PAGER_DUTY_ACCEPT);
        httpHeaders.put(HttpHeaders.CONTENT_TYPE, Constants.PAGER_DUTY_CONTENT_TYPE);
        httpHeaders.put(HttpHeaders.AUTHORIZATION, AuthType.TOKEN.getKey().concat(connectDto.getAuthToken()));
        app2AppService.httpGet(listIncidentUrl, app2AppService.setHeaders(httpHeaders, null), String.class);
        log.info("Token validation for {} of client id '{}' is success", ServiceType.PAGER_DUTY, SecurityUtil.getClientId());
        httpHeaders.remove(HttpHeaders.AUTHORIZATION);
        connectDto.setAuthType(AuthType.TOKEN);
        connectDto.setHeaders(new JSONObject(httpHeaders).toJSONString());
        return add(connectDto);
    }

    private void addPagerdutyTask(Long connectId) {
        taskService.add(TaskDto.builder().taskType(TaskType.INCIDENTS).serviceType(ServiceType.PAGER_DUTY).connectId(connectId).url(listIncidentUrl).build());
        taskService.add(TaskDto.builder().taskType(TaskType.SERVICES).serviceType(ServiceType.PAGER_DUTY).connectId(connectId).url(listServiceUrl).build());
        taskService.add(TaskDto.builder().taskType(TaskType.INCIDENT_METRICS).serviceType(ServiceType.PAGER_DUTY).connectId(connectId).url(listIncidentMetricsUrl).build());
        taskService.add(TaskDto.builder().taskType(TaskType.INCIDENT_LOG_ENTRY).serviceType(ServiceType.PAGER_DUTY).connectId(connectId).url(listLogEntryUrl).build());
    }

    // token validation method for given Jira connect details
    private ConnectDto jiraValidation(ConnectDto connectDto) {
        var url = connectDto.getDomain().concat(jiraGetProjectUri).concat(connectDto.getProjectKey());
        String encoder = Base64.getEncoder().encodeToString(connectDto.getUserEmail().concat(":").concat(connectDto.getAuthToken()).getBytes());
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, AuthType.BASIC.getKey().concat(" ").concat(encoder));
        app2AppService.httpGet(url, app2AppService.setHeaders(headers, null), String.class);
        log.info("Token validation for {} of client id '{}' is success", ServiceType.JIRA, SecurityUtil.getClientId());
        connectDto.setAuthType(AuthType.BASIC);
        return add(connectDto);
    }

    // create task to create ticket in Jira software.
    private void addJiraTask(ConnectDto connectDto) {
        var url = connectDto.getDomain().concat(jiraCreateTicketUri);
        taskService.add(TaskDto.builder().taskType(TaskType.CREATE_TICKET).serviceType(ServiceType.JIRA).connectId(connectDto.getId()).url(url).build());
        url = connectDto.getDomain().concat(jiraGetTicketsUri);
        taskService.add(TaskDto.builder().taskType(TaskType.GET_TICKETS).serviceType(ServiceType.JIRA).connectId(connectDto.getId()).url(url).build());
    }

    // token validation method for given Zendesk connect details
    private ConnectDto zendeskValidation(ConnectDto connectDto) {
        var zendeskUrl = connectDto.getDomain().concat(zendeskTicketCountUri);
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, Constants.ACCEPT);
        if (connectDto.getAuthType().equals(AuthType.BASIC)) {
            String encoder = Base64.getEncoder().encodeToString(connectDto.getUserEmail().concat("/token:").concat(connectDto.getAuthToken()).getBytes());
            headers.put(HttpHeaders.AUTHORIZATION, connectDto.getAuthType().getKey().concat(" ").concat(encoder));
        }
        if (connectDto.getAuthType().equals(AuthType.BEARER)) {
            headers.put(HttpHeaders.AUTHORIZATION, connectDto.getAuthType().getKey().concat(" ").concat(connectDto.getAuthToken()));
        }
        app2AppService.httpGet(zendeskUrl, app2AppService.setHeaders(headers, null), String.class);
        log.info("Token validation for {} of client id '{}' is success", ServiceType.ZENDESK, SecurityUtil.getClientId());
        headers.put(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE);
        headers.remove(HttpHeaders.AUTHORIZATION);
        connectDto.setHeaders(new JSONObject(headers).toString());
        return add(connectDto);
    }

    // create task to create ticket in Zendesk software.
    private void addZendeskTask(ConnectDto connectDto) {
        var url = connectDto.getDomain().concat(zendeskCreateTicketUri);
        taskService.add(TaskDto.builder().taskType(TaskType.CREATE_TICKET).serviceType(ServiceType.ZENDESK).connectId(connectDto.getId()).url(url).build());
        url = connectDto.getDomain().concat(zendeskGetTicketUri);
        taskService.add(TaskDto.builder().taskType(TaskType.GET_TICKETS).serviceType(ServiceType.ZENDESK).connectId(connectDto.getId()).url(url).build());
    }
    // Credentials validation for slack
    private ConnectDto slackValidation(ConnectDto connectDto) {
        var headers = Map.of(HttpHeaders.AUTHORIZATION, connectDto.getAuthType().getKey().concat(" ").concat(connectDto.getAuthToken()));
        Map<String, String> message = new HashMap<>();
        message.put("channel", connectDto.getChannelId());
        message.put("text", "Hello :wave: \nOpsbeach Credential Validation");
        // var responseObject = JsonUtil.convertJsonIntoObject(app2AppService.sendRequest(slackPostMessageUrl, HttpMethod.POST, app2AppService.setHeaders(headers, message)), JsonNode.class);
        var responseObject = app2AppService.httpPost(slackPostMessageUrl, app2AppService.setHeaders(headers, message), JsonNode.class);
        if (!responseObject.get("ok").asBoolean()) slackException(responseObject.get("error").asText(), connectDto);
        log.info("Token validation for {} of client id '{}' is success", ServiceType.SLACK, SecurityUtil.getClientId());
        return add(connectDto);
    }

    private void slackException(String message, ConnectDto connectDto) {
        if (message.equals("invalid_auth")) {
            throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_INVALID, responseMessage.getErrorMessage(ErrorCode.ACCESS_TOKEN_INVALID, connectDto.getAuthToken()));
        }
        if (message.equals("channel_not_found")) {
            throw new InvalidDataException(ErrorCode.CHANNEL_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.CHANNEL_NOT_FOUND, connectDto.getChannelId()));
        }
        if (message.equals("not_in_channel")) {
            throw new InvalidDataException(ErrorCode.NOT_IN_CHANNEL, responseMessage.getErrorMessage(ErrorCode.NOT_IN_CHANNEL, "Not a Member in the channel id - ".concat(connectDto.getChannelId())));
        }
        throw new InvalidDataException(ErrorCode.SOMETHING_WENT_WRONG, responseMessage.getErrorMessage(ErrorCode.SOMETHING_WENT_WRONG, message));
    }

    private void addSlackTask(ConnectDto connectDto) {
        taskService.add(TaskDto.builder().taskType(TaskType.POST_MESSAGE).serviceType(ServiceType.SLACK).connectId(connectDto.getId()).url(slackPostMessageUrl).build());
    }
}
