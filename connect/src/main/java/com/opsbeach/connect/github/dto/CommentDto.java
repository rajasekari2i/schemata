package com.opsbeach.connect.github.dto;

import com.opsbeach.connect.github.entity.Comment;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CommentDto {

    Long id;
    Long clientId;
    Long nodeId;
    String comments;
    String type;
    Long pullRequestId;
    Long commentableId;
    boolean isResolved;
    
    public Comment toDomain(CommentDto commentDto) {
        return Comment.builder().id(commentDto.id)
                                .clientId(commentDto.clientId)
                                .nodeId(commentDto.nodeId)
                                .comments(commentDto.comments)
                                .type(commentDto.type)
                                .pullRequestId(commentDto.pullRequestId)
                                .commentableId(commentDto.commentableId)
                                .isResolved(commentDto.isResolved)
                                .build();
    }
}
