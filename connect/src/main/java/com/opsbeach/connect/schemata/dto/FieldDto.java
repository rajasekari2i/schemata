package com.opsbeach.connect.schemata.dto;

import java.util.List;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.opsbeach.connect.schemata.entity.Field;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(Include.NON_NULL)
public class FieldDto {
    
    private Long id;

    private int rowNumber;

    @Builder.Default
    private Boolean isDeleted = Boolean.FALSE;

    @Builder.Default
    private Boolean isUserChanged = Boolean.FALSE;

    private Long prId;

    private String name;

    private String schema;
    
    private String dataType;
    
    private Boolean isPrimitiveType;

    private String description;
    
    private String defaultValue;

    private Boolean isPii;
    
    private Boolean isClassified;

    private Boolean deprecated;
    
    private Boolean isPrimaryKey;
    
    private String[] symbols;  // for enum values

    private String items; // for array field (ex: Array<?>)

    private Integer size; // for fixed field

    private String values; // for Map field (ex: Map<String,?>) The key for an Avro map must be a string

    // this field get added while items (or) values field contains ARRAY
    private FieldDto arrayField; // for nested array field (ex: Array<Array<?>> or Map<Array<?>>)

    // this field get added while items (or) values field contains MAP
    private FieldDto mapField;  // for nested map field  (ex: Map<Map<String,?>> or Array<Map<String,?>>)

    private List<FieldDto> unionTypes;  
    // union field may have more than one filed (ex: [null, boolean, double, record]) these all in one type is union.

    private TableDto contain;

    // This Field is refered to field of another table. (i.e. may or may not be foreign key).
    private FieldDto referenceField;

    public Field toDomin(FieldDto fieldDto) {
        return Field.builder().id(fieldDto.getId())
                              .rowNumber(fieldDto.getRowNumber())
                              .isDeleted(fieldDto.getIsDeleted())
                              .isUserChanged(fieldDto.getIsUserChanged())
                              .prId(fieldDto.getPrId())
                              .name(fieldDto.getName())
                              .isPii(fieldDto.getIsPii())
                              .schema(fieldDto.getSchema())
                              .dataType(fieldDto.getDataType())
                              .isPrimitiveType(fieldDto.isPrimitiveType)
                              .description(fieldDto.getDescription())
                              .defaultValue(fieldDto.getDefaultValue())
                              .isClassified(fieldDto.getIsClassified())
                              .deprecated(fieldDto.getDeprecated())
                              .isPrimaryKey(fieldDto.getIsPrimaryKey())
                              .symbols(fieldDto.getSymbols())
                              .items(fieldDto.getItems())
                              .size(fieldDto.getSize())
                              .values(fieldDto.getValues())
                              .arrayField(ObjectUtils.isEmpty(fieldDto.getArrayField()) ? null : fieldDto.getArrayField().toDomin(fieldDto.getArrayField()))
                              .mapField(ObjectUtils.isEmpty(fieldDto.getMapField()) ? null : fieldDto.getMapField().toDomin(fieldDto.getMapField()))
                              .unionTypes(ObjectUtils.isEmpty(fieldDto.getUnionTypes()) ? List.of() : fieldDto.getUnionTypes().stream().map(fieldDto.getUnionTypes().get(0)::toDomin).toList())
                              .contain(ObjectUtils.isEmpty(fieldDto.getContain()) ? null : fieldDto.getContain().toDomain(fieldDto.getContain()))
                              .referenceField(ObjectUtils.isEmpty(fieldDto.getReferenceField()) ? null : fieldDto.getReferenceField().toDomin(fieldDto.getReferenceField()))
                              .build();
    }
}
