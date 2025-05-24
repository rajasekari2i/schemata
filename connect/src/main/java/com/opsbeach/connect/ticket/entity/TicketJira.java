package com.opsbeach.connect.ticket.entity;

import java.time.LocalDateTime;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.ticket.dto.TicketJiraDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Entity
@Table(name = "ticket_jira")
@AllArgsConstructor
@NoArgsConstructor
public class TicketJira extends BaseModel {

    @Column(name = "ticket_id")
    private String ticketId;

    private String key;
    
    private String priority;

    private String status;

    private String summary;

    private String description;

    @Column(name = "issue_type")
    private String issueType;

    private String statuscategorychangedate;

    @Column(name = "ticket_created")
    private LocalDateTime ticketCreated;

    @Column(name = "ticket_updated")
    private LocalDateTime ticketUpdated;

    private String assignee;

    private String creater;
    
    private String reporter;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "project_key")
    private String projectKey;

    @Column(name = "project_name")
    private String projectName;

    public TicketJiraDto toDto(TicketJira ticketJira) {
        return TicketJiraDto.builder().id(ticketJira.getId())
                                        .clientId(ticketJira.getClientId())
                                        .key(ticketJira.getKey())
                                        .ticketId(ticketJira.getTicketId())
                                        .priority(ticketJira.getPriority())
                                        .status(ticketJira.getStatus())
                                        .summary(ticketJira.getSummary())
                                        .description(ticketJira.getDescription())
                                        .issueType(ticketJira.getIssueType())
                                        .statuscategorychangedate(ticketJira.getStatuscategorychangedate())
                                        .ticketCreated(ticketJira.getTicketCreated())
                                        .ticketUpdated(ticketJira.getTicketUpdated())
                                        .assignee(ticketJira.getAssignee())
                                        .creater(ticketJira.getCreater())
                                        .reporter(ticketJira.getReporter())
                                        .projectId(ticketJira.getProjectId())
                                        .projectKey(ticketJira.getProjectKey())
                                        .projectName(ticketJira.getProjectKey())
                                        .build();
    }
}
