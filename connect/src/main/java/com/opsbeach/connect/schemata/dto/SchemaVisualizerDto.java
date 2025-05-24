package com.opsbeach.connect.schemata.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SchemaVisualizerDto {
    
    private List<TableDto> tables;

    private List<FieldDto> fields;

    private List<Map<String, Long>> links;

    // this enum is used for parsing purpose of uidto
    public enum Purpose {
        VALIDATE(0), SUBMIT(1);

        private final int key; 

        Purpose(int key) {
            this.key = key;
        }
        public int getKey() {
            return this.key;
        }
    }
}
