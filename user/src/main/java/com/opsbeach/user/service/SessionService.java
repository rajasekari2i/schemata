package com.opsbeach.user.service;

import com.opsbeach.sharedlib.dto.SessionDto;
import com.opsbeach.user.entity.Session;
import com.opsbeach.user.mapper.SessionMapper;
import com.opsbeach.user.repository.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * CRUD implementation for UserSession.
 * </p>
 */
@Slf4j
@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;

    public SessionService(SessionRepository sessionRepository, SessionMapper sessionMapper) {
        this.sessionRepository = sessionRepository;
        this.sessionMapper = sessionMapper;
    }

    public SessionDto add(SessionDto sessionDto) {
        Session session = sessionMapper.dtoToDomain(sessionDto);
        return sessionMapper.domainToDto(addModel(session));
    }

    public Session addModel(Session session) {
        return (Session)sessionRepository.save(session);
    }

}