package com.opsbeach.connect.github.dto;

import com.opsbeach.connect.github.entity.ClientRepo;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClientRepoDto {

    Long id;
    String name;
    String owner;
    String fullName;
    ClientRepo.RepoType repoType;
    Long connectId;
    ClientRepo.Status status;
    String defaultBranch;
    ClientRepo.RepoSource repositorySource;
    
    public ClientRepo toDomain(ClientRepoDto clientRepoDto) {
        return ClientRepo.builder().id(clientRepoDto.id)
                                   .name(clientRepoDto.name)
                                   .owner(clientRepoDto.owner)
                                   .fullName(clientRepoDto.fullName)
                                   .repoType(clientRepoDto.repoType)
                                   .connectId(clientRepoDto.connectId)
                                   .status(clientRepoDto.status)
                                   .defaultBranch(clientRepoDto.defaultBranch)
                                   .repositorySource(clientRepoDto.getRepositorySource())
                                   .build();
    }
}
