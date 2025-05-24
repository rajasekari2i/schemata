package com.opsbeach.connect.ticket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opsbeach.connect.askob.message.service.AskobMessageService;
import com.opsbeach.connect.askob.routing.service.AskobRoutingService;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.jira.service.JiraService;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.ticket.dto.TicketActionDto;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.entity.Ticket;
import com.opsbeach.connect.ticket.enums.ActionType;
import com.opsbeach.connect.ticket.repository.TicketAuditRepository;
import com.opsbeach.connect.ticket.repository.TicketRepository;
import com.opsbeach.connect.zendesk.service.ZendeskService;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yaml.snakeyaml.Yaml;

public class TicketServiceTest {

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AskobMessageService askobMessageService;

    @Mock
    private AskobRoutingService askobRoutingService;

    @Mock
    private JiraService jiraService;

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private ResponseMessage responseMessage;

    @Spy
    private IdSpecifications<Ticket> ticketSpecifications;

    @Mock
    private TicketActionService ticketActionService;

    @Mock
    private TicketAuditRepository ticketAuditRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private ZendeskService zendeskService;

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
        userEmail = data.get("user-email");
    }

    private TicketDto getDto() {
        return TicketDto.builder().id(1L).askobMessageId(1L).assignedTo(userEmail.toString()).title("test").description("test").build();
    }

    @Test
    public void addTestPass() {
        var ticketDto = getDto();
        var ticket = ticketDto.toDomin(ticketDto);
        mockApplicationUser();
            when(ticketRepository.save(ArgumentMatchers.<Ticket>any())).thenReturn(ticket);
        var response = ticketService.add(ticketDto);
        assertEquals(ticket.getAssignedTo(), response.getAssignedTo());
        assertEquals(ticket.getTitle(), response.getTitle());
        ticketDto.setCanonicalId("9221b3ac-6b33-4d2e-b6a1-612ae64258bf");
        response = ticketService.add(ticketDto);
        assertEquals(ticket.getAssignedTo(), response.getAssignedTo());
        assertEquals(ticket.getTitle(), response.getTitle());
    }

    private void mockApplicationUser() {
        UserDto userDto = mock(UserDto.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDto);
    }

    @Test
    public void addTestFail() {
        var ticketDto = getDto();
        var taskDto = TaskDto.builder().serviceType(ServiceType.SLACK).build();
            when(taskService.getByTypes(List.of(ServiceType.JIRA, ServiceType.ZENDESK), TaskType.CREATE_TICKET)).thenReturn(taskDto);
        assertThrows(InvalidDataException.class, () -> { ticketService.action(ActionType.CREATE_TICKET, ticketDto); });
    }

    @Test
    public void actionTest() {
        var ticketDto = getDto();
        var taskDto = TaskDto.builder().serviceType(ServiceType.JIRA).build();
            when(taskService.getByTypes(List.of(ServiceType.JIRA, ServiceType.ZENDESK), TaskType.CREATE_TICKET)).thenReturn(taskDto);
            var node = JsonNodeFactory.instance.objectNode().put("key", "OP-404");
            when(jiraService.addTicket(ticketDto, taskDto)).thenReturn(node);
            when(ticketActionService.add(anyString(), anyLong(), any())).thenReturn(TicketActionDto.builder().referenceId("404").build());
        var response = ticketService.action(ActionType.CREATE_TICKET, ticketDto);
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.CREATED, "Ticket created with reference Id: "+"404"), response);

        taskDto = TaskDto.builder().serviceType(ServiceType.ZENDESK).build();
            when(taskService.getByTypes(List.of(ServiceType.JIRA, ServiceType.ZENDESK), TaskType.CREATE_TICKET)).thenReturn(taskDto);
            ObjectNode payload = JsonNodeFactory.instance.objectNode();
                ObjectNode ticket = payload.putObject("ticket");
                    ticket.put("id", "1");
            when(zendeskService.createTicket(ticketDto, taskDto)).thenReturn(payload);
        response = ticketService.action(ActionType.CREATE_TICKET, ticketDto);
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.CREATED, "Ticket created with reference Id: "+"1"), response);
    }
    
    @Test
    public void getTestFail() {
        assertThrows(RecordNotFoundException.class, () -> { ticketService.get(2L); });
    }

    @Test
    public void getAllTest() {
        assertEquals(0, ticketService.getAll().size());
        var ticketDtos = List.of(getDto());
        var tickets = List.of(ticketDtos.get(0).toDomin(ticketDtos.get(0)));
            when(ticketRepository.findAll()).thenReturn(tickets);
        var response = ticketService.getAll();
        assertEquals(1, response.size());
        assertEquals(ticketDtos.get(0).getAssignedTo(), response.get(0).getAssignedTo());
    }

    @Test
    public void updateTest() {
        var ticketDto = getDto();
        var ticket = ticketDto.toDomin(ticketDto);
            when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
            when(ticketRepository.save(ArgumentMatchers.<Ticket>any())).thenReturn(ticket);
        var response = ticketService.update(ticketDto);
        assertEquals(ticket.getAssignedTo(), response.getAssignedTo());
        assertEquals(ticket.getTitle(), response.getTitle());
    }

    @Test
    public void getByPageTest() {
        List<Ticket> tickets = List.of();
        var pageTicket = new PageImpl<>(tickets);
        when(ticketRepository.findAll(ArgumentMatchers.<Specification<Ticket>>any(), ArgumentMatchers.<Pageable>any())).thenReturn(pageTicket);
        var response = ticketService.getByPage(2, 5, 9L);
        assertEquals(0, response.size());
        tickets = List.of(Ticket.builder().id(1L).build());
        pageTicket = new PageImpl<>(tickets);
        when(ticketRepository.findAll(ArgumentMatchers.<Specification<Ticket>>any(), ArgumentMatchers.<Pageable>any())).thenReturn(pageTicket);
        response = ticketService.getByPage(1, 2, 5L);
        assertEquals(tickets.get(0).getId(), response.get(0).getId());
    }

    @Test
    public void getByAfterUpdateAtAndPageTest() {
        List<Ticket> tickets = List.of();
        var pageTicket = new PageImpl<>(tickets);
        when(ticketRepository.findAll(ArgumentMatchers.<Specification<Ticket>>any(), ArgumentMatchers.<Pageable>any())).thenReturn(pageTicket);
        var response = ticketService.getByAfterUpdateAtAndPage(2, 5, null, 9L);
        assertEquals(0, response.size());
        tickets = List.of(Ticket.builder().id(1L).build());
        pageTicket = new PageImpl<>(tickets);
        when(ticketRepository.findAll(ArgumentMatchers.<Specification<Ticket>>any(), ArgumentMatchers.<Pageable>any())).thenReturn(pageTicket);
        response = ticketService.getByAfterUpdateAtAndPage(2, 5, null, 9L);
        assertEquals(tickets.get(0).getId(), response.get(0).getId());
    }
}
