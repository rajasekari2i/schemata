package com.opsbeach.connect.scheduler;

import com.opsbeach.connect.task.entity.Task;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.github.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class SchedulerTaskService {

    // Task Scheduler
    private final TaskScheduler scheduler;

    private final ResponseMessage responseMessage;

    private final GitHubService gitHubService;

    // A map for keeping scheduled tasks
    private final Map<Long, ScheduledFuture<?>> jobsMap = new HashMap<>();

    // Schedule Task to be executed on time period
    public void addTaskToScheduler(Task task, Trigger runningDate) {
        ScheduledFuture<?> scheduledTask = scheduler.schedule(() ->
                sendRequest(task.getServiceType(), task.getTaskType(), task.getId()), runningDate);
        jobsMap.put(task.getId(), scheduledTask);
    }

    public void sendRequest(ServiceType serviceType, TaskType taskType, Long taskId) {
        switch (serviceType) {
            case GITHUB: sendRequestGithub(taskType, taskId); break;
            default: throw new InvalidDataException(ErrorCode.INVALID_SERVICE_TYPE, responseMessage.getErrorMessage(ErrorCode.INVALID_SERVICE_TYPE, serviceType.name()));
        }
    }

    private void sendRequestGithub(TaskType taskType, Long taskId) {
        switch (taskType) {
            case RENEWAL_ACCESS_TOKEN : gitHubService.generateNewToken(taskId); break;
            default: throw new InvalidDataException(ErrorCode.INVALID_TASK_TYPE, responseMessage.getErrorMessage(ErrorCode.INVALID_TASK_TYPE, taskType.name()));
        }
    }

    public List<Long> getTaskIds() {
        return new ArrayList<>(jobsMap.keySet());
    }

    // Remove scheduled task
    public void removeTaskFromScheduler(Long id) {
        ScheduledFuture<?> scheduledTask = jobsMap.get(id);
        if (Objects.nonNull(scheduledTask)) {
            scheduledTask.cancel(true);
            jobsMap.remove(id);
        }
    }
}
