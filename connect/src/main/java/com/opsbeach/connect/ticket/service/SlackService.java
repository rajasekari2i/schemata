package com.opsbeach.connect.ticket.service;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opsbeach.connect.askob.message.dto.AskobMessageDto;
import com.opsbeach.connect.askob.message.service.AskobMessageService;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.enums.TicketSeverity;
import com.opsbeach.connect.ticket.enums.TicketStatus;
import com.opsbeach.sharedlib.service.App2AppService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlackService {
    
    private final TicketService ticketService;

    private final App2AppService app2AppService;

    private final AskobMessageService askobMessageService;

    private final TaskService taskService;

    private final ConnectService connectService;

    // method to push ticket created message to slack channel
    // create ticket record in local ticket table
    @Transactional
    public TicketDto addTicketMessage(String title, String ticketCreatedBy, TicketSeverity ticketSeverity, TicketStatus status) {
        var taskDto = taskService.getByType(ServiceType.SLACK, TaskType.POST_MESSAGE);
        var connectDto = connectService.get(taskDto.getConnectId());
        var ticketDto = TicketDto.builder().title(title).ticketCreatedBy(ticketCreatedBy).severity(ticketSeverity).build();
        ticketDto = ticketService.add(ticketDto);
        var headers = Map.of(HttpHeaders.AUTHORIZATION, connectDto.getAuthType().getKey().concat(" ").concat(connectDto.getAuthToken()));
        var attachments = getAttachment(ticketDto.getCanonicalId(), title, ticketSeverity, status.name(), connectDto.getChannelId());
        var messageResponse = app2AppService.httpPost(taskDto.getUrl(), app2AppService.setHeaders(headers, attachments), JsonNode.class);
        var askobMessageDto = AskobMessageDto.builder().messageTs(messageResponse.get("message").get("ts").asText())
                                                    .messageUserId(messageResponse.get("message").get("user").asText())
                                                    .build();
        askobMessageDto = askobMessageService.add(askobMessageDto);
        ticketDto.setAskobMessageId(askobMessageDto.getId());
        ticketService.update(ticketDto);
        return ticketDto;
    }

    // method to generate object for attachment message of slack
    private JsonNode getAttachment(String ticketId, String message, TicketSeverity priority, String status, String channelId) {
        var color = priority.equals(TicketSeverity.HIGH) ? "#fa0525" : priority.equals(TicketSeverity.MEDIUM) ? "#ffff05" : "#1bcc04";
        var circle = priority.equals(TicketSeverity.HIGH) ? ":red_circle:" : priority.equals(TicketSeverity.MEDIUM) ? ":large_yellow_circle:" : ":large_green_circle:";
        var ticketText = "*Details:*\n "+circle+" [Ticket: "+ticketId+"] "+message+"\n *Priority:*\n "+priority+"\n *Created By:*\n Obie\n *Status:*\n "+status+".";
        JsonNodeFactory jnf = JsonNodeFactory.instance;
        ObjectNode payload = jnf.objectNode();
            payload.put("channel", channelId);
            payload.put("text", "Ticket Created");
            ArrayNode attachments = payload.putArray("attachments");
                ObjectNode attachment = attachments.addObject();
                    attachment.put("color", color);
                    ArrayNode blocks = attachment.putArray("blocks");
                        ObjectNode blocks0 = blocks.addObject();
                            blocks0.put("type", "section");
                            ObjectNode text0 = blocks0.putObject("text");
                                text0.put("type", "mrkdwn");
                                text0.put("text", ticketText);
        return payload;
    }
}
