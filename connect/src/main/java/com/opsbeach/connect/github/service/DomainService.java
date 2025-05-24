package com.opsbeach.connect.github.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.github.dto.DomainDto;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Domain;
import com.opsbeach.connect.github.repository.DomainRepository;
   import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DomainService {
    
    private final DomainRepository domainRepository;

    private final ResponseMessage responseMessage;

    private final IdSpecifications<Domain> domainSpecifications;

    public DomainDto add(DomainDto domainDto) {
        var domain = domainDto.toDomain(domainDto);
        domain = addModel(domain);
        return domain.toDto(domain);
    }

    public Domain addModel(Domain domain) {
        return domainRepository.save(domain);
    }

    public Domain addDomain(ClientRepo clientRepo, Long nodeId) {
        var domain = Domain.builder().clientId(clientRepo.getClientId()).nodeId(nodeId).clientRepoId(clientRepo.getId())
                                    .name(clientRepo.getFullName()).build();
        return addModel(domain);
    }

    public DomainDto get(Long id) {
        var domain = getModel(id);
        return domain.toDto(domain);
    }

    public Domain getModel(Long id) {
        return domainRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.DOMAIN)));
    }

    public Domain getDefaultDomain(String clientRepoFullName) {
        return domainRepository.findOne(domainSpecifications.findByName(clientRepoFullName)).orElse(null);
    }

    public List<DomainDto> getAll(Long clientRepoId) {
        Specification<Domain> specification = Specification.where(null);
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(clientRepoId))) {
            specification = specification.and(domainSpecifications.findByClientRepoId(clientRepoId));
        }
        return toDtos(domainRepository.findAll(specification));
    }

    private List<DomainDto> toDtos(List<Domain> domains) {
        return domains.isEmpty() ? List.of() : domains.stream().map(domains.get(0)::toDto).toList();
    }

    public void deleteByClientRepoId(Long clientRepoId) {
        domainRepository.deleteByClientRepoId(clientRepoId);
    }
}
