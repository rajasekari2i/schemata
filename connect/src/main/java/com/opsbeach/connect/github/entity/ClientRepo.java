package com.opsbeach.connect.github.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.github.dto.ClientRepoDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Table(name = "client_repo")
@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ClientRepo extends BaseModel {
    
    private String name;

    private String owner;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "repo_type")
    private RepoType repoType;

    @Column(name = "connect_id")
    private Long connectId;

    @Setter
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "default_branch")
    private String defaultBranch;

    @Column(name = "repository_source")
    @Enumerated(EnumType.STRING)
    private RepoSource repositorySource;

    @Setter
    @Column(name = "folder_path")
    private String folderPath;  // same will be the zip file path till initial loading.

    public ClientRepoDto toDto(ClientRepo clientRepo) {
        return ClientRepoDto.builder().id(clientRepo.getId())
                                      .name(clientRepo.getName())
                                      .owner(clientRepo.getOwner())
                                      .fullName(clientRepo.getFullName())
                                      .repoType(clientRepo.getRepoType())
                                      .connectId(clientRepo.getConnectId())
                                      .status(clientRepo.getStatus())
                                      .defaultBranch(clientRepo.getDefaultBranch())
                                      .repositorySource(clientRepo.getRepositorySource())
                                      .build();
    }

    public enum RepoSource {
        GITHUB(1), LOCAL(2);

        private final int key;

        RepoSource(int key) {
            this.key = key;
        }

        public int getKey() {
            return this.key;
        }
    }

    public enum Status {
        ACTIVE(1), DEACTIVE(2);

        private final int key;

        Status(int key) {
            this.key = key;
        }

        public int getKey() {
            return this.key;
        }
    }

    public enum RepoType {
        AVRO(1), JSON(2), PROTOBUF(3), YAML(4);

        private final int key;

        RepoType(int key) {
            this.key = key;
        }

        public int getKey() {
            return this.key;
        }
    }
}
