package com.opsbeach.connect.scheduler;


import com.opsbeach.connect.task.entity.Task;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.connect.core.utils.TaskUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.List;

@RequiredArgsConstructor
public class SyncScheduler implements SchedulingConfigurer {
    @Autowired
    private  SchedulerTaskService schedulerTaskService;

    @Autowired
    private  TaskService taskService;

    @Value("${scheduler.enabled:false}")
    private boolean schedulerEnabled;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (schedulerEnabled) {
            List<Task> tasks = getTasks();
            tasks.forEach(task -> schedulerTaskService.addTaskToScheduler(task,
                    triggerContext -> TaskUtils.findNextExecutionTime(triggerContext, task).toInstant()));
        }
    }

    private List<Task> getTasks() {
        return taskService.getAllForScheduler();
    }

}
