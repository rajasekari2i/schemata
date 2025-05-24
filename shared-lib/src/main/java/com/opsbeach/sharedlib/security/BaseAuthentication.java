package com.opsbeach.sharedlib.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * <p>
 *     Basic authentication entity
 * </p>
 */
public class BaseAuthentication implements Authentication {

    private final UserDetails userDetails;

    public BaseAuthentication(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public static BaseAuthentication getInstance(UserDetails userDetails) {
        return new BaseAuthentication(userDetails);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public void setAuthenticated(boolean b) throws IllegalArgumentException {
        //Override method
    }

    @Override
    public String getName() {
        return null;
    }
}
