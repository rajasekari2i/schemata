package com.opsbeach.connect.github.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.opsbeach.connect.core.BaseRepository;
import com.opsbeach.connect.github.dto.AutoCompleteModelDto;
import com.opsbeach.connect.github.entity.Model;

public interface ModelRepository extends BaseRepository<Model> {
    
    @Query(value = """
            SELECT m.name, m.name_space nameSpace, m.node_id nodeId FROM analytics.model m 
            INNER JOIN analytics.client_repo c ON m.client_repo_id = c.id WHERE
            m.name ilike :name AND m.client_id = :clientId AND m.is_deleted = false AND c.status = :status
            GROUP BY m.name, m.name_space, m.node_id
            """, nativeQuery = true)
    List<AutoCompleteModelDto> findByNameLike(@Param("name") String name, @Param("clientId") Long clientId, @Param("status") String status);

    void deleteAllByClientRepoId(Long clientRepoId);
}
