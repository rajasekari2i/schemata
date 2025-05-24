package com.opsbeach.user.mapper;

import com.opsbeach.sharedlib.dto.JwtDto;
import com.opsbeach.user.entity.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtMapper {

    public JwtDto domainToDto(Jwt jwt) {
        return JwtDto.builder().accessToken(jwt.getAccessToken())
                .refreshToken(jwt.getRefreshToken())
                .privateKey(jwt.getPrivateKey())
                .publicKey(jwt.getPublicKey())
                .id(jwt.getId())
                .isDeleted(jwt.getIsDeleted())
                .expireAt(jwt.getExpiryAt())
                .build();
    }

    public Jwt dtoToDomain(JwtDto jwtDto) {
        return Jwt.builder().accessToken(jwtDto.getAccessToken())
                .refreshToken(jwtDto.getRefreshToken())
                .privateKey(jwtDto.getPrivateKey())
                .publicKey(jwtDto.getPublicKey())
                .isDeleted(jwtDto.getIsDeleted())
                .expiryAt(jwtDto.getExpireAt())
                .build();
    }
}
