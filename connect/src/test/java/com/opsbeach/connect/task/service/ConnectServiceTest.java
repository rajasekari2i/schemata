package com.opsbeach.connect.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.schemata.validate.Status;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.entity.Connect;
import com.opsbeach.connect.task.repository.ConnectRepository;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.exception.UnAuthorizedException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.service.App2AppService;

public class ConnectServiceTest {
    
    @InjectMocks
    private ConnectService connectService;

    @Mock
    private ConnectRepository connectRepository;

    @Mock
    private ResponseMessage responseMessage;

    @Mock
    private App2AppService app2AppService;

    @Mock
    private TaskService taskService;

    @Spy
    private IdSpecifications<Connect> connectSpecifications;

    private Map<String, String> zendesk = new HashMap<>();

    private Map<String, String> jira = new HashMap<>();

    private Map<String, String> pagerduty = new HashMap<>();

    private Map<String, String> slack = new HashMap<>();

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
        zendesk = data.get("zendesk");
        pagerduty = data.get("pager-duty");
        jira = data.get("jira");
        userEmail = data.get("user-email");
        slack = data.get("slack");
    }

    @Test
    public void connectTestPass() {

        // step to Pagerduty credential validation method testcase
        var connectDto = ConnectDto.builder().serviceType(ServiceType.PAGER_DUTY).authToken(pagerduty.get("token")).authType(AuthType.TOKEN).userEmail(userEmail.toString()).build();
        ReflectionTestUtils.setField(connectService, "listIncidentUrl", pagerduty.get("incidents-url"));
        ReflectionTestUtils.setField(connectService, "listServiceUrl", pagerduty.get("services-url"));
        ReflectionTestUtils.setField(connectService, "listIncidentMetricsUrl", pagerduty.get("incident-metrics-url"));
        ReflectionTestUtils.setField(connectService, "listLogEntryUrl", pagerduty.get("log-entry-url"));
        var connect = connectDto.toDomin(connectDto);
            when(connectRepository.save(connect)).thenReturn(connect);
        var response = connectService.connect(connectDto);
        assertEquals(connect.getServiceType(), response.getServiceType());

        // step to Jira credential validation method testcase
        connectDto = ConnectDto.builder().serviceType(ServiceType.JIRA).authToken(jira.get("token")).domain(jira.get("domin")).authType(AuthType.BASIC).projectKey("OP").userEmail(userEmail.toString()).build();
        ReflectionTestUtils.setField(connectService, "jiraGetProjectUri", jira.get("get-project-uri"));
        ReflectionTestUtils.setField(connectService, "jiraCreateTicketUri", jira.get("create-ticket-uri"));
        ReflectionTestUtils.setField(connectService, "jiraGetTicketsUri", jira.get("get-tickets-uri"));
        connect = connectDto.toDomin(connectDto);
            when(connectRepository.save(connect)).thenReturn(connect);
        response = connectService.connect(connectDto);
        assertEquals(connect.getServiceType(), response.getServiceType());

        // step to zendesk credential validation method testcase
        connectDto = ConnectDto.builder().serviceType(ServiceType.ZENDESK).authToken(zendesk.get("token")).domain(zendesk.get("domin")).authType(AuthType.BASIC).userEmail(userEmail.toString()).build();
        ReflectionTestUtils.setField(connectService, "zendeskTicketCountUri", zendesk.get("ticket-count-uri"));
        ReflectionTestUtils.setField(connectService, "zendeskCreateTicketUri", zendesk.get("create-ticket-uri"));
        ReflectionTestUtils.setField(connectService, "zendeskGetTicketUri", zendesk.get("get-tickets-uri"));
        connect = connectDto.toDomin(connectDto);
            when(connectRepository.save(connect)).thenReturn(connect);
        response = connectService.connect(connectDto);
        assertEquals(connect.getServiceType(), response.getServiceType());

        // step to zendesk credential validation method testcase FOR BEARER AUTH TYPE
        connectDto = ConnectDto.builder().serviceType(ServiceType.ZENDESK).authToken(zendesk.get("token")).domain(zendesk.get("domin")).authType(AuthType.BEARER).userEmail(userEmail.toString()).build();
        connect = connectDto.toDomin(connectDto);
            when(connectRepository.save(connect)).thenReturn(connect);
        response = connectService.connect(connectDto);
        assertEquals(connect.getServiceType(), response.getServiceType());

            when(taskService.getByType(ServiceType.METRICS, TaskType.INCIDENT_METRICS)).thenReturn(TaskDto.builder().build());
            when(taskService.getByType(ServiceType.METRICS, TaskType.TICKET_METRICS)).thenReturn(TaskDto.builder().build());

        // step to zendesk credential validation method testcase
        connectDto = ConnectDto.builder().serviceType(ServiceType.SLACK).authToken(slack.get("token")).authType(AuthType.BEARER).channelId(slack.get("channelId")).build();
        ReflectionTestUtils.setField(connectService, "slackPostMessageUrl", slack.get("post-message-url"));
        var responseMessage = JsonNodeFactory.instance.objectNode().put("ok", true);
            when(app2AppService.httpPost(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), eq(JsonNode.class))).thenReturn(responseMessage);
        connect = connectDto.toDomin(connectDto);
            when(connectRepository.save(connect)).thenReturn(connect);
        response = connectService.connect(connectDto);
        assertEquals(connect.getServiceType(), response.getServiceType());
    }

    @Test
    public void addSlackTestFail() {
        var connectDto = ConnectDto.builder().serviceType(ServiceType.SLACK).authToken(slack.get("token")).authType(AuthType.BEARER).channelId(slack.get("channel-id")).build();
        ReflectionTestUtils.setField(connectService, "slackPostMessageUrl", slack.get("post-message-url"));
        
        var responseMessage = JsonNodeFactory.instance.objectNode().put("ok", true).put("error", "invalid_auth");
            when(app2AppService.httpPost(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), eq(JsonNode.class))).thenReturn(responseMessage);
            when(connectRepository.save(any(Connect.class))).thenReturn(connectDto.toDomin(connectDto));
        var response = connectService.connect(connectDto);
        assertEquals(connectDto.getServiceType(), response.getServiceType());

        responseMessage = JsonNodeFactory.instance.objectNode().put("ok", false).put("error", "invalid_auth");
            when(app2AppService.httpPost(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), eq(JsonNode.class))).thenReturn(responseMessage);
        assertThrows(UnAuthorizedException.class, () -> { connectService.connect(connectDto); });

        responseMessage.put("error", "channel_not_found");
            when(app2AppService.httpPost(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), eq(JsonNode.class))).thenReturn(responseMessage);
        assertThrows(InvalidDataException.class, () -> { connectService.connect(connectDto); });

        responseMessage.put("error", "not_in_channel");
            when(app2AppService.httpPost(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), eq(JsonNode.class))).thenReturn(responseMessage);
        assertThrows(InvalidDataException.class, () -> { connectService.connect(connectDto); });

        responseMessage.put("error", "something_went_wrong");
            when(app2AppService.httpPost(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), eq(JsonNode.class))).thenReturn(responseMessage);
        assertThrows(InvalidDataException.class, () -> { connectService.connect(connectDto); });
    
    }

    @Test
    public void addConnectTestFail() {
        var connectDto = ConnectDto.builder().serviceType(ServiceType.FRESH_DESK).build();
        assertThrows(InvalidDataException.class, () -> { connectService.connect(connectDto); });
    }

    @Test
    public void getTest() {
        assertThrows(RecordNotFoundException.class, () -> connectService.get(ServiceType.GITHUB));

        var connect = Connect.builder().serviceType(ServiceType.FRESH_DESK).build();
            when(connectRepository.findOne(ArgumentMatchers.<Specification<Connect>>any())).thenReturn(Optional.of(connect));
        var response = connectService.get(ServiceType.FRESH_DESK);
        assertEquals(ServiceType.FRESH_DESK, response.getServiceType());
    }

    @Test
    public void getModelTest() {
        var response = connectService.getModel(ServiceType.GITHUB, 1L);
        assertTrue(response.isEmpty());
    }

    @Test
    public void updateTestPass() {
        var connectDto = ConnectDto.builder().id(1L).serviceType(ServiceType.PAGER_DUTY).authToken("12345").authType(AuthType.TOKEN).userEmail(userEmail.toString()).build();
        var connect = connectDto.toDomin(connectDto);
            when(connectRepository.findById(1L)).thenReturn(Optional.of(connect));
        ReflectionTestUtils.setField(connectService, "listIncidentUrl", pagerduty.get("incidents-url"));
        ReflectionTestUtils.setField(connectService, "listServiceUrl", pagerduty.get("services-url"));
        ReflectionTestUtils.setField(connectService, "listIncidentMetricsUrl", pagerduty.get("incident-metrics-url"));
        ReflectionTestUtils.setField(connectService, "listLogEntryUrl", pagerduty.get("log-entry-url"));
            when(connectRepository.save(connect)).thenReturn(connect);
        var response = connectService.update(connectDto);
        assertEquals(connect.getServiceType(), response.getServiceType());
        connectDto = ConnectDto.builder().id(1L).serviceType(ServiceType.JIRA).authToken(jira.get("token")).domain(jira.get("domin")).projectKey("OP").authType(AuthType.BASIC).userEmail(userEmail.toString()).build();
        ReflectionTestUtils.setField(connectService, "jiraGetProjectUri", jira.get("get-project-uri"));
        connect = connectDto.toDomin(connectDto);
            when(connectRepository.save(connect)).thenReturn(connect);
        response = connectService.update(connectDto);
        assertEquals(connect.getServiceType(), response.getServiceType());

        connectDto = ConnectDto.builder().id(1L).serviceType(ServiceType.GITHUB).build();
        connect = connectDto.toDomin(connectDto);
            when(connectRepository.save(any())).thenReturn(connect);
        response = connectService.update(connectDto);
        assertEquals(connect.getServiceType(), response.getServiceType());
    }

    @Test
    public void updateTestFail() {
        var connectDto = ConnectDto.builder().id(1L).serviceType(ServiceType.ZOHO).authToken("12345").authType(AuthType.TOKEN).userEmail(userEmail.toString()).build();
        var connect = connectDto.toDomin(connectDto);
            when(connectRepository.findById(1L)).thenReturn(Optional.of(connect));
        assertThrows(InvalidDataException.class, () -> { connectService.update(connectDto); });
        var connectDto1 = ConnectDto.builder().id(2L).build();
        assertThrows(RecordNotFoundException.class, () -> { connectService.update(connectDto1); });
    }

    @Test
    public void getAllTest() {
        when(connectRepository.findAll()).thenReturn(List.of(Connect.builder().id(1L).build()));
        var response = connectService.getAll();
        assertEquals(1, response.size());
        when(connectRepository.findAll()).thenReturn(List.of());
        response = connectService.getAll();
        assertEquals(0, response.size());
    }

    @Test
    public void getAllServiceTypeTest() {
        var response = connectService.getAllServiceType();
        assertEquals(ServiceType.values().length, response.length);
    }

    @Test
    public void checkConnectByServiceTypeTest() {
        var connect = Connect.builder().id(1L).serviceType(ServiceType.ZOHO).authToken("12345").authType(AuthType.TOKEN).userEmail(userEmail.toString()).build();
            when(connectRepository.findAll()).thenReturn(List.of(connect));
        var response = connectService.checkConnect();
        assertEquals(connect.getId(), response.get(ServiceType.ZOHO.name()));
        assertNull(response.get(ServiceType.GITHUB.name()));
    }

    @Test
    public void addRepoOrganizationTest() {
        var connectDto = ConnectDto.builder().id(1L).serviceType(ServiceType.PAGER_DUTY).authToken("12345").authType(AuthType.TOKEN).userEmail(userEmail.toString()).build();
        var connect = connectDto.toDomin(connectDto);
            when(connectRepository.findById(1L)).thenReturn(Optional.of(connect));
            when(connectRepository.save(any(Connect.class))).thenReturn(connect);
        var response = connectService.addRepoOrganization(1L, "opsbeach");
        assertEquals(response, Status.SUCCESS.name());
    }
}
