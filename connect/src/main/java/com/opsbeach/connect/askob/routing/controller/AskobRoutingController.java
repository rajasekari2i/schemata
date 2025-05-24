package com.opsbeach.connect.askob.routing.controller;

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

import com.opsbeach.connect.askob.routing.dto.AskobRoutingDto;
import com.opsbeach.connect.askob.routing.service.AskobRoutingService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/v1/askob/routing")
@RequiredArgsConstructor
public class AskobRoutingController {
    
    private final AskobRoutingService askobRoutingService;

    @Transactional
    @PostMapping
    public SuccessResponse<AskobRoutingDto> add(@RequestBody @Valid AskobRoutingDto askobRoutingDto) {
        return SuccessResponse.statusCreated(askobRoutingService.add(askobRoutingDto));
    }

    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<AskobRoutingDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(askobRoutingService.get(id));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<AskobRoutingDto>> getAll(@RequestParam(required = false, name = "channelOrigin") String channelOrigin) {
        return SuccessResponse.statusOk(askobRoutingService.getAll(channelOrigin));
    }

    @Transactional
    @PutMapping
    public SuccessResponse<AskobRoutingDto> update(@RequestBody @Valid AskobRoutingDto askobRoutingDto) {
        return SuccessResponse.statusOk(askobRoutingService.update(askobRoutingDto));
    }    
}
