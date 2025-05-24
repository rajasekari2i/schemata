package com.opsbeach.connect.core.utils;

import org.springframework.scheduling.TriggerContext;

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.task.entity.Task;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskUtils {
    
    static final Map<ServiceType, List<TaskType>> serviceTaskTypes = new HashMap<>();
    // Add serviceType with its respective taskType of task which are needed to run when application starts.
    static {
        serviceTaskTypes.put(ServiceType.GITHUB, List.of(TaskType.RENEWAL_ACCESS_TOKEN));
    }

    public static Date findNextExecutionTime(TriggerContext triggerContext, Task task) {
        var instant = triggerContext.lastCompletion();
        Optional<Date> lastCompletionTime = instant == null ? Optional.empty() : Optional.of(Date.from(instant));
        Instant nextExecutionTime = nextExecutionTime(lastCompletionTime, task);
        return Date.from(nextExecutionTime);
    }

    private static Instant nextExecutionTime(Optional<Date> lastCompletionTime, Task task) {
        if (lastCompletionTime.isPresent()) {
            return lastCompletionTime.get().toInstant().plusMillis(task.getExecutionInterval()); // set range based on user info
        }
        if (serviceTaskTypes.containsKey(task.getServiceType()) && serviceTaskTypes.get(task.getServiceType()).contains(task.getTaskType())) {
            return new Date().toInstant();
        }
        return new Date().toInstant().plusMillis(task.getExecutionInterval());
    }
}
