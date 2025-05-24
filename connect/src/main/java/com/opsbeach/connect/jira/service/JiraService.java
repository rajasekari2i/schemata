package com.opsbeach.connect.jira.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.jira.dto.JiraUserDto;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.dto.TicketJiraDto;
import com.opsbeach.connect.ticket.service.TicketJiraService;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.utils.DateUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JiraService {

    private final TaskService taskService;

    private final App2AppService app2AppService;

    @Value("${jira.get-user-uri}")
    private String getUserUri;

    private final ConnectService connectService;

    private final TicketJiraService ticketJiraService;

    private final ObjectMapper mapper;
    
    private String getJiraUserId(String email, String dominName, Map<String, String> headers) {
        var url = UriComponentsBuilder.fromHttpUrl(dominName.concat(getUserUri)).queryParam("query", email).buildAndExpand().toUriString();
        HttpEntity<Object> entity = app2AppService.setHeaders(headers, null);
        return app2AppService.httpGetEntities(url, entity, JiraUserDto.class).get(0).getAccountId();
    }

    private Map<String, String> getHeadersMap(ConnectDto connectDto) {
        String encoder = Base64.getEncoder().encodeToString(connectDto.getUserEmail().concat(":").concat(connectDto.getAuthToken()).getBytes());
        return Map.of(HttpHeaders.AUTHORIZATION, connectDto.getAuthType().getKey().concat(" ").concat(encoder));
    }

    //create request body for creating ticket in jira
    private ObjectNode createIssueBody(TicketDto ticketDto, String reporterId, String projectKey) {

        JsonNodeFactory jnf = JsonNodeFactory.instance;
        ObjectNode payload = jnf.objectNode();
            ObjectNode fields = payload.putObject("fields");
                fields.put("summary", ticketDto.getTitle());
                ObjectNode project = fields.putObject("project");
                    project.put("key", projectKey);
                ObjectNode issueType = fields.putObject("issuetype");
                    issueType.put("name", Constants.TASK);
                ObjectNode priority = fields.putObject("priority");
                    priority.put("name", ticketDto.getSeverity().getKey());
                ObjectNode reporter = fields.putObject("reporter");
                    reporter.put("accountId", reporterId);
                ObjectNode description = fields.putObject("description");
                    description.put("type", "doc");
                    description.put("version", 1);
                    ArrayNode content = description.putArray("content");
                    ObjectNode content0 = content.addObject();
                        content0.put("type", "paragraph");
                        ArrayNode des = content0.putArray("content");
                        ObjectNode des0 = des.addObject();
                            des0.put("text", ticketDto.getDescription());
                            des0.put("type", "text");
        return payload;
    }

    // method to create ticket on jira software
    public JsonNode addTicket(TicketDto ticketDto, TaskDto taskDto) {
        taskService.taskValidation(taskDto, TaskType.CREATE_TICKET, ServiceType.JIRA);
        var connectDto = connectService.get(taskDto.getConnectId());
        var headers = getHeadersMap(connectDto);
        var reporterId = getJiraUserId(connectDto.getUserEmail(), connectDto.getDomain(), headers);
        var ticketresponse = app2AppService.httpPost(taskDto.getUrl(), app2AppService.setHeaders(headers, createIssueBody(ticketDto, reporterId, connectDto.getProjectKey())), JsonNode.class);
        taskDto.setLastSyncDate(DateUtil.currentDateTimeUTC());
        taskService.update(taskDto);
        return ticketresponse;
    }

    // method to get tickets from jira based on last sync date
    public TaskDto getTickets(Long taskId) {
        var taskDto = taskService.get(taskId);
        taskService.taskValidation(taskDto, TaskType.GET_TICKETS, ServiceType.JIRA);
        var connectDto = connectService.get(taskDto.getConnectId());
        var headers = getHeadersMap(connectDto);
        List<TicketJiraDto> ticketJiraDtos = new ArrayList<>();
        int limit = 0;
        do {
            var ticket = app2AppService.httpPost(taskDto.getUrl(), app2AppService.setHeaders(headers, requestBody(taskDto.getLastSyncDate())), JsonNode.class);
            var tickets = ticket.get("issues");
            StreamSupport.stream(tickets.spliterator(), false).forEach(node -> {
                var ticketJiraDto = ticketJiraService.getByKey(node.get("key").asText(), taskDto.getClientId());
                if (ObjectUtils.isEmpty(taskDto.getLastSyncDate()) || ObjectUtils.isEmpty(ticketJiraDto)) {
                    ticketJiraDtos.add(createTicketJira(node, taskDto.getClientId(), null));
                } else {
                    ticketJiraDtos.add(createTicketJira(node, taskDto.getClientId(), ticketJiraDto));
                }
                taskDto.setLastSyncDate(DateUtil.convertDatetoLocalDateTime(mapper.convertValue(node.get("fields").get("updated").asText(), Date.class)));
            });
            limit = tickets.size();
        } while (limit == 50);
        ticketJiraService.addAll(ticketJiraDtos);
        return taskService.update(taskDto);
    }

    // create request body for fetch tickets from jira
    private ObjectNode requestBody(LocalDateTime updatedDateTime) {
        var jql = "order by updated ASC";
        if (!ObjectUtils.isEmpty(updatedDateTime)) {
            var minutes = ChronoUnit.MINUTES.between(updatedDateTime, LocalDateTime.now());
            jql = "updatedDate > -".concat(Long.toString(minutes)).concat("m ").concat(jql);  // jql = updatedDate > -10m order by updated ASC
        } 
        JsonNodeFactory jnf = JsonNodeFactory.instance;
        ObjectNode payload = jnf.objectNode();
            payload.put("maxResults", 50);
            payload.put("jql", jql);
        return payload;
    }

    // convert JsonNode to ticketJiraDto object
    private TicketJiraDto createTicketJira(JsonNode node, Long clientId, TicketJiraDto ticketJiraDto) {

        var ticketId = node.get("id").asText();
        var key = node.get("key").asText();

        var fields = node.get("fields");

        var assignee = fields.get("assignee").isEmpty() ? null : fields.get("assignee").get("displayName").asText();
        var creater = fields.get("creator").isEmpty() ? null : fields.get("creator").get("displayName").asText();
        var reporter = fields.get("reporter").isEmpty() ? null : fields.get("reporter").get("displayName").asText();
        var priority = fields.get("priority").get("name").asText();
        var status = fields.get("status").get("name").asText();
        var summary = fields.get("summary").asText();
        var description = fields.get("description").isEmpty() ? null : fields.get("description").asText();
        var issueType = fields.get("issuetype").get("name").toString();
        var statuscategorychangedate = fields.get("statuscategorychangedate").asText();
        var created = fields.get("created").asText();
        var updated = fields.get("updated").asText();
        var projectId = fields.get("project").get("id").asText();
        var projectKey = fields.get("project").get("key").asText();
        var projectName = fields.get("project").get("name").asText();


        var id = ticketJiraDto != null ? ticketJiraDto.getId() : null;
        // This process runs in background by schedular so that we need to add client Id
        return TicketJiraDto.builder().id(id).clientId(clientId)
                                             .ticketId(ticketId)
                                             .key(key)
                                             .priority(priority)
                                             .status(status)
                                             .summary(summary)
                                             .description(description)
                                             .issueType(issueType)
                                             .statuscategorychangedate(statuscategorychangedate)
                                             .ticketCreated(DateUtil.convertDatetoLocalDateTime(mapper.convertValue(created, Date.class)))
                                             .ticketUpdated(DateUtil.convertDatetoLocalDateTime(mapper.convertValue(updated, Date.class)))
                                             .assignee(assignee)
                                             .creater(creater)
                                             .reporter(reporter)
                                             .projectId(projectId)
                                             .projectKey(projectKey)
                                             .projectName(projectName)
                                             .build();
    }
}
