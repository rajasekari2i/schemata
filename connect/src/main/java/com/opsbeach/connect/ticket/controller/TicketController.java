package com.opsbeach.connect.ticket.controller;

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

import com.opsbeach.connect.ticket.dto.TicketDto;
import com.opsbeach.connect.ticket.enums.ActionType;
import com.opsbeach.connect.ticket.service.TicketService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/ticket")
@RequiredArgsConstructor
public class TicketController {
    
    private final TicketService ticketService;

    @Transactional
    @PostMapping
    public SuccessResponse<TicketDto> add(@RequestBody @Valid TicketDto ticketDto) {
        return SuccessResponse.statusCreated(ticketService.add(ticketDto));
    }

    @Transactional
    @PostMapping("/action")
    public SuccessResponse<String> action(@RequestParam("action") ActionType actionType, @RequestBody @Valid TicketDto ticketDto) {
        return SuccessResponse.statusOk(ticketService.action(actionType, ticketDto));
    }

    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<TicketDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(ticketService.get(id));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<TicketDto>> getAll() {
        return SuccessResponse.statusOk(ticketService.getAll());
    }

    @Transactional
    @PutMapping
    public SuccessResponse<TicketDto> update(@RequestBody @Valid TicketDto ticketDto) {
        return SuccessResponse.statusOk(ticketService.update(ticketDto));
    }
}
