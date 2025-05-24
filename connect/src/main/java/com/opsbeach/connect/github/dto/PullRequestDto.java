package com.opsbeach.connect.github.dto;

import com.opsbeach.connect.github.entity.PullRequest;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PullRequestDto {
    
    Long id;
    Long clientId;
    String number;
    Long workflowId;
    Long clientRepoId;
    PullRequest.Status status;
    String sourceBranch;
    String targetBranch;
    String sha;
    String url;
    String errorMessage;
    com.opsbeach.connect.schemata.validate.Status validationStatus;

    public PullRequest toDomain(PullRequestDto pullRequestDto) {
        return PullRequest.builder().id(pullRequestDto.id)
                                    .clientId(pullRequestDto.clientId)
                                    .number(pullRequestDto.number)
                                    .workflowId(pullRequestDto.workflowId)
                                    .clientRepoId(pullRequestDto.clientRepoId)
                                    .status(pullRequestDto.status)
                                    .sourceBranch(pullRequestDto.sourceBranch)
                                    .targetBranch(pullRequestDto.targetBranch)
                                    .validationStatus(pullRequestDto.validationStatus)
                                    .errorMessage(pullRequestDto.errorMessage)
                                    .sha(pullRequestDto.sha)
                                    .url(pullRequestDto.url)
                                    .build();
    }
}
