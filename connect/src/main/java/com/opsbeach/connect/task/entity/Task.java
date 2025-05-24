package com.opsbeach.connect.task.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.task.dto.TaskDto;
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

import java.time.LocalDateTime;

@Entity
@Table
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Task extends BaseModel{
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type")
    private TaskType taskType;
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;
    @Column(name = "connect_id")
    private Long connectId;
    private String url;
    @Column(name = "execution_interval")
    private long executionInterval;
    @Setter
    @Column(name = "last_sync_date")
    private LocalDateTime lastSyncDate;

    public TaskDto toDto(Task task) {
        return TaskDto.builder().id(task.getId())
                .clientId(task.getClientId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .serviceType(task.getServiceType())
                .taskType(task.getTaskType())
                .url(task.getUrl())
                .connectId(task.getConnectId())
                .executionInterval(task.getExecutionInterval())
                .lastSyncDate(task.getLastSyncDate())
                .build();
    }
}