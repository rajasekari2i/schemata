package com.opsbeach.connect.metrics.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.metrics.dto.SlaDto;
import com.opsbeach.connect.metrics.entity.Sla;
import com.opsbeach.connect.metrics.repository.SlaRepository;
import com.opsbeach.sharedlib.exception.AlreadyExistException;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SlaService {

    private final SlaRepository slaRepository;

    private final ResponseMessage responseMessage;

    private final IdSpecifications<Sla> slaSpecifications;
    
    public SlaDto add(SlaDto slaDto) {
        if (getModelByType(slaDto.getType()).isPresent()) {
            throw new AlreadyExistException(ErrorCode.ALREADY_EXISTS, responseMessage.getErrorMessage(ErrorCode.ALREADY_EXISTS, Constants.SLA));
        }
        var sla = slaRepository.save(slaDto.toDomin(slaDto));
        return sla.toDto(sla);
    }

    public SlaDto get(Long id) {
        var sla = slaRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.SLA)));
        return sla.toDto(sla);
    }

    public SlaDto getByType(ServiceType serviceType) {
        var sla = getModelByType(serviceType);
        if (sla.isEmpty()) {
            throw new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND, Constants.SLA));
        }
        return sla.get().toDto(sla.get());
    }

    public Optional<Sla> getModelByType(ServiceType serviceType) {
        return slaRepository.findOne(slaSpecifications.findBySlaType(serviceType));
    }

    public List<SlaDto> getAll() {
        var slas = slaRepository.findAll();
        return !ObjectUtils.isEmpty(slas) ? slas.stream().map(slas.get(0)::toDto).collect(Collectors.toList()) : List.of();
    }

    public SlaDto update(SlaDto slaDto) {
        get(slaDto.getId());
        var sla = slaRepository.save(slaDto.toDomin(slaDto));
        return sla.toDto(sla);
    }

    public String delete(Long id) {
        var slaDto = get(id);
        var sla = slaDto.toDomin(slaDto);
        sla.setIsDeleted(Boolean.TRUE);
        slaRepository.save(sla);
        return responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.SLA);
    }
}
