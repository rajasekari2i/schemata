package com.opsbeach.user.mapper;

import com.opsbeach.user.base.BaseMapper;
import com.opsbeach.user.dto.ClientDto;
import com.opsbeach.user.entity.Client;
import org.springframework.stereotype.Component;


/**
 * <p>
 * Converts TenantDto to Tenant Entity and vice versa.
 * </p>
 */
@Component
public class ClientMapper implements BaseMapper<Client, ClientDto> {

    public ClientDto domainToDto(Client tenant) {
        return ClientDto.builder().name(tenant.getName())
                .description(tenant.getDescription())
                .id(tenant.getId())
                .isOnboarded(tenant.isOnboarded())
                .isDeleted(tenant.getIsDeleted())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .createdBy(tenant.getCreatedBy())
                .updatedBy(tenant.getUpdatedBy()).build();
    }

    public Client dtoToDomain(ClientDto clientDto) {
        return Client.builder().name(clientDto.getName())
                .description(clientDto.getDescription())
                .isOnboarded(clientDto.isOnboarded())
                .isDeleted(clientDto.getIsDeleted()).build();
    }
}
