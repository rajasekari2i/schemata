package com.opsbeach.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.user.base.BaseMapper;
import com.opsbeach.user.base.BaseService;
import com.opsbeach.user.base.specification.IdSpecifications;
import com.opsbeach.user.dto.ClientDto;
import com.opsbeach.user.entity.Client;
import com.opsbeach.user.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * <p>
 * Implements the CRUD operation for the Tenant of an user.
 * </p>
 */
@Slf4j
@Service
public class ClientService extends BaseService<Client, ClientDto> {

    private final App2AppService app2AppService;

    protected ClientService(ClientRepository clientRepository, BaseMapper<Client, ClientDto> baseMapper,
                            IdSpecifications<Client> tenantIdSpecifications, App2AppService app2AppService) {
        super(clientRepository, baseMapper, tenantIdSpecifications);
        this.app2AppService = app2AppService;
    }

    @Value("${application.connect.create-organization-node}")
    private String createOrganizationUrl;

    @Override
    public void validateAdd(ClientDto incomingDto) {
        if (incomingDto.getIsDeleted() == null) {
            incomingDto.setIsDeleted(Boolean.FALSE);
        }
    }

    @Override
    public void doPatch(Client incomingTenant, Client toUpdateTenant) {
        if (Objects.nonNull(incomingTenant.isOnboarded())) {
            toUpdateTenant.setOnboarded(incomingTenant.isOnboarded());
        }
        if (Objects.nonNull(incomingTenant.getDescription())) {
            toUpdateTenant.setDescription(incomingTenant.getDescription());
        }
    }

    /*
     * Returns details of Tenant.
     *
     * @return TenantDto - Details of Tenant.
     */
    public ClientDto getClient(String name) {
        log.info("Fetching Tenant Details");
        return findByClientName(name.toUpperCase());
    }

    public ClientDto add(String clientName) {
        try {
            // if client is present already then return.
            var clientDto = getClient(clientName);
            return clientDto;
        } catch (Exception e) {
            // if client is not present the exception is thrown. then create new client and add.
            var clientDto = ClientDto.builder().name(clientName.toUpperCase()).build();
            clientDto = add(clientDto);
            createOrganizationNode(clientDto);
            return clientDto;
        }
    }

    private void createOrganizationNode(ClientDto clientDto) {
        var url = createOrganizationUrl.replace("{clientName}", clientDto.getName());
        var entity = app2AppService.setHeaders(Map.of(Constants.CLIENT_ID_HEADER, clientDto.getId().toString()), null);
        app2AppService.httpPost(url, entity, JsonNode.class);
    }

    public ClientDto updateOnBoardedStatus(Long id, boolean isOnboarded) {
        var clientDto = findById(id);
        clientDto.setOnboarded(isOnboarded);
        patch(clientDto);
        return clientDto;
    }
}
