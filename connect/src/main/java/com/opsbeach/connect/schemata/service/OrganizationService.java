package com.opsbeach.connect.schemata.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.schemata.entity.Organization;
import com.opsbeach.connect.schemata.repository.OrganizationRepository;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    
    private final OrganizationRepository organizationRepository;

    private final ResponseMessage responseMessage;

    public Organization add(Organization organization) {
        return organizationRepository.save(organization);
    }

    public Organization add(Long clientId, String clientName) {
        var organization = getByClientId(clientId);
        if (ObjectUtils.isEmpty(organization)) {
            organization = Organization.builder().name(clientName)
                                                 .clinetId(clientId)
                                                 .build();
            return add(organization);
        }
        return organization;
    }

    public Organization get(Long id) {
        return organizationRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.ORGANIZATION)));
    }

    public Organization getByClientId(Long clientId) {
        return organizationRepository.findByClinetId(clientId);
    }

    public Organization update(Organization organization) {
        get(organization.getId());
        return add(organization);
    }

    public List<Organization> getAll() {
        return organizationRepository.findAll();
    }
}
