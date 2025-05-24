package com.opsbeach.user.controller;

import com.opsbeach.sharedlib.dto.JwtDto;
import com.opsbeach.sharedlib.response.SuccessResponse;
import com.opsbeach.user.service.JwtService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(tags = "Jwt Controller")
@RestController
@RequestMapping("v1/jwt")
public class JwtController {

    private final JwtService jwtService;

    public JwtController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Transactional
    @PostMapping()
    @ApiOperation(value = "Add Jwt Token")
    public SuccessResponse<JwtDto> add(@RequestBody @Valid JwtDto jwtDto) {
        return SuccessResponse.statusCreated(jwtService.add(jwtDto));
    }
    
    @GetMapping("/check")
    public SuccessResponse<Boolean> checkTokenValid() {
        return SuccessResponse.statusOk(Boolean.TRUE);
    }
    
    @Transactional
    @GetMapping(path = "/{authenticationToken}/access")
    @ApiOperation(value = "Get Jwt Token by access token")
    public SuccessResponse<JwtDto> getByAccessToken(@PathVariable String authenticationToken) {
        return SuccessResponse.statusOk(jwtService.getByAccessToken(authenticationToken));
    }

    @Transactional
    @GetMapping(path = "/{refreshToken}/refresh")
    @ApiOperation(value = "Get Jwt Token by refresh token")
    public SuccessResponse<JwtDto> getByRefreshToken(@PathVariable String refreshToken) {
        return SuccessResponse.statusOk(jwtService.getByRefreshToken(refreshToken));
    }

    @Transactional
    @DeleteMapping("/{accessToken}")
    @ApiOperation(value = "Delete Jwt Token by access")
    public SuccessResponse<Boolean> delete(@PathVariable(name = "accessToken") String accessToken) {
        return SuccessResponse.statusOk(jwtService.delete(accessToken));
    }
}
