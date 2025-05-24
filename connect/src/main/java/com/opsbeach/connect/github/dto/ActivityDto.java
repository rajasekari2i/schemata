package com.opsbeach.connect.github.dto;

import com.opsbeach.connect.github.entity.Activity;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ActivityDto {

    Long id;
    Long clientId;
    Long workflowId;
    Activity.Type type;
    Long sourceNodeId;
    Long targetNodeId;

    public Activity toDomain(ActivityDto activityDto) {

        return Activity.builder().id(activityDto.id)
                                 .clientId(activityDto.clientId)
                                 .workflowId(activityDto.workflowId)
                                 .type(activityDto.type)
                                 .sourceNodeId(activityDto.sourceNodeId)
                                 .targetNodeId(activityDto.targetNodeId)
                                 .build();
    }
}
