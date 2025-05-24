package com.opsbeach.sharedlib.security;


import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.UnAuthorizedException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtil {

    public static UserDto getLoggedInUserDetail() {
        var securityContext = SecurityContextHolder.getContext();
        if (Objects.isNull(securityContext) || Objects.isNull(securityContext.getAuthentication()) || Objects.isNull(securityContext.getAuthentication().getPrincipal())) {
            return null;
        }
        if (securityContext.getAuthentication().getPrincipal().equals(Constants.ANONYMOUS_USER)) {
            return null;
        }
        if (Objects.isNull(securityContext.getAuthentication())) {
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ONLY_OWNER);
        }
        return (UserDto) securityContext.getAuthentication().getPrincipal();
    }

    public static UserDto getUserDetails() {
        return getOptionalUserDetails().orElse(null);
    }

    public static Long getClientId() {
        var userDto = getLoggedInUserDetail();
        return Objects.nonNull(userDto) ? userDto.getClientId() : null;
    }

    public static void setClientId(Long clientId) {
        var userDto = getLoggedInUserDetail();
        userDto.setClientId(clientId);
        setAuthenticationContext(userDto);
    }

    public static String getAccessToken() {
        var userDto = getLoggedInUserDetail();
        return Objects.nonNull(userDto) ? userDto.getAccessToken() : null;
    }

    public static Optional<UserDto> getOptionalUserDetails() {
        var securityContext = SecurityContextHolder.getContext();
        var authentication = securityContext.getAuthentication();
        if (Objects.nonNull(authentication)) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDto) {
                return Optional.of((UserDto) (principal));
            }
        }
        return Optional.empty();
    }

    public static String getHashKey() {
        var userDto = getLoggedInUserDetail();
        return Objects.nonNull(userDto) ? userDto.getUsername() : Constants.EMPTY;
    }

    public static void setAuthenticationContext(UserDto userDto) {
        var userContext = new UserDto();
        userContext.setId(userDto.getId());
        userContext.setRoles(userDto.getRoles());
        userContext.setMobile(userDto.getMobile());
        userContext.setClientId(userDto.getClientId());
        userContext.setIsDeleted(userDto.getIsDeleted());
        userContext.setOnboardStatus(userDto.getOnboardStatus());
        userContext.setAccessToken(userDto.getAccessToken());
        var securityContext = SecurityContextHolder.getContext();
        var authentication = BaseAuthentication.getInstance(userContext);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}