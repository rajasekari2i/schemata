package com.opsbeach.connect.ticket.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opsbeach.connect.ticket.dto.TicketActionDto;
import com.opsbeach.connect.ticket.service.TicketActionService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/v1/ticket")
@RequiredArgsConstructor
public class TicketActionController {

    private final TicketActionService ticketActionService;
    
    @Transactional
    @GetMapping(value="/action/{id}")
    public SuccessResponse<TicketActionDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(ticketActionService.get(id));
    }

    @Transactional
    @GetMapping("/action")
    public SuccessResponse<List<TicketActionDto>> getAll() {
        return SuccessResponse.statusOk(ticketActionService.getAll());
    }    
}
