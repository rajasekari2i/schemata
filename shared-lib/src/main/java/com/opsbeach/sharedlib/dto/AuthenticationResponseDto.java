package com.opsbeach.sharedlib.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>
 *     Authentication Response Dto
 * </p>>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponseDto {
    private Boolean isOnboarded;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Token token;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Token {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String accessToken;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String refreshToken;
        private String tokenType;
        private Integer expiresIn;
        private Boolean isChangePasswordRequired;
    }
}
