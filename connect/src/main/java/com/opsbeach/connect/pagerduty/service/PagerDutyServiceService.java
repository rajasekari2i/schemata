package com.opsbeach.connect.pagerduty.service;

import java.util.List;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.ServiceDto;
import com.opsbeach.connect.pagerduty.entity.PagerDutyService;
import com.opsbeach.connect.pagerduty.repository.PagerDutyServiceRepository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagerDutyServiceService {
    
    private final PagerDutyServiceRepository pagerDutyServiceRepository;
    private final IdSpecifications<PagerDutyService> serviceSpecifications;

    public List<ServiceDto> addAll(List<ServiceDto> serviceDto) {
        List<PagerDutyService> pagerDutyServices = !ObjectUtils.isEmpty(serviceDto) ? serviceDto.stream().map(serviceDto.get(0)::toDomain).collect(Collectors.toList()) : List.of();
        pagerDutyServiceRepository.saveAll(pagerDutyServices);
        return !ObjectUtils.isEmpty(pagerDutyServices) ? pagerDutyServices.stream().map(pagerDutyServices.get(0)::toDto).collect(Collectors.toList()) : List.of();
    }

    public List<ServiceDto> getAll(Long clientId) {
        Specification<PagerDutyService> bSpecification = serviceSpecifications.findByClientId(clientId).and(serviceSpecifications.findByDeleted(Boolean.FALSE));
        var pagerdutyServices = pagerDutyServiceRepository.findAll(bSpecification);
        return !ObjectUtils.isEmpty(pagerdutyServices) ? pagerdutyServices.stream().map(pagerdutyServices.get(0)::toDto).collect(Collectors.toList()) : List.of();

    }

    public void deleteAllByIds(List<Long> ids) {
        pagerDutyServiceRepository.deleteAllByIdInBatch(ids);
    }
}
