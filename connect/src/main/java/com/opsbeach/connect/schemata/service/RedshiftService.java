package com.opsbeach.connect.schemata.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.schemata.dto.RedshiftDto;
import com.opsbeach.connect.schemata.entity.DomainNode;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.SchemaType;
import com.opsbeach.connect.schemata.repository.SchemaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedshiftService {
    
    // to fetch shema from our postgres db.
    private final SchemaRepository schemaRepository;

    private final TableService tableService;

    private final DomainNodeService domainNodeService;

    static final List<String> PRIMITIVE = List.of("integer", "bigint", "text", 
                                                  "float4", "float8", "varchar", 
                                                  "character varying", "bool", "boolean", 
                                                  "bytea", "char", "name", "numeric",
                                                  "int8", "int2", "int2vector", "int4");

    // get schema structure from redshift and save in neo4j
    public DomainNode getSchema(Long domainId) {

        var domain = domainNodeService.get(domainId);
        
        // this line should replace by redshift api, to fetch schema.
        var redshiftDtos = schemaRepository.getSchemaByName(domain.getName());   
        
        Map<String, Table> tableMap = new HashMap<>();
        redshiftDtos.forEach(dto -> {
            var table = tableMap.get(dto.getTableName());
            if (table == null) {
                table = Table.builder().fields(List.of(createField(dto))).schemaType(SchemaType.ENTITY).name(dto.getTableName().toString()).build();
            } else {
                List<Field> fields = new ArrayList<>();
                fields.addAll(table.getFields());
                fields.add(createField(dto));
                table.setFields(fields);
            }
            tableMap.put(dto.getTableName().toString(), table);
        });
        
        var tables = tableService.addTables(List.copyOf(tableMap.values()));
        if (!ObjectUtils.isEmpty(domain.getTables())) {
            tables.addAll(domain.getTables());
        }
        domain.setTables(tables);
        domain = domainNodeService.update(domain); 
        return domain;
    }

    private Field createField(RedshiftDto redshiftDto) {
        return Field.builder().schema(redshiftDto.getTableSchema().toString())
                              .name(redshiftDto.getColumnName().toString())
                              .dataType(redshiftDto.getDataType().toString())
                              .defaultValue(cheackNull(redshiftDto.getColumnDefault()))
                            //   .isNullable(redshiftDto.getIsNullable().toString())
                              .isPrimitiveType(PRIMITIVE.contains(redshiftDto.getDataType().toString()))
                              .build();
    }

    private String cheackNull(Object object) {
        return object == null ? null : object.toString();
    }
}
