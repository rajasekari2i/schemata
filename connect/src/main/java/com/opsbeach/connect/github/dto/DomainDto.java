package com.opsbeach.connect.github.dto;

import com.opsbeach.connect.github.entity.Domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class DomainDto {
    
    Long id;
    Long clientId;
    Long nodeId;
    Long clientRepoId;
    String name;
    
    public Domain toDomain(DomainDto domainDto) {
        return Domain.builder().id(domainDto.getId())
                               .clientId(domainDto.getClientId())
                               .nodeId(domainDto.getNodeId())
                               .name(domainDto.getName())
                               .clientRepoId(domainDto.getClientRepoId())
                               .build();
    }
}
