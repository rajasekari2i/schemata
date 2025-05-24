package com.opsbeach.connect.schemata.processor.json;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.tool.schema.extract.spi.SchemaExtractionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonSchemaParser {

    private final TableService tableService;

    private final List<String> PRIMITIVES;

    private final ObjectMapper mapper;

    private final String DRAFT = "http://json-schema.org/draft-07/schema#";

    public JsonSchemaParser(TableService tableService, List<String> primitives, ObjectMapper mapper) {
        this.tableService = tableService;
        this.PRIMITIVES = primitives;
        this.mapper = mapper;
    }

    // get table properties from JsonNode of schema.
    public Table parseTable(JsonNode schema, Map<String, Table> tableMap, Map<Field, String> fieldTableMap, Boolean initialPull) {
        var draft = getString(schema, Table.Prop.$SCHEMA, null);
            if (Objects.isNull(draft) || Boolean.FALSE.equals(draft.equals(DRAFT))) throw new SchemaExtractionException("Schema Draft is invalid - (Draft 07 only acceptable)");
        var id = getString(schema, Table.Prop.JSON_SCHEMA_ID, null);
            if (Objects.isNull(id)) throw new SchemaExtractionException("Schema $id is not present");
        // var name = getString(schema, Table.Prop.TITLE, null);
            // if (Objects.isNull(name)) throw new SchemaExtractionException("Schema Title is Not present");
        // if (name.contains(" ")) throw new SchemaExtractionException("Invalid title - "+name);

        var uri = getUri(id);
        var name = uri.getPath().substring(uri.getPath().lastIndexOf("/")+1);
        var nameSpace = uri.getPath().substring(1, uri.getPath().lastIndexOf("/")).replaceAll("/", ".");
        return constructTable(id, schema, nameSpace, name, tableMap, fieldTableMap, initialPull);
    }

    private Table constructTable(String schemaId, JsonNode schema, String nameSpace, String name, Map<String, Table> tableMap, Map<Field, String> fieldTableMap, Boolean initialPull) {
        var fullname = StringUtil.constructStringEmptySeparator(nameSpace, ".", name);
        return Table.builder().name(name)
                              .nameSpace(nameSpace)
                              .jsonSchemaId(schemaId)
                              .clientId(SecurityUtil.getClientId())
                              .type(schema.get(Table.Prop.TYPE).asText())
                              .description(getString(schema, Table.Prop.DESCRIPTION, null))
                              .fields(parseFields(fullname, schema, tableMap, fieldTableMap, initialPull))   // get list of fields
                            //   .requiredFields(schema.has(Table.Prop.REQUIRED) ? getArrayFromNode(schema.get(Table.Prop.REQUIRED)) : null)
                            //   .additionalProperties(getBoolean(schema, Table.Prop.ADDITIONAL_PROPERTIES))
                              .domain(getString(schema, Table.Prop.DOMAIN, null))
                              .owner(getString(schema, Table.Prop.OWNER, null))
                              .complianceOwner(getString(schema, Table.Prop.COMPLIANCE_OWNER, null))
                              .channel(getString(schema, Table.Prop.CHANNEL, null))
                              .email(getString(schema, Table.Prop.EMAIL, null))
                              .status(getString(schema, Table.Prop.STATUS, "Active"))
                              .subscribers(schema.has(Table.Prop.SUBSCRIBERS) ? getArrayFromNode(schema.get(Table.Prop.SUBSCRIBERS)) : null)
                              .qualityRuleBase(getString(schema, Table.Prop.QUALITY_RULE_BASE, null))
                              .qualityRuleSql(getString(schema, Table.Prop.QUALITY_RULE_SQL, null))
                              .qualityRuleCel(getString(schema, Table.Prop.QUALITY_RULE_CEL, null))
                              .build();
    }

    private URI getUri(String uri) {
        try {
            return new URI(uri);
        } catch (Exception e) {
            throw new InvalidDataException(ErrorCode.INVALID_ID, e.getMessage());
        }
    }

    // get field properites from JsonNode
    private List<Field> parseFields(String schemaName, JsonNode schema, Map<String, Table> tableMap, Map<Field, String> fieldTableMap, Boolean initialPull) {
        if (Boolean.FALSE.equals(schema.has(Table.Prop.PROPERTIES))) {
            return null;
        }
        JsonNode properties = schema.get(Table.Prop.PROPERTIES);
        List<Field> fields = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = properties.fields(); // get fields from node
        var rowNumber = new AtomicInteger(1);
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> fieldEntry = fieldsIterator.next();
            var fieldNode = fieldEntry.getValue();
            var builder = Field.builder(); // build field from node
            builder.rowNumber(rowNumber.getAndIncrement());
            var field = parseField(schema, fieldNode, builder, schemaName, fieldEntry.getKey(), tableMap, fieldTableMap, initialPull);
            fields.add(field);
        }
        return fields;
    }

    private Field parseField(JsonNode schema, JsonNode fieldNode, Field.FieldBuilder builder, String schemaName, 
                             String fieldName, Map<String, Table> tableMap, Map<Field, String> fieldTableMap, Boolean initialPull) {
        builder.description(getString(fieldNode, Field.Prop.DESCRIPTION, null));
        builder.name(fieldName);
        builder.schema(schemaName);
        builder.defaultValue(getString(fieldNode, Field.Prop.DEFAULT, null));
        builder.isPii(getBoolean(fieldNode, Field.Prop.IS_PII, Boolean.FALSE));
        builder.isClassified(getBoolean(fieldNode, Field.Prop.IS_CLASSIFIED, Boolean.FALSE));
        builder.deprecated(getBoolean(fieldNode, Field.Prop.DEPRECATED, Boolean.FALSE));
        /*
         * Here need to check "$ref" for inner schmea because it refers ouside of this schema.
         */
        if (fieldNode.has(Field.Prop.$REF)) {
            // Need to check table is present already or not before saving it into the table.
            var ref = fieldNode.get(Field.Prop.$REF).asText();
            if (ref.startsWith("#/definitions")) {
                var path = ref.split("/");
                fieldNode = schema.get(path[1]).get(path[2]);
                var nameSpace = schemaName; 
                var name = path[2];
                var table = constructTable(null, fieldNode, nameSpace, name, tableMap, fieldTableMap, initialPull);
                if (Boolean.TRUE.equals(initialPull)) {
                    table = tableService.addTable(table);
                    tableMap.put(table.getId().toString(), table);
                } else {
                    tableMap.put(table.getName(), table);
                }
                return builder.isPrimitiveType(false).contain(table).build();
            } else {
                var uri = getUri(ref);
                var path = uri.getPath();
                var name = path.substring(path.lastIndexOf("/")+1);   // taking name from $id
                var nameSpace = path.substring(1, path.lastIndexOf("/")).replaceAll("/", ".");  // taking namespace from $id
                var type = StringUtil.constructStringEmptySeparator(nameSpace, ".", name);
                var field = builder.dataType(type).isPrimitiveType(false).build();
                if (initialPull) field = tableService.addField(field);
                fieldTableMap.put(field, type);
                return field;
            }
        }

        // check weather the field is enum
        if (fieldNode.has(Field.Prop.ENUM)) {
            log.info("Field name - {} of Type - {} of schema - {}", fieldName, Field.Prop.ENUM, schemaName);
            var field = builder.dataType(Field.Prop.ENUM)
                    .isPrimitiveType(false)
                    .symbols(getArrayFromNode(fieldNode.get(Field.Prop.ENUM)))
                    .build();
            return field;
        }
        // if the type has list then it is union type field
        JsonNode unionTypes = null;
        if (fieldNode.has(Field.Prop.ONE_OF)) unionTypes = fieldNode.get(Field.Prop.ONE_OF);
        else if (fieldNode.has(Field.Prop.ANY_OF)) unionTypes = fieldNode.get(Field.Prop.ANY_OF);
        
        if (Objects.nonNull(unionTypes)) {
            builder.dataType("union");
            builder.isPrimitiveType(false);
            parseUnionTypes(schema, builder, unionTypes, schemaName, fieldName, tableMap, fieldTableMap, initialPull);
            return builder.build();
        }
        if (fieldNode.get(Field.Prop.TYPE).isArray()) {  // if union type less than draft 4.
            builder.dataType("union");
            builder.isPrimitiveType(false);
            parseUnionType(builder, fieldNode.get(Field.Prop.TYPE), fieldName);
            return builder.build();
        }

        var type = fieldNode.get(Field.Prop.TYPE).asText();
        builder.dataType(type);
        builder.isPrimitiveType(isPrimitiveType(type));

        log.info("Field name - {} of Type - {} of schema - {}", fieldName, type, schemaName);

        // check weather the field is array
        if (type.contains(Field.Prop.ARRAY)) {
            return parseArrayField(schema, builder, fieldNode, schemaName, fieldName, tableMap, fieldTableMap, initialPull);
        }
        // check weather the field is object
        else if (type.contains(Field.Prop.OBJECT)) {
            // nameSpace of new table is same as Parent table AND name is same as Field.
            var table = parseNestedTable(fieldNode, schemaName, fieldName, tableMap, fieldTableMap, initialPull);
            builder.dataType(type).isPrimitiveType(false).contain(table);
        }
        // or else the field is primitive type
        return builder.build();
    }

    private Table parseNestedTable(JsonNode fieldNode, String nameSpace, String name, Map<String, Table> tableMap, Map<Field, String> fieldTableMap, Boolean initialPull) {
        var id = getString(fieldNode, Table.Prop.JSON_SCHEMA_ID, null);
        var table = constructTable(id, fieldNode, nameSpace, name, tableMap, fieldTableMap, initialPull);
        var type = StringUtil.constructStringEmptySeparator(nameSpace, ".", name);
        if (Boolean.TRUE.equals(initialPull)) {
            table = tableService.addTable(table);
            tableMap.put(table.getId().toString(), table);
        } else {
            tableMap.put(type, table);
        }
        return table;
    }

    private void parseUnionTypes(JsonNode schema, Field.FieldBuilder builder, JsonNode unionNode, String schemaName, String fieldName, 
                                 Map<String, Table> tableMap, Map<Field, String> fieldTableMap, Boolean initialPull) {
        List<Field> unionTypes = new ArrayList<>();
        for (JsonNode jsonNode : unionNode) {
            var field = parseField(schema, jsonNode, Field.builder(), schemaName, fieldName, tableMap, fieldTableMap, initialPull);
            unionTypes.add(field);
        }
        builder.unionTypes(unionTypes);
    }

    private void parseUnionType(Field.FieldBuilder builder, JsonNode fieldTypes, String fieldName) {
        List<Field> unionTypes = new ArrayList<>();
        var types = getArrayFromNode(fieldTypes);
        for (String type : types) {
            unionTypes.add(Field.builder().name(fieldName).dataType(type).build());
        }
        builder.unionTypes(unionTypes);
    }

    private Field parseArrayField(JsonNode schema, Field.FieldBuilder builder, JsonNode fieldNode, String nameSpace, String name, Map<String, Table> tableMap,
                                 Map<Field, String> fieldTableMap, Boolean initialPull) {
        JsonNode itemNode = fieldNode.get(Field.Prop.ITEMS);
        Table table = null;
        if (itemNode.has(Field.Prop.$REF)) {
            var ref = itemNode.get(Field.Prop.$REF).asText();
            builder.jsonSchemaRefId(ref);
            if (ref.startsWith("#/definitions")) {
                var path = ref.split("/");
                fieldNode = schema.get(path[1]).get(path[2]);
                name = path[2];
                var type = StringUtil.constructStringEmptySeparator(nameSpace, ".", name);
                table = constructTable(null, fieldNode, nameSpace, name, tableMap, fieldTableMap, initialPull);
                if (Boolean.TRUE.equals(initialPull)) {
                    table = tableService.addTable(table);
                    tableMap.put(table.getId().toString(), table);
                } else {
                    tableMap.put(table.getName(), table);
                }
                return builder.isPrimitiveType(false).contain(table).items(type).build();
            } else {
                var uri = getUri(ref);
                var path = uri.getPath();
                name = path.substring(path.lastIndexOf("/")+1);
                nameSpace = path.substring(1, path.lastIndexOf("/")).replaceAll("/", ".");
                var type = StringUtil.constructStringEmptySeparator(nameSpace, ".", name);
                var field = builder.items(type).isPrimitiveType(false).build();
                field = tableService.addField(field);
                fieldTableMap.put(field, type);
                return field;
            }
        }

        // check weather the field is enum
        if (itemNode.has(Field.Prop.ENUM)) {
            var field = builder.items(Field.Prop.ENUM)
                    .isPrimitiveType(false)
                    .symbols(getArrayFromNode(itemNode.get(Field.Prop.ENUM)))
                    .build();
            return field;
        }

        JsonNode unionTypes = null;
        if (itemNode.has(Field.Prop.ONE_OF)) unionTypes = itemNode.get(Field.Prop.ONE_OF);
        else if (itemNode.has(Field.Prop.ANY_OF)) unionTypes = itemNode.get(Field.Prop.ANY_OF);
        
        if (Objects.nonNull(unionTypes)) {
            parseUnionTypes(schema, builder, unionTypes, nameSpace, name, tableMap, fieldTableMap, initialPull);
            return builder.items("union").build();
        }
        if (itemNode.isArray()) {  // if union type less than draft 4.
            parseUnionType(builder, itemNode, name);
            return builder.items("union").build();
        }
        var items =itemNode.get(Field.Prop.TYPE).asText();
        // check weather the array field contains object (i.e., check weather it is
        // array of object)
        if (items.equals(Field.Prop.OBJECT)) {
            // if the field is object, then construct another Tabe.
            table = parseNestedTable(fieldNode, nameSpace, name, tableMap, fieldTableMap, initialPull);
            items = getString(itemNode, Table.Prop.TYPE, null);
            builder.isPrimitiveType(false);
        }
        return builder.items(items).contain(table).build();
    }

    private String[] getArrayFromNode(JsonNode arrNode) {
        // List<String> strings = new ArrayList<>();
        // if (arrNode.isArray()) {
        //     for (JsonNode objNode : arrNode) {
        //         strings.add(objNode.asText());
        //     }
        // }
        // return strings.toArray(new String[0]);
        return mapper.convertValue(arrNode, String[].class);
    }

    private String getString(JsonNode node, String type, String defaultValue) {
        return node.has(type) ? node.get(type).asText() : defaultValue;
    }

    // private Integer getInteger(JsonNode node, String type) {
    // return node.has(type) ? node.get(type).asInt() : null;
    // }

    private Boolean getBoolean(JsonNode node, String type, Boolean defaultvalue) {
    return node.has(type) ? node.get(type).asBoolean() : defaultvalue;
    }

    private boolean isPrimitiveType(String name) {
        return PRIMITIVES.contains(name);
    }
}
