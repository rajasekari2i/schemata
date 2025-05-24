package com.opsbeach.connect.schemata.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.schemata.dto.TableDto;
import com.opsbeach.connect.schemata.dto.SchemaVisualizerDto;
import com.opsbeach.connect.schemata.entity.DomainNode;
import com.opsbeach.connect.schemata.graph.SchemaGraph;
import com.opsbeach.connect.schemata.repository.DomainNodeRepository;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DomainNodeService {
    
    private final DomainNodeRepository domainNodeRepository;

    private final ResponseMessage responseMessage;

    private final OrganizationService organizationService;

    private final TableService tableService;

    public DomainNode add(DomainNode domain, Long orgId) {
        domainNodeRepository.save(domain);
        domainNodeRepository.createOrganizationDomainRelationship(orgId, domain.getId());
        return domain;
    }

    public DomainNode get(Long id) {
        return domainNodeRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.DOMIN)));
    }

    public DomainNode update(DomainNode domain) {
        get(domain.getId());
        return domainNodeRepository.save(domain);
    }

    public List<DomainNode> getAll(Long orgId) {
        var organization = organizationService.get(orgId);
        return organization.getDomains();
    }

    public List<TableDto> getTablesByDominId(Long dominId) {
        var tables = get(dominId).getTables();
        return tables.isEmpty() ? List.of() : tables.stream().map(tables.get(0)::toDto).toList();
    }

    public double getSchemaScore(Long domainId, String name) {
        var tables = get(domainId).getTables();
        var graph = new SchemaGraph(tables);
        return graph.getSchemataScore(name);
    }

    public SchemaVisualizerDto getSchemaVisualizerByDomain(Long domainId) {
        var tables = get(domainId).getTables();
        return tableService.buildSchemaVisualizerDto(tables);
    }

    public DomainNode addDomainNode(String name, Long clientId, Long clientRepoId) {
        var organization = organizationService.getByClientId(clientId);
        var domainNode =  DomainNode.builder().name(name).clientId(clientId).clientRepoId(clientRepoId).build();
        domainNodeRepository.save(domainNode);
        domainNodeRepository.createOrganizationDomainRelationship(organization.getId(), domainNode.getId());
        return domainNode;
    }

    public void deleteByClientRepoId(Long clientRepoId) {
        domainNodeRepository.deleteByClientRepoId(clientRepoId);
    }
}
