package com.opsbeach.connect.task.controller;

import java.util.List;

import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("v1/task")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;

    @Transactional
    @PostMapping
    public SuccessResponse<TaskDto> add(@RequestBody TaskDto taskdDto) {
        return SuccessResponse.statusCreated(taskService.add(taskdDto));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<TaskDto>> getAll() {
        return SuccessResponse.statusOk(taskService.getAll());
    }

    @DeleteMapping("{id}")
    public void removeTaskFromScheduler(@PathVariable("id") Long id) {
        taskService.delete(id);
    }
}
