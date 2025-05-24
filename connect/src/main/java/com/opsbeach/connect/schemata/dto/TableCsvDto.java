package com.opsbeach.connect.schemata.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TableCsvDto {
    private String tableName;
    private String tableNamespace;
    private String tableDescription;
    private String owner;
    private String domain;
    private String columnName;
    private String dataType;
    private String columnDescription;
    private Boolean isPii;
    private Boolean isClassified;

    public static String[] getCsvHeaders() {
        String[] arr = {"table_namespace", "table_name", "table_description", "owner", "domain", "column_name", "data_type", "column_description", "is_pii", "is_classified"};
        return arr;
    }
}
