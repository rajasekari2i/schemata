package com.opsbeach.user.service;

import com.opsbeach.sharedlib.dto.JwtDto;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.user.base.specification.IdSpecifications;
import com.opsbeach.user.entity.Jwt;
import com.opsbeach.user.mapper.JwtMapper;
import com.opsbeach.user.repository.JwtRespository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class JwtService {

    private final JwtRespository jwtRespository;
    private final JwtMapper jwtMapper;
    private final IdSpecifications<Jwt> jwtIdSpecifications;
    private final ResponseMessage responseMessage;

    public JwtService(JwtRespository jwtRespository, JwtMapper jwtMapper, IdSpecifications<Jwt> jwtIdSpecifications,
                      ResponseMessage responseMessage) {
        this.jwtRespository = jwtRespository;
        this.jwtMapper = jwtMapper;
        this.jwtIdSpecifications = jwtIdSpecifications;
        this.responseMessage = responseMessage;
    }

    public JwtDto add(JwtDto jwtDto) {
        Jwt jwt = jwtMapper.dtoToDomain(jwtDto);
        jwt.setIsDeleted(Boolean.FALSE);
        return jwtMapper.domainToDto(addModel(jwt));
    }

    public Jwt addModel(Jwt jwt) {
        return jwtRespository.save(jwt);
    }

    public Jwt getByAccessTokenModel(String authenticationToken) {
        Specification<Jwt> baseSpecification = jwtIdSpecifications.findByAccessToken(authenticationToken);
        Optional<Jwt> jwtOptional = jwtRespository.findOne(baseSpecification);
        if (jwtOptional.isEmpty()) {
            throw new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND,
                    responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND, "There access token"));
        }
        return jwtOptional.get();
    }

    public JwtDto getByAccessToken(String authenticationToken) {
        return jwtMapper.domainToDto(getByAccessTokenModel(authenticationToken));
    }

    public JwtDto getByRefreshToken(String authenticationToken) {
        Specification<Jwt> baseSpecification = jwtIdSpecifications.findByRefreshToken(authenticationToken);
        Optional<Jwt> jwtOptional = jwtRespository.findOne(baseSpecification);
        if (jwtOptional.isEmpty()) {
            throw new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND,
                    responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND, "There access token"));
        }
        return jwtMapper.domainToDto(jwtOptional.get());
    }

    public Boolean delete(String accessToken) {
        var jwt = getByAccessTokenModel(accessToken);
        jwtRespository.delete(jwt);
        return Boolean.TRUE;
    }
}
