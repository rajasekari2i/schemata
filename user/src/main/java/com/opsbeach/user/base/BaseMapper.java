package com.opsbeach.user.base;

import org.springframework.stereotype.Component;

/**
 * <p>
 * BaseMapper for converting domainToDto and vice-versa.
 * </p>
 */
@Component
public interface BaseMapper<M extends BaseModel, D extends BaseDto> {
    D domainToDto(M baseModel);

    M dtoToDomain(D baseDto);
}