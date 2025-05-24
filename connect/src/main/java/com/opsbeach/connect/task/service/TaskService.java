package com.opsbeach.connect.task.service;

import java.util.List;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.core.utils.TaskUtils;
import com.opsbeach.connect.scheduler.SchedulerTaskService;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.entity.Task;
import com.opsbeach.connect.task.repository.TaskRepository;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TaskService {

    private final SchedulerTaskService schedulerTaskService;
    
    private final TaskRepository taskRepository;

    private final ResponseMessage responseMessage;

    private final IdSpecifications<Task> taskSpecifications;

    @Value("${scheduler.enabled:false}")
    private boolean schedulerEnabled;

    public TaskService(@Lazy SchedulerTaskService schedulerTaskService, TaskRepository taskRepository,
                       ResponseMessage responseMessage, IdSpecifications<Task> taskSpecifications) {
        this.schedulerTaskService = schedulerTaskService;
        this.taskRepository = taskRepository;
        this.responseMessage = responseMessage;
        this.taskSpecifications = taskSpecifications;
    }

    public TaskDto get(Long id) {
        var task = taskRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.TASK)));
        return task.toDto(task);
    }

    public TaskDto add(TaskDto taskDto) {
        Task taskResponse = taskRepository.save(taskDto.toDomin(taskDto));
        // adding task to scheduler
        if (schedulerEnabled) {
            schedulerTaskService.addTaskToScheduler(taskResponse,
                    triggerContext -> TaskUtils.findNextExecutionTime(triggerContext, taskResponse).toInstant());
        }
        return taskResponse.toDto(taskResponse);
    }

    public TaskDto update(TaskDto taskDto) {
        var task = taskDto.toDomin(taskDto);
        return task.toDto(taskRepository.save(task));
    }

    public List<TaskDto> getAll() {
        Task task = new Task();
        return taskRepository.findAll().stream().map(task::toDto).collect(Collectors.toList());
    }

    public List<Task> getAllForScheduler() {
        return taskRepository.findAll();
    }

    public TaskDto getByType(ServiceType serviceType, TaskType taskType) {
        Specification<Task> baseSpecification = taskSpecifications.findByTaskType(taskType).and(taskSpecifications.findByServiceType(serviceType));
        var task = taskRepository.findOne(baseSpecification).orElse(null);
        return task == null ? null : task.toDto(task);
    }

    public TaskDto getByTypes(List<ServiceType> serviceTypes, TaskType taskType) {
        Specification<Task> baseSpecification = taskSpecifications.findByTaskType(taskType).and(taskSpecifications.findByServiceTypes(serviceTypes));
        var task = taskRepository.findOne(baseSpecification).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND, Constants.TASK.concat(" - ").concat(serviceTypes.toString()))));
        return task.toDto(task);
    }

    public void taskValidation(TaskDto taskDto, TaskType taskType, ServiceType serviceType) {
        if(!taskType.equals(taskDto.getTaskType()) || !serviceType.equals(taskDto.getServiceType())){
            log.info(responseMessage.getErrorMessage(ErrorCode.INVALID_ID, Constants.TASK));
            throw new InvalidDataException(ErrorCode.INVALID_ID, responseMessage.getErrorMessage(ErrorCode.INVALID_ID, Constants.TASK));
        }
    }

    public void delete(Long id) {
        schedulerTaskService.removeTaskFromScheduler(id);
        taskRepository.deleteById(id);
    }
}
