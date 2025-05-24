package com.opsbeach.user.controller;

import com.opsbeach.sharedlib.response.SuccessResponse;
import com.opsbeach.user.dto.ClientDto;
import com.opsbeach.user.service.ClientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * Client Controller
 * </p>
 */
@Api(tags = "Client Controller")
@RestController
@RequestMapping("v1/client")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Transactional
    @PostMapping("/register")
    @ApiOperation(value = "Creates a new client and their tables once a partner sign up")
    public SuccessResponse<ClientDto> add(@RequestBody @Valid ClientDto clientDto) {
        return SuccessResponse.statusCreated(clientService.add(clientDto));
    }

    @Transactional
    @GetMapping(path = "/{id}")
    @ApiOperation(value = "Returns details of client detail by Id")
    public SuccessResponse<ClientDto> getClientById(@PathVariable long id) {
        return SuccessResponse.statusOk(clientService.findById(id));
    }

    @Transactional
    @GetMapping
    @ApiOperation(value = "Returns details of client detail by name")
    public SuccessResponse<String> getClient(@RequestParam("name") String name) {
        return SuccessResponse.statusOk(clientService.getClient(name));
    }

    @Transactional
    @PutMapping(path = "/{id}")
    @ApiOperation(value = "update onboarded status of client by Id")
    public SuccessResponse<ClientDto> updateOnBoardedStatus(@RequestParam("isOnboarded") boolean isOnboarded, @PathVariable long id) {
        return SuccessResponse.statusOk(clientService.updateOnBoardedStatus(id, isOnboarded));
    }
}
