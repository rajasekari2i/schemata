package com.opsbeach.connect.askob.message.controller;

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

import com.opsbeach.connect.askob.message.dto.AskobMessageDto;
import com.opsbeach.connect.askob.message.service.AskobMessageService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/askob/message")
@RequiredArgsConstructor
public class AskobMessageController {

    private final AskobMessageService askobMessageService;

    @Transactional
    @PostMapping
    public SuccessResponse<AskobMessageDto> add(@RequestBody @Valid AskobMessageDto askobMessageDto) {
        return SuccessResponse.statusCreated(askobMessageService.add(askobMessageDto));
    }

    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<AskobMessageDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(askobMessageService.get(id));
    }

    @Transactional
    @GetMapping("/ts")
    public SuccessResponse<AskobMessageDto> get(@RequestParam("messageTs") String messageTs) {
        return SuccessResponse.statusOk(askobMessageService.getByMessageTs(messageTs));
    }

    @Transactional
    @PutMapping
    public SuccessResponse<AskobMessageDto> update(@RequestBody @Valid AskobMessageDto askobMessageDto) {
        return SuccessResponse.statusOk(askobMessageService.update(askobMessageDto));
    }    
}
