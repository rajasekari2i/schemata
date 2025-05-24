package com.opsbeach.connect.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraUserDto {
    
    private String accountId;

    private String accountType;

    private String displayName;

    private String emailAddress;

    private String self;
}
