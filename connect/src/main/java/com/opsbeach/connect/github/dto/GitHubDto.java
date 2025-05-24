package com.opsbeach.connect.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubDto {
    
    private String user;

    private Long connectId; // where credentials of the user get saved.

    // private String[] selectedRepos;

    private Map<String, RepoType> selectedRepos;

    private List<String> repos;

    private String repoOwner;

    private String loginRedirectURL;

    private String privateRepoName;
}
