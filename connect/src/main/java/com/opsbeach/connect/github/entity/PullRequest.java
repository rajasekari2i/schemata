package com.opsbeach.connect.github.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.github.dto.PullRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "pull_request")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PullRequest extends BaseModel {
    private String number;
    @Column(name = "workflow_id")
    private Long workflowId;
    @Column(name = "client_repo_id")
    private Long clientRepoId;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "source_branch")
    private String sourceBranch;
    @Column(name = "target_branch")
    private String targetBranch;
    @Column(name = "validation_status")
    private com.opsbeach.connect.schemata.validate.Status validationStatus;
    @Column(name = "error_message")
    private String errorMessage;
    private String sha;
    private String url;
    @Column(name = "issue_comment_id")
    private Long issueCommentId;
    public enum Status {
        OPEN(0), CLOSED(1), MERGED(2), REOPENED(3);
        private final int key;
        Status(int key) {
            this.key = key;
        }
        public int getKey() {
            return this.key;
        }
    }

    public PullRequestDto toDto(PullRequest pullRequest) {
        return PullRequestDto.builder().id(pullRequest.getId())
                                       .clientId(pullRequest.getClientId())
                                       .number(pullRequest.getNumber())
                                       .workflowId(pullRequest.getWorkflowId())
                                       .clientRepoId(pullRequest.getClientRepoId())
                                       .status(pullRequest.getStatus())
                                       .sourceBranch(pullRequest.getSourceBranch())
                                       .targetBranch(pullRequest.getTargetBranch())
                                       .validationStatus(pullRequest.getValidationStatus())
                                       .errorMessage(pullRequest.getErrorMessage())
                                       .sha(pullRequest.getSha())
                                       .url(pullRequest.getUrl())
                                       .build();
    }
}
