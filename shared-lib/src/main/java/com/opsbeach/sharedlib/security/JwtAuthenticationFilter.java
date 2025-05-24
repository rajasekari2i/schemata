package com.opsbeach.sharedlib.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsbeach.sharedlib.dto.SessionDto;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.ExceptionResponseCreator;
import com.opsbeach.sharedlib.exception.UnAuthorizedException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.service.AuthService;
import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.sharedlib.utils.StringUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * Authentication Filter for Processing JWT Access Token and Authenticating User
 * Other custom Authentication Filters will be before this filter in chain as this covers most end points
 * and will resolve here that if it doesn't have Authorization Header Bearer, no other authorizations are in header.
 *</p>
 */
@Slf4j
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String AUDIT = "audit";
    private static final String X_FORWARDED_FOR = "X-FORWARDED-FOR";
    private final App2AppService app2AppService;
    private final ResponseMessage responseMessage;
    private final ApplicationConfig applicationConfig;
    private final AuthService authService;
    private final ExceptionResponseCreator exceptionResponseCreator;

    public JwtAuthenticationFilter(ExceptionResponseCreator exceptionResponseCreator, ResponseMessage responseMessage, App2AppService app2AppService, ApplicationConfig applicationConfig, AuthService authService) {
        super("/**");
        this.app2AppService = app2AppService;
        this.responseMessage = responseMessage;
        this.applicationConfig = applicationConfig;
        this.authService = authService;
        this.exceptionResponseCreator = exceptionResponseCreator;
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Boolean isAuthenticated = SecurityUtil.getOptionalUserDetails().isPresent();
        if (isAuthenticated.equals(Boolean.TRUE) || StringUtil.isEmpty(httpServletRequest.getHeader(Constants.AUTHORIZATION_HEADER)).equals(Boolean.TRUE)) {
            return Boolean.FALSE;
        }
        List<RequestMatcher> matchers = List.of(new AntPathRequestMatcher(Constants.AUTHORIZED_PATH_PREFIX + "/**"));
        for (RequestMatcher matcher : matchers) {
            if (matcher.matches(httpServletRequest)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws org.springframework.security.core.AuthenticationException {
        String header = httpServletRequest.getHeader(Constants.AUTHORIZATION_HEADER);
        if (!header.startsWith(Constants.PREFIX)) {
            throw new UnAuthorizedException(ErrorCode.UNABLE_VERIFY_ACCESS_TOKEN, responseMessage.getErrorMessage(ErrorCode.UNABLE_VERIFY_ACCESS_TOKEN));
        }
        var accessToken = header.substring(Constants.PREFIX.length());
        var authenticationToken = new AuthenticationToken(null, accessToken);
        var authenticationManager = getAuthenticationManager();
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        log.info("Authenticated using - {}", JwtAuthenticationFilter.class.getSimpleName());
        chain.doFilter(httpServletRequest, httpServletResponse);
        var jweDto = authService.decryptToken(httpServletRequest.getHeader(Constants.AUTHORIZATION_HEADER).substring(Constants.PREFIX.length()));
        addSessionAudit(httpServletRequest, Boolean.TRUE);
        //updateTokenExpiry(jweDto);
    }

    /**
     * <p>
     * Common method to identify the client IP address
     * </p>
     *
     * @param request - HttpServletRequest
     * @return deviceType - Client IP Address.
     */
    public String getClientIp(HttpServletRequest request) {
        String remoteAddress = Constants.EMPTY;
        if (request != null) {
            remoteAddress = request.getHeader(X_FORWARDED_FOR);
            if (StringUtil.isEmpty(remoteAddress).equals(Boolean.TRUE)) {
                remoteAddress = request.getRemoteAddr();
            }
        }
        return remoteAddress;
    }

    private String getModule(String requestUri) {
        String module;
        module = requestUri.substring(requestUri.indexOf(Constants.AUTHORIZED_PATH_PREFIX) + (Constants.AUTHORIZED_PATH_PREFIX + Constants.FORWARD_SLASH).length());
        module = module + Constants.FORWARD_SLASH;
        return module.substring(0, module.indexOf(Constants.FORWARD_SLASH)).replace(Constants.HYPHEN, Constants.EMPTY_SPACE);
    }

    private void addSessionAudit(HttpServletRequest httpServletRequest, Boolean successLogin) {
        var userDto = SecurityUtil.getLoggedInUserDetail();
        var sessionAuditUrl = applicationConfig.getUser().get(Constants.BASE_URL) + applicationConfig.getUser().get(AUDIT);
        var sessionDto = SessionDto.builder()
                .action(httpServletRequest.getMethod())
                .ipAddress(getClientIp(httpServletRequest))
                .uri(httpServletRequest.getRequestURI())
                .userId(userDto.getId())
                .type("USER")
                .module(StringUtil.capitalizeWord(getModule(httpServletRequest.getRequestURI())))
                .successLogin(successLogin).build();
        app2AppService.httpPost(sessionAuditUrl, app2AppService.setHeaders(new HashMap<>(), sessionDto), SessionDto.class);
    }

    /*private void updateTokenExpiry(JweDto jweDto) {
        var sessionAuditUrl = applicationConfig.getUser().get(Constants.BASE_URL) + applicationConfig.getUser().get("expiry-update") + "?userId=" + jweDto.getUserId();
        app2AppService.httpPatch(sessionAuditUrl, app2AppService.setHeaders(App2AppService.formClientHeader(jweDto.getClient()), null), GenericResponseDto.class);
    }*/

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException failed) throws IOException {
        var jweDto = authService.decryptToken(request.getHeader(Constants.AUTHORIZATION_HEADER).substring(Constants.PREFIX.length()));
        //addSessionAudit(request, Boolean.FALSE);
        ResponseEntity<Object> responseEntity;
        if (failed instanceof UnAuthorizedException) {
            UnAuthorizedException gatewayAuthenticationException = (UnAuthorizedException) failed;
            responseEntity = exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.UNAUTHORIZED, gatewayAuthenticationException.getErrorCode(), gatewayAuthenticationException);
        } else {
            responseEntity = exceptionResponseCreator.getExceptionResponseEntity(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED_USER, failed);
        }
        var objectMapper = new ObjectMapper();
        var json = objectMapper.writeValueAsString(responseEntity.getBody());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(json);
    }
}