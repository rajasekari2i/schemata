package com.opsbeach.sharedlib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
    private String username;
    private String password;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendOTP {
        private String username;
    }
}