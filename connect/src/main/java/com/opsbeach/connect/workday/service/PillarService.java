package com.opsbeach.connect.workday.service;

import java.util.List;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.workday.dto.PillarDto;
import com.opsbeach.connect.workday.entity.Pillar;
import com.opsbeach.connect.workday.repository.PillarRepository;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PillarService {

    private final PillarRepository pillarRepository;

    private final ResponseMessage responseMessage;

    private final IdSpecifications<Pillar> pillarSpecifications;

    public PillarDto add(PillarDto pillarDto) {
        Specification<Pillar> bSpecification = pillarSpecifications.findByName(pillarDto.getName());
        var pillar = pillarRepository.findOne(bSpecification).orElse(null);
        if (!ObjectUtils.isEmpty(pillar)) { throw new IllegalArgumentException(responseMessage.getErrorMessage(ErrorCode.ALREADY_EXISTS, Constants.PILLAR)); }
        pillar = pillarDto.toDomain(pillarDto);
        return pillar.toDto(pillarRepository.save(pillar));
    }

    public PillarDto get(Long id) {
        var pillar = pillarRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.PILLAR)));
        return pillar.toDto(pillar);
    }

    public PillarDto getByName(String name) {
        Specification<Pillar> bSpecification = pillarSpecifications.findByName(name);
        var pillar = pillarRepository.findOne(bSpecification).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND, name)));
        return pillar.toDto(pillar);
    }

    public List<PillarDto> getAll() {       
        var pillars = pillarRepository.findAll();
        return ObjectUtils.isEmpty(pillars) ? List.of() : pillars.stream().map(pillars.get(0)::toDto).collect(Collectors.toList());
    }

    public PillarDto update(PillarDto pillarDto) {
        get(pillarDto.getId());
        var pillar = pillarDto.toDomain(pillarDto);
        return pillar.toDto(pillarRepository.save(pillar));
    }

    public String delete(Long id) {
        var pillarDto = get(id);
        var pillar = pillarDto.toDomain(pillarDto);
        pillar.setIsDeleted(Boolean.TRUE);
        pillarRepository.save(pillar);
        return responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.PILLAR);
    }
}
