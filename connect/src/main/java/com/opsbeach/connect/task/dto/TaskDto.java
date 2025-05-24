package com.opsbeach.connect.task.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.task.entity.Task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto extends BaseDto {

    @JsonProperty("task_type")
    private TaskType taskType;

    @JsonProperty("service_type")
    private ServiceType serviceType;

    private Long connectId;
    
    private String url;
    
    @Setter
    @Default
    private long executionInterval = 3600000;

    @Setter
    @JsonProperty("last_sync_date")
    private LocalDateTime lastSyncDate;    

    public Task toDomin(TaskDto taskDto) {
        return Task.builder().id(taskDto.getId())
                             .clientId(taskDto.getClientId())
                             .createdAt(taskDto.getCreatedAt())
                             .updatedAt(taskDto.getUpdatedAt())
                             .serviceType(taskDto.getServiceType())
                             .taskType(taskDto.getTaskType())
                             .connectId(taskDto.getConnectId())
                             .url(taskDto.getUrl())
                             .executionInterval(taskDto.getExecutionInterval())
                             .lastSyncDate(taskDto.getLastSyncDate())
                             .build();
    }
}
