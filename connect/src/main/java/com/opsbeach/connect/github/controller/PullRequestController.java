package com.opsbeach.connect.github.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.github.dto.DashboardDto;
import com.opsbeach.connect.github.dto.PullRequestDto;
import com.opsbeach.connect.github.entity.PullRequest.Status;
import com.opsbeach.connect.github.service.PullRequestService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pull-request")
public class PullRequestController {
    
    private final PullRequestService pullRequestService;

    @GetMapping("/{id}")
    public SuccessResponse<PullRequestDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(pullRequestService.get(id));
    }

    @PutMapping("/{id}")
    public SuccessResponse<PullRequestDto> updateStatus(@PathVariable("id") Long id,
                                                        @RequestParam("status") Status status) {
        return SuccessResponse.statusOk(pullRequestService.updateStatus(id, status));
    }

    @GetMapping
    public SuccessResponse<List<PullRequestDto>> getAll(Pageable pageable) {
        return SuccessResponse.statusOk(pullRequestService.getAll(pageable));
    }

    @GetMapping("/dashboard")
    public SuccessResponse<DashboardDto> getDashboardMetrics() {
        return SuccessResponse.statusOk(pullRequestService.getDashboardMetrics());
    }
}
