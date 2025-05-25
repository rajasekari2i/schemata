package com.opsbeach.connect.schemata.repository;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import com.opsbeach.connect.schemata.dto.RedshiftDto;

// temporary repository to fetch schema from postgres
@Repository
public class SchemaRepositoryImpl implements SchemaRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    // get schema by schema name
    @SuppressWarnings("unchecked")
    @Override
    public List<RedshiftDto> getSchemaByName(String schemaName) {
        List<RedshiftDto> schemaDtos = new ArrayList<>();
        var schemaDto = new RedshiftDto();
        var query = entityManager.createNativeQuery("select * from information_schema.columns where table_schema = ?");
        query.setParameter(1, schemaName);
        List<Object[]> list = query.getResultList();
        for (Object[] obj : list) {
            schemaDtos.add(schemaDto.toDto(obj));
        }
        return schemaDtos;
    }
}
