package com.opsbeach.sharedlib.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.opsbeach.sharedlib.utils.Constants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 *
 */
@Getter
@Setter
public class UserDto implements UserDetails, Serializable {
    private long id;
    private String userType;
    private String email;
    private String username;
    private String mobile;
    private Collection<? extends GrantedAuthority> authorities;
    private Boolean isDeleted;
    private transient List<RoleDto> roles;
    private long clientId;
    private String onboardStatus;
    private String timeZone;
    private String accessToken;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return Constants.EMPTY;
    }

    @Override
    public String getUsername() {
        return this.getMobile();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !getIsDeleted();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !getIsDeleted();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !getIsDeleted();
    }

    @Override
    public boolean isEnabled() {
        return !getIsDeleted();
    }
}
