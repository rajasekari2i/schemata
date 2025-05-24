package com.opsbeach.connect.scheduler;

import com.opsbeach.connect.task.entity.Task;
import com.opsbeach.connect.zendesk.service.ZendeskService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.github.service.GitHubService;
import com.opsbeach.connect.jira.service.JiraService;
import com.opsbeach.connect.metrics.service.MetricsService;
import com.opsbeach.connect.pagerduty.service.PagerDutyProcessor;
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

    private final PagerDutyProcessor pagerDutyService;

    private final ResponseMessage responseMessage;

    private final JiraService jiraService;

    private final ZendeskService zendeskService;

    private final MetricsService metricsService;

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
            case PAGER_DUTY: sendRequestPagerDuty(taskType, taskId); break;
            case JIRA: sendRequestJira(taskType, taskId); break;
            case ZENDESK: sendRequestZendesk(taskType, taskId); break;
            case METRICS: sendRequestMetrics(taskType, taskId); break;
            case GITHUB: sendRequestGithub(taskType, taskId); break;
            default: throw new InvalidDataException(ErrorCode.INVALID_SERVICE_TYPE, responseMessage.getErrorMessage(ErrorCode.INVALID_SERVICE_TYPE, serviceType.name()));
        }
    }

    private void sendRequestJira(TaskType taskType, Long taskId) {
        switch (taskType) {
            case GET_TICKETS : jiraService.getTickets(taskId); break;
            default: throw new InvalidDataException(ErrorCode.INVALID_TASK_TYPE, responseMessage.getErrorMessage(ErrorCode.INVALID_TASK_TYPE, taskType.name()));
        }
    }

    private void sendRequestZendesk(TaskType taskType, Long taskId) {
        switch (taskType) {
            case GET_TICKETS : zendeskService.getTickets(taskId); break;
            default: throw new InvalidDataException(ErrorCode.INVALID_TASK_TYPE, responseMessage.getErrorMessage(ErrorCode.INVALID_TASK_TYPE, taskType.name()));
        }
    }

    private void sendRequestGithub(TaskType taskType, Long taskId) {
        switch (taskType) {
            case RENEWAL_ACCESS_TOKEN : gitHubService.generateNewToken(taskId); break;
            default: throw new InvalidDataException(ErrorCode.INVALID_TASK_TYPE, responseMessage.getErrorMessage(ErrorCode.INVALID_TASK_TYPE, taskType.name()));
        }
    }

    private void sendRequestMetrics(TaskType taskType, Long taskId) {
        switch (taskType) {
            case TICKET_METRICS : metricsService.ticketMetricsComputation(taskId); break;
            case INCIDENT_METRICS : metricsService.incidentMetricsComputation(taskId); break;
            default: throw new InvalidDataException(ErrorCode.INVALID_TASK_TYPE, responseMessage.getErrorMessage(ErrorCode.INVALID_TASK_TYPE, taskType.name()));
        }
    }

    private void sendRequestPagerDuty(TaskType taskType, Long taskId) {
        
        switch (taskType) {
            case INCIDENTS : pagerDutyService.addIncidents(taskId); break;
            case SERVICES : pagerDutyService.addServices(taskId); break;
            case INCIDENT_METRICS : pagerDutyService.addIncidentMetrics(taskId); break;
            case INCIDENT_LOG_ENTRY: pagerDutyService.addLogEntry(taskId); break;
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
