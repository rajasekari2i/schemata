package com.opsbeach.connect.github.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.github.dto.CommentDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseModel {
    @Column(name = "node_id")
    private Long nodeId;

    private String comments;

    private String type;
    @Column(name = "pull_request_id")
    private Long pullRequestId;
    @Column(name = "commentable_id")
    private Long commentableId;
    @Column(name = "is_resolved")
    @Setter
    private Boolean isResolved;

    public CommentDto toDto(Comment comment) {
        return CommentDto.builder().id(comment.getId())
                                   .clientId(comment.getClientId())
                                   .nodeId(comment.getNodeId())
                                   .comments(comment.getComments())
                                   .type(comment.getType())
                                   .pullRequestId(comment.getPullRequestId())
                                   .commentableId(comment.getCommentableId())
                                   .isResolved(comment.getIsResolved())
                                   .build();
    }
}
