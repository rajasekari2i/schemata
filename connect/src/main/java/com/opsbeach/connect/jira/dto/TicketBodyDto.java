package com.opsbeach.connect.jira.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketBodyDto {
    
    private String summary;

    @JsonProperty("parent_key")
    private String parentKey;

    @JsonProperty("project_key")
    private String projectKey;

    @JsonProperty("issue_type")
    private String issueType;

    @JsonProperty("repoter_email")
    private String repoterEmail;

    @JsonProperty("assignee_email")
    private String assigneeEmail;

    private String description;

    @JsonProperty("epic_name")
    private String epicName;
}
