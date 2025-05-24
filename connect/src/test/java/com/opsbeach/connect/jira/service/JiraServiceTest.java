package com.opsbeach.connect.jira.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.jira.dto.JiraUserDto;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.dto.TicketJiraDto;
import com.opsbeach.connect.ticket.enums.TicketSeverity;
import com.opsbeach.connect.ticket.service.TicketJiraService;
import com.opsbeach.connect.ticket.service.TicketService;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.service.App2AppService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.Yaml;

public class JiraServiceTest {

    @InjectMocks
    private JiraService jiraService;

    @Mock
    private TaskService taskService;

    @Mock
    private App2AppService app2AppService;

    @Mock
    private TicketService ticketService;

    @Mock
    private ResponseMessage responseMessage;

    @Mock
    private ConnectService connectService;

    private Map<String, String> jira = new HashMap<>();

    private JsonNode getTicketsResponse;

    private Object userEmail;

    @Mock
    private TicketJiraService ticketJiraService;

    @Spy
    private ObjectMapper mapper;

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
        Map<String, Map<String, String>> data = yaml.load(inputStream);
        jira = data.get("jira");
        userEmail = data.get("user-email");

        inputStream = new FileInputStream(new File("src/test/resources/jira-response.json"));
        getTicketsResponse = new ObjectMapper().readValue(inputStream, JsonNode.class);
    }

    private TaskDto getTask() {
        return TaskDto.builder().taskType(TaskType.CREATE_TICKET).serviceType(ServiceType.JIRA).clientId(1L)
                                .url(jira.get("domin").concat(jira.get("create-ticket-uri")))
                                .build();
    }

    private TicketDto getTicketDto() {
        return TicketDto.builder().title("title").description("description").severity(TicketSeverity.HIGH).build();
    }

    private HttpEntity<Object> setHeaders(Map<String, String> token, Object requestBody) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        token.forEach(headers::set);
        if (Objects.isNull(requestBody)) return new HttpEntity<>(headers);
        return new HttpEntity<>(requestBody, headers);
    }

    private String uriBuilderString(String email, String dominName) {
        return UriComponentsBuilder.fromHttpUrl(dominName.concat(jira.get("get-user-uri")))
                                   .queryParam("query", email).buildAndExpand().toUriString();
    }

    private List<JiraUserDto> getJiraUserDtoList() {
        return List.of(JiraUserDto.builder().accountId("1").build());
    }
    
    @Test
    public void addTicketTestPass() {
        var taskDto = getTask();
        var connectDto = ConnectDto.builder().authToken(jira.get("token")).domain(jira.get("domin")).authType(AuthType.BASIC).userEmail(userEmail.toString()).build();
            when(connectService.get(taskDto.getId())).thenReturn(connectDto);
        var ticketDto = getTicketDto();
        String encoder = Base64.getEncoder().encodeToString(connectDto.getUserEmail().concat(":").concat(connectDto.getAuthToken()).getBytes());
        var headers = Map.of(HttpHeaders.AUTHORIZATION, "Basic ".concat(encoder));
        String url = UriComponentsBuilder.fromUriString(taskDto.getUrl()).buildAndExpand().toUri().toString();
        var entity = setHeaders(headers, null);

            when(app2AppService.setHeaders(headers, null)).thenReturn(entity);
            when(app2AppService.httpGetEntities(uriBuilderString(connectDto.getUserEmail(), connectDto.getDomain()), entity, JiraUserDto.class)).thenReturn(getJiraUserDtoList());

            var node = JsonNodeFactory.instance.objectNode().put("key", "OP-404");
            when(app2AppService.httpPost(url, null, JsonNode.class)).thenReturn(node);
            ReflectionTestUtils.setField(jiraService, "getUserUri", jira.get("get-user-uri"));

        var response = jiraService.addTicket(ticketDto, taskDto);
        assertEquals("OP-404", response.get("key").asText());
    }

    @Test
    public void getTicketsTest() {
        var taskDto = getTask();
            when(taskService.get(taskDto.getId())).thenReturn(taskDto);
        var connectDto = ConnectDto.builder().authToken(jira.get("token")).domain(jira.get("domin")).authType(AuthType.BASIC).userEmail(userEmail.toString()).build();
            when(connectService.get(taskDto.getId())).thenReturn(connectDto);
            when(app2AppService.httpPost(taskDto.getUrl(), null, JsonNode.class)).thenReturn(getTicketsResponse);
            when(taskService.update(any(TaskDto.class))).thenReturn(taskDto);
        var response = jiraService.getTickets(taskDto.getId());
        assertEquals(taskDto.getTaskType(), response.getTaskType());
            taskDto.setLastSyncDate(LocalDateTime.now());
            when(taskService.get(taskDto.getId())).thenReturn(taskDto);
            when(ticketJiraService.getByKey(anyString(), anyLong())).thenReturn(TicketJiraDto.builder().id(1L).build());
            when(app2AppService.httpPost(taskDto.getUrl(), null, JsonNode.class)).thenReturn(getFiftyResults(), getEmptyResults());
        response = jiraService.getTickets(taskDto.getId());
        assertEquals(taskDto.getTaskType(), response.getTaskType());
    }

    private JsonNode getEmptyResults() {
        var results = JsonNodeFactory.instance.objectNode();
        results.putArray("issues");
        return results;
    }

    private JsonNode getFiftyResults() {
        var tickets = getTicketsResponse.get("issues");
        var results = JsonNodeFactory.instance.objectNode();
        var arryaNode = results.putArray("issues");
        for (int i=0; i<50; i++) {
            arryaNode.add(tickets.get(i%tickets.size()));
        }
        return results;
    }
}
