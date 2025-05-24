package com.opsbeach.connect.github.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GithubActionDto {
    String prName;
    String prNumber;
    String repoName;
    String sourceBranch;
    String targetBranch;
    String sha;
    String status;
    String raisedBy;
    String filesChanged;
    String schemaValidationMessage;
}
