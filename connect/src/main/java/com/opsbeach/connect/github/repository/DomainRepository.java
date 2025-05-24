package com.opsbeach.connect.github.repository;

import com.opsbeach.connect.core.BaseRepository;
import com.opsbeach.connect.github.entity.Domain;

public interface DomainRepository extends BaseRepository<Domain> {
    
    void deleteByClientRepoId(Long clientRepoId);
}
