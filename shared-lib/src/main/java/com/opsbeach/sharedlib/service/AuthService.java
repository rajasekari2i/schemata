package com.opsbeach.sharedlib.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import com.opsbeach.sharedlib.dto.JweDto;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.JweLibrary;
import com.opsbeach.sharedlib.security.RSAKeyGen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Slf4j
@Service
public class AuthService {

    private static final String CLIENT = "client";
    private static final String USER_CHECK_URL = "user-check";
    private static final String USER_DETAIL_URL = "user-detail";
    private static final String SESSION_UPDATE = "session-update";
    private static final String USER_LOGIN = "user-login";
    private static final String CLIENT_REGISTER = "client-register";
    private static final String USER_REGISTER = "user-register";
    private static final String USER = "user";
    private static final String JWT_ADD = "jwt-add";

    private final JweLibrary jweLibrary;
    private final JwtTokenService jwtService;
    private final ResponseMessage responseMessage;

    public AuthService(ResponseMessage responseMessage, JweLibrary jweLibrary,
                       JwtTokenService jwtService) {
        this.jweLibrary = jweLibrary;
        this.responseMessage = responseMessage;
        this.jwtService = jwtService;
    }

    public JweDto decryptToken(String authToken) {
        log.info("Decrypting the token");
        try {
            //var jwtTokenDto = (JwtDto) cacheService.get(authToken, Constants.TOKEN);
            var jwtTokenDto = jwtService.getByAccessToken(authToken);
            return jweLibrary.decrypt(authToken, RSAKeyGen.getStringToKeys(jwtTokenDto.getPublicKey(), jwtTokenDto.getPrivateKey()));
        } catch (JOSEException | JsonProcessingException | ParseException jsonProcessingException) {
            throw new InvalidDataException(ErrorCode.INVALID_JSON_PARSE, responseMessage.getErrorMessage(ErrorCode.INVALID_JSON_PARSE));
        }
    }

}
