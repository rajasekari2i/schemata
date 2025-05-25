package com.opsbeach.connect.scheduler;

import com.opsbeach.connect.task.entity.Task;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.utils.DateUtil;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.github.service.GitHubService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;

@ExtendWith(SpringExtension.class)
public class SchedulerTaskServiceTest {
    @Mock
    TaskScheduler scheduler;

    SchedulerTaskService schedulerTaskService;

    @Mock
    ScheduledFuture<?> t;

    @Mock
    ResponseMessage responseMessage;

    @Mock
    GitHubService gitHubService;

    @BeforeEach
    public void setup() {
        schedulerTaskService = new SchedulerTaskService(scheduler, responseMessage, gitHubService);
    }

    @Test
    public void shouldReturnEmptyTask() {
        List<Long> taskIds = schedulerTaskService.getTaskIds();
        assertThat(taskIds).isEmpty();
    }

    @Test
    public void shouldReturnTask() {
        var date = DateUtil.convertLocalDateTimeToDate(LocalDateTime.now().plusSeconds(5));        
        Trigger trigger = triggerContext -> date.toInstant();
        Task task = Task.builder().id(1L).build();
        Mockito.doReturn(t).when(scheduler).schedule(
                any(Runnable.class), any(Trigger.class));
        schedulerTaskService.addTaskToScheduler(task, trigger);
        List<Long> taskIds = schedulerTaskService.getTaskIds();
        assertThat(taskIds).isNotEmpty();
        assertEquals(1L, taskIds.stream().findFirst().get());
    }

    @Test
    public void sendRequestTest() {
        assertThrows(InvalidDataException.class, () -> schedulerTaskService.sendRequest(ServiceType.FRESH_DESK, null, null));

        schedulerTaskService.sendRequest(ServiceType.GITHUB, TaskType.RENEWAL_ACCESS_TOKEN, 1L);
        assertThrows(InvalidDataException.class, () -> schedulerTaskService.sendRequest(ServiceType.GITHUB, TaskType.CREATE_TICKET, 1L));
    }

    @Test
    public void shouldRemoveTask() {
        Trigger trigger = triggerContext -> new Date().toInstant();
        Task task = Task.builder().id(1L).build();
        Mockito.doReturn(t).when(scheduler).schedule(
                any(Runnable.class), any(Trigger.class));
        schedulerTaskService.addTaskToScheduler(task, trigger);
        List<Long> taskIds = schedulerTaskService.getTaskIds();
        assertThat(taskIds).isNotEmpty();
        schedulerTaskService.removeTaskFromScheduler(task.getId());
        List<Long> removedIds = schedulerTaskService.getTaskIds();
        assertThat(removedIds).isEmpty();

        schedulerTaskService.removeTaskFromScheduler(2L);
    }


}
