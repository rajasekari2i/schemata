package com.opsbeach.connect.task.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.scheduler.SchedulerTaskService;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.entity.Task;
import com.opsbeach.connect.task.repository.TaskRepository;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.service.App2AppService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.Trigger;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private ResponseMessage responseMessage;

    @Spy
    private IdSpecifications<Task> taskSpecifications;

    @Mock
    private SchedulerTaskService schedulerTaskService;

    @Mock
    private App2AppService app2AppService;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private List<TaskDto> getTasks() {
        return List.of(TaskDto.builder().id(1L).taskType(TaskType.INCIDENTS).serviceType(ServiceType.PAGER_DUTY)
                                        .connectId(1L).clientId(1L).build());
    }

    @Test
    public void getTest() {
        var taskDto = getTasks().get(0);
        var task = taskDto.toDomin(taskDto);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        var response = taskService.get(1L);
        assertEquals(task.getTaskType(), response.getTaskType());
        
        assertThrows(RecordNotFoundException.class, () -> { taskService.get(2L); });
    }

    @Test
    public void addTest() {
        var taskDto = getTasks().get(0);
        var task = taskDto.toDomin(taskDto);
        ReflectionTestUtils.setField(taskService, "schedulerEnabled", true);
        when(taskRepository.save(ArgumentMatchers.<Task>any())).thenReturn(task);
        var schedulerTaskService = mock(SchedulerTaskService.class);
        doNothing().when(schedulerTaskService).addTaskToScheduler(any(Task.class), any(Trigger.class));
        var response = taskService.add(taskDto);
        assertEquals(task.getTaskType(), response.getTaskType());
    }

    @Test
    public void updateTest() {
        var taskDto = getTasks().get(0);
        var task = taskDto.toDomin(taskDto);
        when(taskRepository.save(ArgumentMatchers.<Task>any())).thenReturn(task);
        var response = taskService.update(taskDto);
        assertEquals(task.getTaskType(), response.getTaskType());
    }

    @Test
    public void getAllTest() {
        var taskDtos = getTasks();
        var tasks = taskDtos.stream().map(taskDtos.get(0)::toDomin).collect(Collectors.toList());
        when(taskRepository.findAll()).thenReturn(tasks);
        var response = taskService.getAll();
        assertEquals(tasks.size(), response.size());
        assertEquals(tasks.get(0).getConnectId(), response.get(0).getConnectId());
    }

    @Test
    public void getAllForSchedulerTest() {
        var taskDtos = getTasks();
        var tasks = taskDtos.stream().map(taskDtos.get(0)::toDomin).collect(Collectors.toList());
        when(taskRepository.findAll()).thenReturn(tasks);
        var response = taskService.getAllForScheduler();
        assertEquals(tasks.size(), response.size());
        assertEquals(tasks.get(0).getConnectId(), response.get(0).getConnectId());
    }

    @Test
    public void getByTypePass() {
        var taskDto = getTasks().get(0);
        var task = taskDto.toDomin(taskDto);
        when(taskRepository.findOne(ArgumentMatchers.<Specification<Task>>any())).thenReturn(Optional.of(task));
        var response = taskService.getByType(ServiceType.JIRA, TaskType.CREATE_TICKET);
        assertEquals(task.getTaskType(), response.getTaskType());
        when(taskRepository.findOne(ArgumentMatchers.<Specification<Task>>any())).thenReturn(Optional.empty());
        assertNull(taskService.getByType(ServiceType.JIRA, TaskType.CREATE_TICKET));
    }

    @Test
    public void getByTypesPass() {
        var taskDto = getTasks().get(0);
        var task = taskDto.toDomin(taskDto);
        when(taskRepository.findOne(ArgumentMatchers.<Specification<Task>>any())).thenReturn(Optional.of(task));
        var response = taskService.getByTypes(List.of(ServiceType.JIRA, ServiceType.ZENDESK), TaskType.CREATE_TICKET);
        assertEquals(task.getTaskType(), response.getTaskType());
    }

    @Test
    public void getByTypesFail() {
        assertThrows(RecordNotFoundException.class, () -> { taskService.getByTypes(List.of(ServiceType.JIRA, ServiceType.ZENDESK), TaskType.CREATE_TICKET); });
    }

    @Test
    public void taskValidationTestPass() {
        var task = getTasks().get(0);
        taskService.taskValidation(task, TaskType.INCIDENTS, ServiceType.PAGER_DUTY);
    }

    @Test
    public void taskValidationTestFail() {
        var task = getTasks().get(0);
        assertThrows(InvalidDataException.class, () -> taskService.taskValidation(task, TaskType.SERVICES, ServiceType.PAGER_DUTY));
    }

    @Test
    public void deleteTest() {
        taskService.delete(1L);
    }
}
