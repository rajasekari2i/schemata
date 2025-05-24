package com.opsbeach.user.mapper;

import com.opsbeach.sharedlib.dto.SessionDto;
import com.opsbeach.user.entity.Session;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public SessionDto domainToDto(Session session) {
        return SessionDto.builder().userId(session.getUserId())
                .action(session.getAction())
                .ipAddress(session.getIpAddress())
                .module(session.getModule())
                .type(session.getType())
                .uri(session.getUri())
                .id(session.getId())
                .successLogin(session.getSuccessLogin())
                .build();
    }

    public Session dtoToDomain(SessionDto sessionDto) {
        return Session.builder().userId(sessionDto.getUserId())
                .action(sessionDto.getAction())
                .ipAddress(sessionDto.getIpAddress())
                .module(sessionDto.getModule())
                .type(sessionDto.getType())
                .uri(sessionDto.getUri())
                .successLogin(sessionDto.getSuccessLogin())
                .build();
    }
}
