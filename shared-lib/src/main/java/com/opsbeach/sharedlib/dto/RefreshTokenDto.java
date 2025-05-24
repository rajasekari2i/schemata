package com.opsbeach.sharedlib.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *     Refresh token resonse dto.
 * </p>
 */
@Getter
@Setter
public class RefreshTokenDto {
    private String accessToken;
    private String refreshToken;
}
