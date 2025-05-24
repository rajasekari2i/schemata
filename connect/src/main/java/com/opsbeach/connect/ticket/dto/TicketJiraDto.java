package com.opsbeach.connect.ticket.dto;

import java.time.LocalDateTime;

import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.ticket.entity.TicketJira;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TicketJiraDto extends BaseDto {

    private String ticketId;

    private String key;
    
    private String priority;

    private String status;

    private String summary;

    private String description;

    private String issueType;

    private String statuscategorychangedate;

    private LocalDateTime ticketCreated;

    private LocalDateTime ticketUpdated;

    private String assignee;

    private String creater;
    
    private String reporter;
    
    private String projectId;
    
    private String projectKey;
    
    private String projectName;
    
    public TicketJira toDomin(TicketJiraDto ticketJiraDto) {
        return TicketJira.builder().id(ticketJiraDto.getId())
                                    .clientId(ticketJiraDto.getClientId())
                                    .key(ticketJiraDto.getKey())
                                    .ticketId(ticketJiraDto.getTicketId())
                                    .priority(ticketJiraDto.getPriority())
                                    .status(ticketJiraDto.getStatus())
                                    .summary(ticketJiraDto.getSummary())
                                    .description(ticketJiraDto.getDescription())
                                    .issueType(ticketJiraDto.getIssueType())
                                    .statuscategorychangedate(ticketJiraDto.getStatuscategorychangedate())
                                    .ticketCreated(ticketJiraDto.getTicketCreated())
                                    .ticketUpdated(ticketJiraDto.getTicketUpdated())
                                    .assignee(ticketJiraDto.getAssignee())
                                    .creater(ticketJiraDto.getCreater())
                                    .reporter(ticketJiraDto.getReporter())
                                    .projectId(ticketJiraDto.getProjectId())
                                    .projectKey(ticketJiraDto.getProjectKey())
                                    .projectName(ticketJiraDto.getProjectKey())
                                    .build();
    }
}
