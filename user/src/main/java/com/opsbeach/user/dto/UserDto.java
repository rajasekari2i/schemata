package com.opsbeach.user.dto;

import com.opsbeach.sharedlib.dto.RoleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private long id;
    private String userType;
    private String email;
    private String username;
    private String mobile;
    private Collection<GrantedAuthority> authorities;
    private Boolean isDeleted;
    private transient List<RoleDto> roles;
    private long clientId;
    private String onboardStatus;
    private String timeZone;
}
