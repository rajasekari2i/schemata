package com.opsbeach.connect.zendesk.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.dto.TicketZendeskDto;
import com.opsbeach.connect.ticket.enums.TicketSeverity;
import com.opsbeach.connect.ticket.service.TicketZendeskService;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.utils.DateUtil;
import com.opsbeach.sharedlib.utils.JsonUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ZendeskService {
    
    private final TaskService taskService;

    private final ConnectService connectService;

    private final App2AppService app2AppService;

    private final TicketZendeskService ticketZendeskService;

    private final ObjectMapper mapper;

    private Map<String, String> getHeadersMap(ConnectDto connectDto) {
        Map<String, String> headers = new HashMap<>();
        headers.putAll(JsonUtil.convertJsonToMap(connectDto.getHeaders()));
        if (connectDto.getAuthType().equals(AuthType.BASIC)) {
            String encoder = Base64.getEncoder().encodeToString(connectDto.getUserEmail().concat("/token:").concat(connectDto.getAuthToken()).getBytes());
            headers.put(HttpHeaders.AUTHORIZATION, connectDto.getAuthType().getKey().concat(" ").concat(encoder));
        }
        if (connectDto.getAuthType().equals(AuthType.BEARER)) {
            headers.put(HttpHeaders.AUTHORIZATION, connectDto.getAuthType().getKey().concat(" ").concat(connectDto.getAuthToken()));
        }
        return headers;
    }

    // create ticket in ZenDesk software
    public JsonNode createTicket(TicketDto ticketDto, TaskDto taskDto) {
        taskService.taskValidation(taskDto, TaskType.CREATE_TICKET, ServiceType.ZENDESK);
        var connectDto = connectService.get(taskDto.getConnectId());
        var headers = getHeadersMap(connectDto);
        var ticketresponse = app2AppService.httpPost(taskDto.getUrl(), app2AppService.setHeaders(headers, createIssueBody(ticketDto)), JsonNode.class);
        taskDto.setLastSyncDate(DateUtil.currentDateTime());
        taskService.update(taskDto);
        return ticketresponse;
    }

    // build request Body for create ticket in ZenDesk
    private ObjectNode createIssueBody(TicketDto ticketDto) {
        var priority = ticketDto.getSeverity().equals(TicketSeverity.HIGH) ? "high" : ticketDto.getSeverity().equals(TicketSeverity.MEDIUM) ? "normal" : "low";
        JsonNodeFactory jnf = JsonNodeFactory.instance;
        ObjectNode payload = jnf.objectNode();
            ObjectNode ticket = payload.putObject("ticket");
                ObjectNode comment = ticket.putObject("comment");
                    comment.put("body", ticketDto.getDescription());
                ticket.put("priority", priority);
                ticket.put("subject", ticketDto.getTitle());
        return payload;
    }

    // build url for get tickets from zendesk
    private String getTicketUrlBuilder(String url, LocalDateTime updatedDateTime, int page) {
        var query = "type:ticket order_by:updated_at sort:asc"; 
        if (updatedDateTime != null) {
            var minutes = ChronoUnit.MINUTES.between(updatedDateTime, LocalDateTime.now());
            query = query.concat(" updated>").concat(Long.toString(minutes)).concat("minutes");
        }
        return UriComponentsBuilder.fromHttpUrl(url)
                                   .queryParam("query", query)
                                   .queryParam("per_page", 50)
                                   .queryParam("page", page)
                                   .buildAndExpand().toUriString();
    }

    // method to get tickets from zendesk based on last sync date
    public TaskDto getTickets(Long taskId) {
        var taskDto = taskService.get(taskId);
        taskService.taskValidation(taskDto, TaskType.GET_TICKETS, ServiceType.ZENDESK);
        var connectDto = connectService.get(taskDto.getConnectId());
        var headers = getHeadersMap(connectDto);
        List<TicketZendeskDto> ticketZendeskDtos = new ArrayList<>();
        int limit = 0;
        int page = 1;
        var url = getTicketUrlBuilder(taskDto.getUrl(), taskDto.getLastSyncDate(), page);
        do {
            var ticketResponse = app2AppService.httpGet(url, app2AppService.setHeaders(headers, null), JsonNode.class);
            var tickets = ticketResponse.get("results");
            StreamSupport.stream(tickets.spliterator(), false).forEach(node -> {
                var ticketZendeskDto = ticketZendeskService.getByticketId(node.get("id").asText(), taskDto.getClientId());
                if (ObjectUtils.isEmpty(taskDto.getLastSyncDate()) || ObjectUtils.isEmpty(ticketZendeskDto)) {
                    ticketZendeskDtos.add(createTicketZendeskDto(node, taskDto.getClientId(), null));
                } else {
                    ticketZendeskDtos.add(createTicketZendeskDto(node, taskDto.getClientId(), ticketZendeskDto));
                }
            });
            url = url.replace("&page=".concat(Integer.toString(page)), "&page=".concat(Integer.toString(++page)));
            limit = tickets.size();
            if(!tickets.isEmpty()) {
                taskDto.setLastSyncDate(DateUtil.convertDatetoLocalDateTime(mapper.convertValue(tickets.get(limit - 1).get("updated_at").asText(), Date.class)));
            }
        } while (limit == 50);
        ticketZendeskService.addAll(ticketZendeskDtos);
        return taskService.update(taskDto);
    }

        // convert JsonNode to TicketZendeskDto object.
    private TicketZendeskDto createTicketZendeskDto(JsonNode node, Long clientId, TicketZendeskDto ticketZendeskDto) {
        var id = ticketZendeskDto != null ? ticketZendeskDto.getId() : null;
        return TicketZendeskDto.builder().id(id)
                                           .clientId(clientId)
                                           .ticketId(node.get("id").asText())
                                           .priority(node.get("priority").asText())
                                           .status(node.get("status").asText())
                                           .summary(node.get("subject").asText())
                                           .description(node.get("description").asText())
                                           .type(node.get("type").asText())
                                           .ticketCreated(DateUtil.convertDatetoLocalDateTime(mapper.convertValue(node.get("created_at").asText(), Date.class)))
                                           .ticketUpdated(DateUtil.convertDatetoLocalDateTime(mapper.convertValue(node.get("updated_at").asText(), Date.class)))
                                           .requesterId(node.get("requester_id").asText())
                                           .submitterId(node.get("submitter_id").asText())
                                           .assigneeId(node.get("assignee_id").asText())
                                           .organizationId(node.get("organization_id").asText())
                                           .groupId(node.get("group_id").asText())
                                           .isPublic(node.get("is_public").asBoolean())
                                           .hasIncidents(node.get("has_incidents").asBoolean())
                                           .dueAt(node.get("due_at").isNull() ? null : DateUtil.convertDatetoLocalDateTime(mapper.convertValue(node.get("due_at").asText(), Date.class)))
                                           .build();
    }
}
