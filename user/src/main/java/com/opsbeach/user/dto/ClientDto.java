package com.opsbeach.user.dto;

import com.opsbeach.user.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Holds details of Tenant
 * </p>
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto extends BaseDto {
    private String name;
    private String description;
    private boolean isOnboarded;
}