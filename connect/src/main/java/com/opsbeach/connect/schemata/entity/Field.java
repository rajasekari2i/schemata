package com.opsbeach.connect.schemata.entity;

import java.util.List;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.opsbeach.connect.schemata.dto.FieldDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Node
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Field {
    
    @Id @GeneratedValue
    private Long id;

    private int rowNumber;

    private String jsonSchemaRefId;

    @Builder.Default
    private Boolean isDeleted = Boolean.FALSE;

    @Builder.Default
    private Boolean isUserChanged = Boolean.FALSE;

    private Long prId;  // primary key of pull_request table postgres.

    private String name;

    private String schema;
    
    private String dataType;

    private Boolean isPrimitiveType;

    private String description;   //editable
    
    private String defaultValue;
    
    @Builder.Default
    private Boolean isPii = Boolean.FALSE;  //editable
    
    @Builder.Default
    private Boolean isClassified = Boolean.FALSE;   //editable

    @Builder.Default
    private Boolean deprecated = Boolean.FALSE;
    
    private Boolean isPrimaryKey;

    private String enumFilePath;

    private String enumName;

    private String enumPackage;
    
    private String[] symbols;  // for enum values

    private String items; // for array field (ex: Array<?>)

    private Integer size; // for fixed field

    private String values; // for Map field (ex: Map<String,?>) The key for an Avro map must be a string

    @Setter
    @Relationship(value = "ARRAY_FIELD") // this field get added while items (or) values field contains ARRAY
    private Field arrayField; // for nested array field (ex: Array<Array<?>> or Map<Array<?>>)

    @Setter
    @Relationship(value = "MAP_FIELD") // this field get added while items (or) values field contains MAP
    private Field mapField;  // for nested map field  (ex: Map<Map<String,?>> or Array<Map<String,?>>)

    @Setter
    @Relationship(value = "UNION_TYPE")
    private List<Field> unionTypes;  
    // union field may have more than one filed (ex: [null, boolean, double, record]) these all in one type is union.

    @Setter
    @Relationship(value = "CONTAIN")
    private Table contain;

    @Setter
    @Relationship(value = "REFERENCE_FIELD") // This Field is refered to field of another table. (i.e. may or may not be foreign key).
    private Field referenceField;
    
    public FieldDto toDto(Field field) {
        return FieldDto.builder().id(field.getId())
                                 .rowNumber(field.getRowNumber())
                                 .isDeleted(field.getIsDeleted())
                                 .isUserChanged(field.getIsUserChanged())
                                 .prId(field.getPrId())
                                 .name(field.getName())
                                 .isPii(field.getIsPii())
                                 .schema(field.getSchema())
                                 .dataType(field.getDataType())
                                 .isPrimitiveType(field.isPrimitiveType)
                                 .description(field.getDescription())
                                 .defaultValue(field.getDefaultValue())
                                 .isClassified(field.getIsClassified())
                                 .deprecated(field.getDeprecated())
                                 .isPrimaryKey(field.getIsPrimaryKey())
                                 .symbols(field.getSymbols())
                                 .items(field.getItems())
                                 .size(field.getSize())
                                 .values(field.getValues())
                                 .arrayField(ObjectUtils.isEmpty(field.getArrayField()) ? null : field.getArrayField().toDto(field.getArrayField()))
                                 .mapField(ObjectUtils.isEmpty(field.getMapField()) ? null : field.getMapField().toDto(field.getMapField()))
                                 .unionTypes(ObjectUtils.isEmpty(field.getUnionTypes()) ? List.of() : field.getUnionTypes().stream().map(field.getUnionTypes().get(0)::toDto).toList())
                                 .contain(ObjectUtils.isEmpty(field.getContain()) ? null : field.getContain().toDto(field.getContain()))
                                 .referenceField(ObjectUtils.isEmpty(field.getReferenceField()) ? null : field.getReferenceField().toDto(field.getReferenceField()))
                                 .build();
    }

    public static boolean isPrimitiveType(String dataType) {
        var primitives = List.of("string", "bytes", "int", "long", "float", "double", "boolean", "null", "number", "boolean", "integer");
        return primitives.contains(dataType);
    }
     public static class Prop {

        public static final String DATA_TYPE = "dataType";

        // AVRO
        public static final String DESC = "desc";
        public static final String DESCRIPTION = "description";
        public static final String COMMENT = "comment";
        public static final String SEE_ALSO = "see_also";
        public static final String REFERENCE = "reference";
        public static final String IS_PII = "is_pii";
        public static final String DEPRECATED = "deprecated";
        public static final String IS_CLASSIFIED = "is_classified";
        public static final String IS_PRIMARY_KEY = "is_primary_key";
        public static final String PRODUCT_TYPE = "product_type";
        public static final String LINK = "link";
        public static final String DEPENDS = "depends";
        public static final String MODEL = "model";
        public static final String COLUMN = "column";
        public static final String DEFAULT = "default";
        public static final String CLASSIFICATION_LEVEL = "classification_level";

        // JSON
        public static final String TYPE = "type";
        public static final String PROPERTIES = "properties";
        public static final String OBJECT = "object";
        public static final String ITEMS = "items";
        public static final String ARRAY = "array";
        public static final String ENUM = "enum";
        public static final String STRING = "string";
        public static final String INTEGER = "integer";
        public static final String MIN_ITEMS = "minItems";
        public static final String MIN_LENGTH = "minLength";
        public static final String MAX_LENGTH = "maxLength";
        public static final String PATTERN = "pattern";
        public static final String MINIMUM = "minimum";
        public static final String MAXIMUM = "maximum";
        public static final String ANY_OF = "anyOf";
        public static final String ONE_OF = "oneOf";
        public static final String $REF = "$ref";
        public static final String MULTIPLE_OF = "multipleOf";
        public static final String EXCLUSIVE_MINIMUM = "exclusiveMinimum";
        public static final String EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
    }
}
