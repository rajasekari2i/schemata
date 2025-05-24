package com.opsbeach.sharedlib.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import com.opsbeach.sharedlib.service.JwtTokenService;
import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.exception.UnAuthorizedException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.utils.DateUtil;
import com.opsbeach.sharedlib.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    private static final String USER = "user";
    private final HMac hMac;
    private final JweLibrary jweLibrary;
    private final JwtTokenService jwtService;
    private final App2AppService app2AppService;
    private final ResponseMessage responseMessage;
    private final ApplicationConfig applicationConfig;
    private final RSAMechanism rsaMechanism;

    public AuthenticationProvider(ResponseMessage responseMessage, JweLibrary jweLibrary, HMac hMac, JwtTokenService jwtService,
                                  App2AppService app2AppService, ApplicationConfig applicationConfig, RSAMechanism rsaMechanism) {
        this.hMac = hMac;
        this.jweLibrary = jweLibrary;
        this.jwtService = jwtService;
        this.app2AppService = app2AppService;
        this.responseMessage = responseMessage;
        this.applicationConfig = applicationConfig;
        this.rsaMechanism = rsaMechanism;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws org.springframework.security.core.AuthenticationException {
        var userDto = (UserDto) userDetails;
        try {
            if (Objects.isNull(userDto)) {
                throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_INVALID, responseMessage.getErrorMessage(ErrorCode.ACCESS_TOKEN_INVALID));
            }
            if (CollectionUtils.isEmpty(userDto.getRoles())) {
                throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_INVALID, responseMessage.getErrorMessage(ErrorCode.ACCESS_TOKEN_INVALID));
            }
            List<GrantedAuthority> authorities = userDto.getRoles().stream().flatMap(role -> role.getPermissions().stream()).collect(Collectors.toList());
            userDto.setAuthorities(authorities);
        } catch (RecordNotFoundException recordNotFound) {
            throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_INVALID, responseMessage.getErrorMessage(ErrorCode.ACCESS_TOKEN_INVALID));
        }
    }

    @Override
    protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws org.springframework.security.core.AuthenticationException {
        var authenticationToken = (AuthenticationToken) usernamePasswordAuthenticationToken;
        String accessToken = (String) authenticationToken.getCredentials();
        try {
            //var jwtDto = (JwtDto) cacheService.get(accessToken, Constants.TOKEN);
            var jwtDto = jwtService.getByAccessToken(accessToken);
            if (Objects.isNull(jwtDto)) {
                throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_USER, responseMessage.getErrorMessage(ErrorCode.UNAUTHORIZED_USER));
            }
            if (!accessToken.equals(jwtDto.getAccessToken())) {
                throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_USER, responseMessage.getErrorMessage(ErrorCode.UNAUTHORIZED_USER));
            }
            var jweDto = jweLibrary.decrypt(accessToken, RSAKeyGen.getStringToKeys(jwtDto.getPublicKey(), jwtDto.getPrivateKey()));
            var client = jweDto.getClient();
            if (jweDto.getIsRefresh().equals(Boolean.TRUE)) {
                throw new UnAuthorizedException(ErrorCode.INVALID_ACCESS_TOKEN, responseMessage.getErrorMessage(ErrorCode.INVALID_ACCESS_TOKEN));
            }
            UserDto userDto = getUser(jweDto.getUsername(), rsaMechanism.encrypt(String.valueOf(client)));
            if (Objects.isNull(userDto)) {
                throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_USER, responseMessage.getErrorMessage(ErrorCode.UNAUTHORIZED_USER));
            }
            if (userDto.getIsDeleted().equals(Boolean.TRUE)) {
                throw new UnAuthorizedException(ErrorCode.INVALID_USER, responseMessage.getErrorMessage(ErrorCode.INVALID_USER));
            }
            if (DateUtil.validateExpiration(jwtDto.getExpireAt()).equals(Boolean.TRUE)) {
                throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_USER, responseMessage.getErrorMessage(ErrorCode.UNAUTHORIZED_USER));
            }
            userDto.setAccessToken(accessToken);
            return userDto;
        } catch (ParseException | JOSEException | JsonProcessingException e) {
            throw new UnAuthorizedException(ErrorCode.INVALID_ACCESS_TOKEN, responseMessage.getErrorMessage(ErrorCode.INVALID_ACCESS_TOKEN));
        }
    }

    /**
     * <p>
     * Returns <code>true</code> if this <Code>AuthenticationProvider</code> supports the indicated
     * <Code>Authentication</code> object.
     * </p>
     *
     * @param authentication - Authentication object.
     * @return <code>true</code> if the implementation can more closely evaluate the <code>Authentication</code> class
     * presented
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /*private UserDto getUser(String username, String clientId) {
        log.info("Fetching the Details of the User [{}]", username);
        var getUserDetailsUrl = CommonUtil.constructStringEmptySeparator(applicationConfig.getUserService().get(Constants.BASE_URL), applicationConfig.getUserService().get(USER_DETAIL_URL), "?searchValue=", username, "&type=", type);
        return app2AppService.httpGet(getUserDetailsUrl, app2AppService.setHeaders(App2AppService.formClientHeader(tenant), null), UserDto.class);
    }*/

    private UserDto getUser(String username, String client) {
        log.info("Fetching the Details of the User [{}]", username);
        String getUserDetailsUrl = StringUtil.constructStringEmptySeparator(getUserServiceBaseUrl(), applicationConfig.getUser().get(USER), "?username=", username);
        return app2AppService.httpGet(getUserDetailsUrl, app2AppService.setHeaders(App2AppService.clientHeader(client), null), UserDto.class);
    }

    private String getUserServiceBaseUrl() {
        return applicationConfig.getUser().get(Constants.BASE_URL);
    }
}
