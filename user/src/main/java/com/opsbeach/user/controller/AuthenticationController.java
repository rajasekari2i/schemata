package com.opsbeach.user.controller;

import com.opsbeach.sharedlib.dto.AuthenticationResponseDto;
import com.opsbeach.sharedlib.dto.GenericResponseDto;
import com.opsbeach.sharedlib.dto.JwtDto;
import com.opsbeach.sharedlib.dto.LoginDto;
import com.opsbeach.sharedlib.dto.RefreshTokenDto;
import com.opsbeach.sharedlib.dto.RegisterClientDto;
import com.opsbeach.sharedlib.dto.RegistrationDto;
import com.opsbeach.sharedlib.dto.SessionDto;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.response.SuccessResponse;
import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.user.dto.ClientDto;
import com.opsbeach.user.service.AuthenticationService;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register/client")
    public SuccessResponse<ClientDto> registerClient(@RequestBody @Valid RegisterClientDto registerClientDto, HttpServletRequest httpServletRequest) {
        return SuccessResponse.statusCreated(authenticationService.registerClient(registerClientDto));
    }

    @PostMapping("/add/client")
    public SuccessResponse<ClientDto> registerClient(@RequestParam("workEmail") String workEmail) {
        return SuccessResponse.statusCreated(authenticationService.registerClient(workEmail));
    }

    @PostMapping("/register/user")
    public SuccessResponse<GenericResponseDto> registerUser(@RequestBody @Valid RegistrationDto registrationDto) {
        return SuccessResponse.statusCreated(authenticationService.registerUser(registrationDto));
    }

    @PostMapping("/otp")
    public SuccessResponse<String> getOtp(@RequestBody @Valid LoginDto.SendOTP sendOTP) {
        return SuccessResponse.statusOk(authenticationService.sendOtp(sendOTP.getUsername()));
    }

    @PostMapping("/login")
    public SuccessResponse<AuthenticationResponseDto> login(@RequestBody @Valid LoginDto loginDto) {
        return SuccessResponse.statusCreated(authenticationService.login(loginDto));
    }

    @GetMapping("/github-token")
    public SuccessResponse<AuthenticationResponseDto> githubAuthentication() {
        return SuccessResponse.statusCreated(authenticationService.githubAuthentication());
    }

    @PatchMapping("/refresh")
    public SuccessResponse<AuthenticationResponseDto> refresh(@RequestBody RefreshTokenDto refreshTokenDto) {
        return SuccessResponse.statusOk(authenticationService.refresh(refreshTokenDto));
    }

    @PostMapping("/logout")
    public SuccessResponse<AuthenticationResponseDto> logout(HttpServletRequest httpServletRequest) {
        return SuccessResponse.statusCreated(authenticationService.logout(httpServletRequest.getHeader(Constants.AUTHORIZATION_HEADER)));
    }

    @Transactional
    @GetMapping("/user")
    @ApiOperation(value = "Get user by username")
    public SuccessResponse<UserDto> findByUsername(@RequestParam("username") String username) {
        return SuccessResponse.statusOk(authenticationService.findByUsername(username));
    }

    @Transactional
    @PostMapping("/jwt")
    @ApiOperation(value = "Add Jwt Token")
    public SuccessResponse<JwtDto> add(@RequestBody @Valid JwtDto jwtDto) {
        return SuccessResponse.statusCreated(authenticationService.addJwt(jwtDto));
    }

    @Transactional
    @PostMapping(path = "/access")
    @ApiOperation(value = "Get Jwt Token by access token")
    public SuccessResponse<JwtDto> getByAccessToken(@RequestBody @Valid JwtDto jwtDto) {
        return SuccessResponse.statusCreated(authenticationService.getByAccessToken(jwtDto.getAccessToken()));
    }

    @Transactional
    @PostMapping(path = "/refresh")
    @ApiOperation(value = "Get Jwt Token by refresh token")
    public SuccessResponse<JwtDto> getByRefreshToken(@RequestBody @Valid JwtDto jwtDto) {
        return SuccessResponse.statusCreated(authenticationService.getByRefreshToken(jwtDto.getRefreshToken()));
    }

    @Transactional
    @DeleteMapping("/jwt")
    @ApiOperation(value = "Delete Jwt Token by access")
    public SuccessResponse<Boolean> deleteToken(@RequestBody @Valid JwtDto jwtDto) {
        return SuccessResponse.statusOk(authenticationService.deleteByAccessToken(jwtDto.getAccessToken()));
    }

    @Transactional
    @PostMapping("/audit")
    @ApiOperation(value = "Add Jwt Token")
    public SuccessResponse<SessionDto> addSession(@RequestBody @Valid SessionDto sessionDto) {
        return SuccessResponse.statusCreated(authenticationService.addSession(sessionDto));
    }
}
