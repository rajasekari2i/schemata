package com.opsbeach.connect.schemata.repository;

import java.util.List;

import com.opsbeach.connect.schemata.dto.RedshiftDto;

// this repo is to fetch schema structure of postgres
public interface SchemaRepository {
    List<RedshiftDto> getSchemaByName(String schemaName);
}
