package com.opsbeach.connect.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.Trigger;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.task.entity.Task;
import com.opsbeach.connect.task.service.TaskService;

public class SyncSchedulerTest {
    
    @InjectMocks
    private SyncScheduler syncScheduler;

    @Mock
    private TaskService taskService;

    @Mock
    private SchedulerTaskService schedulerTaskService;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void configureTasksTest() {
        syncScheduler.configureTasks(null);
        List<Task> tasks = List.of(Task.builder().id(1L).build());
        when(taskService.getAllForScheduler()).thenReturn(tasks);
        ReflectionTestUtils.setField(syncScheduler, "schedulerEnabled", true);
        var schedulerTaskService = mock(SchedulerTaskService.class);
        doNothing().when(schedulerTaskService).addTaskToScheduler(any(Task.class), any(Trigger.class));
        syncScheduler.configureTasks(null);
    }
}
