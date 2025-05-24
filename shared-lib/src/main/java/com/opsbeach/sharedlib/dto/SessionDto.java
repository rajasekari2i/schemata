package com.opsbeach.sharedlib.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>
 * Session Audit information.
 * </p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private Long id;
    private String uri;
    private String type;
    private Long userId;
    private String action;
    private String module;
    private String ipAddress;
    private Boolean successLogin;
}
