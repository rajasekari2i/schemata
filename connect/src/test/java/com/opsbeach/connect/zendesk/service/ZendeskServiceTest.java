package com.opsbeach.connect.zendesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.dto.TicketZendeskDto;
import com.opsbeach.connect.ticket.enums.TicketSeverity;
import com.opsbeach.connect.ticket.service.TicketZendeskService;
import com.opsbeach.sharedlib.service.App2AppService;

import net.minidev.json.JSONObject;

public class ZendeskServiceTest {

    @InjectMocks
    private ZendeskService zendeskService;

    @Mock
    private TaskService taskService;

    @Mock
    private ConnectService connectService;

    @Mock
    private App2AppService app2AppService;

    Map<String, String> zendesk = new HashMap<>();

    JsonNode getTicketResponse;

    @Mock
    private TicketZendeskService ticketZendeskService;

    @Spy
    private ObjectMapper mapper;

    private Object userEmail;
    
    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @BeforeEach
    public void init() throws StreamReadException, DatabindException, IOException {
        InputStream inputStream = new FileInputStream(new File("src/test/resources/application-test.yaml"));
        Yaml yaml = new Yaml();
        Map<String, Map<String,String>> data = yaml.load(inputStream);
        zendesk = data.get("zendesk");
        userEmail = data.get("user-email");

        inputStream = new FileInputStream(new File("src/test/resources/zendesk-response.json"));
        getTicketResponse = new ObjectMapper().readValue(inputStream, JsonNode.class);
    }

    private TaskDto getTask() {
        return TaskDto.builder().taskType(TaskType.CREATE_TICKET).serviceType(ServiceType.ZENDESK)
                                .url(zendesk.get("domin").concat(zendesk.get("ticket-count-uri")))
                                .build();
    }

    private ConnectDto createConnect(AuthType authType) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, Constants.ACCEPT);
        headers.put(HttpHeaders.CONTENT_TYPE, Constants.CONTENT_TYPE);
        return ConnectDto.builder().authToken(zendesk.get("token")).headers(new JSONObject(headers).toJSONString()).domain(zendesk.get("domin")).authType(authType).userEmail(userEmail.toString()).build();
    }

    @Test
    public void createTicketTest() {
        var taskDto = getTask();
        var connectDto = createConnect(AuthType.BASIC);
            when(connectService.get(taskDto.getId())).thenReturn(connectDto);
            var node = JsonNodeFactory.instance.objectNode().put("key", "OP-404");
            when(app2AppService.httpPost(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), any())).thenReturn(node);
        var ticketDto = TicketDto.builder().severity(TicketSeverity.LOW).title("Test Zendesk").description("Api test case").build();
        var response = zendeskService.createTicket(ticketDto, taskDto);
        assertEquals("OP-404", response.get("key").asText());
        
        connectDto = createConnect(AuthType.BEARER);
            when(connectService.get(taskDto.getId())).thenReturn(connectDto);
        ticketDto = TicketDto.builder().severity(TicketSeverity.HIGH).title("Test Zendesk").description("Api test case").build();
        response = zendeskService.createTicket(ticketDto, taskDto);
        assertEquals("OP-404", response.get("key").asText());

        ticketDto = TicketDto.builder().severity(TicketSeverity.MEDIUM).title("Test Zendesk").description("Api test case").build();
        response = zendeskService.createTicket(ticketDto, taskDto);
        assertEquals("OP-404", response.get("key").asText());
    }

    @Test
    public void getTicketsTest() {
        var taskDto = getTask();
            when(taskService.get(taskDto.getId())).thenReturn(taskDto);
        var connectDto = createConnect(AuthType.BASIC);
            when(connectService.get(taskDto.getId())).thenReturn(connectDto);
            when(app2AppService.httpGet(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), any())).thenReturn(getTicketResponse);
            when(taskService.update(any(TaskDto.class))).thenReturn(taskDto);
        var response = zendeskService.getTickets(taskDto.getId());
        assertEquals(taskDto.getTaskType(), response.getTaskType());

        taskDto.setLastSyncDate(LocalDateTime.now());
            when(taskService.get(taskDto.getId())).thenReturn(taskDto);
            when(app2AppService.httpGet(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), any())).thenReturn(getFiftyResults(), getEmptyResults());
            when(ticketZendeskService.getByticketId("1", taskDto.getClientId())).thenReturn(TicketZendeskDto.builder().id(1L).build());
        response = zendeskService.getTickets(taskDto.getId());
        assertEquals(taskDto.getTaskType(), response.getTaskType());
        System.out.println(getTicketResponse.toPrettyString());
    }

    private JsonNode getEmptyResults() {
        var results = JsonNodeFactory.instance.objectNode();
        results.putArray("results");
        return results;
    }

    private JsonNode getFiftyResults() {
        var tickets = getTicketResponse.get("results");
        var results = JsonNodeFactory.instance.objectNode();
        var arryaNode = results.putArray("results");
        for (int i=0; i<10; i++) {
            for (int j=0; j<5; j++) {
                arryaNode.add(tickets.get(j));
            }
        }
        return results;
    }
}
