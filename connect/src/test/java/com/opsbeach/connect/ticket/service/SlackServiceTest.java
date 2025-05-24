package com.opsbeach.connect.ticket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsbeach.connect.askob.message.dto.AskobMessageDto;
import com.opsbeach.connect.askob.message.service.AskobMessageService;
import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.enums.TicketSeverity;
import com.opsbeach.connect.ticket.enums.TicketStatus;
import com.opsbeach.sharedlib.service.App2AppService;

public class SlackServiceTest {
    
    @InjectMocks
    private SlackService slackService;

    @Mock
    private TicketService ticketService;

    @Mock
    private App2AppService app2AppService;

    @Mock
    private AskobMessageService askobMessageService;

    @Mock
    private TaskService taskService;

    @Mock
    private ConnectService connectService;

    private final ObjectMapper mapper = new ObjectMapper();

    private Map<String, String> slack = new HashMap<>();

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
        slack = data.get("slack");
    }

    @Test
    public void addTicketMessageTest() {
        var title = "Slack Service Test Case";
        var ticketCreatedBy = "OpsBeach";
        var canonicalId = "MM1gcjt";
        var taskDto = TaskDto.builder().connectId(1L).url(slack.get("post-message-url")).build();
        var connectDto = ConnectDto.builder().authType(AuthType.BEARER).authToken(slack.get("token")).build();
            when(taskService.getByType(ServiceType.SLACK, TaskType.POST_MESSAGE)).thenReturn(taskDto);
            when(connectService.get(1L)).thenReturn(connectDto);
        var ticketDto = TicketDto.builder().canonicalId(canonicalId).title(title).ticketCreatedBy(ticketCreatedBy).severity(TicketSeverity.HIGH).build();
            when(ticketService.add(any(TicketDto.class))).thenReturn(ticketDto);
        var messageResponse = mapper.valueToTree(Map.of("message", Map.of("ts", 1664756495.160729, "user", "U0381H3LFUN")));
            when(app2AppService.httpPost(anyString(), ArgumentMatchers.<HttpEntity<Object>>any(), eq(JsonNode.class))).thenReturn(messageResponse);
        var askobMessageDto = AskobMessageDto.builder().id(1L).messageTs("1664756495.160729").messageUserId("U0381H3LFUN").build();
            when(askobMessageService.add(any(AskobMessageDto.class))).thenReturn(askobMessageDto);
            when(ticketService.update(any(TicketDto.class))).thenReturn(ticketDto);
        var testResponse = slackService.addTicketMessage(title, ticketCreatedBy, TicketSeverity.HIGH, TicketStatus.NEW);
        assertEquals(canonicalId, testResponse.getCanonicalId());
        testResponse = slackService.addTicketMessage(title, ticketCreatedBy, TicketSeverity.MEDIUM, TicketStatus.NEW);
        assertEquals(title, testResponse.getTitle());
        testResponse = slackService.addTicketMessage(title, ticketCreatedBy, TicketSeverity.LOW, TicketStatus.NEW);
        assertEquals(ticketCreatedBy, testResponse.getTicketCreatedBy());
    }
}
