package com.opsbeach.connect.github.controller;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.github.dto.EventAuditDto;
import com.opsbeach.connect.github.service.EventAuditService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/event-audit")
public class EventAuditController {
    
    private final EventAuditService eventAuditService;

    @GetMapping("{id}")
    public SuccessResponse<EventAuditDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(eventAuditService.get(id));
    }

    @Transactional
    @PostMapping("/process")
    public SuccessResponse<Boolean> processEventAudit(@RequestParam("eventAuditId") Long id) {
        return SuccessResponse.statusOk(eventAuditService.processEventAudit(id));
    }

    @GetMapping("/status")
    public SuccessResponse<List<EventAuditDto>> getInitialLoadingStatus() {
        return SuccessResponse.statusOk(eventAuditService.getInitialLoadStatus());
    }
}
