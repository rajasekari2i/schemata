package com.opsbeach.user.controller;

import com.opsbeach.sharedlib.dto.GenericResponseDto;
import com.opsbeach.sharedlib.response.SuccessResponse;
import com.opsbeach.user.dto.UserDetailDto;
import com.opsbeach.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * User Registration
 * </p>
 */
@Api(tags = "User Controller")
@RestController
@RequestMapping("v1/user")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Transactional
    @PostMapping
    @ApiOperation(value = "User Register")
    public SuccessResponse<UserDetailDto> registration(@RequestBody @Valid UserDetailDto userDto) {
        return SuccessResponse.statusCreated(userService.add(userDto));
    }

    @Transactional
    @GetMapping
    @ApiOperation(value = "Get All Users")
    public SuccessResponse<UserDetailDto> getUser() {
        return SuccessResponse.statusOk(userService.findAll());
    }

    @Transactional
    @PatchMapping
    @ApiOperation(value = "Update company name")
    public SuccessResponse<GenericResponseDto> registerCompany(@RequestParam("companyname") String companyName) {
        return SuccessResponse.statusOk(userService.registerCompany(companyName));
    }
}
