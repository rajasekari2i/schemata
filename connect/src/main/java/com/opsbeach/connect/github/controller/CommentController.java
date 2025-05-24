package com.opsbeach.connect.github.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.github.dto.CommentDto;
import com.opsbeach.connect.github.service.CommentService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/comment")
public class CommentController {

    private final CommentService commentService;
    
    @PostMapping
    public SuccessResponse<CommentDto> add(@RequestBody CommentDto workflowDto) {
        return SuccessResponse.statusCreated(commentService.add(workflowDto));
    }

    @GetMapping("/{id}")
    public SuccessResponse<CommentDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(commentService.get(id));
    }

    @PutMapping("/{id}")
    public SuccessResponse<CommentDto> updateStatus(@PathVariable("id") Long id, 
                                                    @RequestParam("isResolved") Boolean isResolved) {
        return SuccessResponse.statusOk(commentService.updateIsResolved(id, isResolved));
    }
}
