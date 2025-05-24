package com.opsbeach.connect.askob.messagerouting.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.askob.messagerouting.dto.MessageRoutingDto;
import com.opsbeach.connect.askob.messagerouting.service.MessageRoutingService;
import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/askob/message-routing")
@RequiredArgsConstructor
public class MessageRoutingController extends BaseModel {
    
    private final MessageRoutingService messageRoutingService;

    @Transactional
    @PostMapping
    public SuccessResponse<MessageRoutingDto> add(@RequestBody @Valid MessageRoutingDto messageRoutingdDto) {
        return SuccessResponse.statusCreated(messageRoutingService.add(messageRoutingdDto));
    }

    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<MessageRoutingDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(messageRoutingService.get(id));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<MessageRoutingDto>> getAll(@RequestParam(required = false, name = "fromMessageId") Long fromMessageId) {
        return SuccessResponse.statusOk(messageRoutingService.getAll(fromMessageId));
    }

    @Transactional
    @PutMapping
    public SuccessResponse<MessageRoutingDto> update(@RequestBody @Valid MessageRoutingDto messageRoutingdDto) {
        return SuccessResponse.statusOk(messageRoutingService.update(messageRoutingdDto));
    }
}
