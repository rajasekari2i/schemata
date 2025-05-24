package com.opsbeach.connect.schemata.processor.json;

import java.util.List;
import java.util.Objects;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonSchemaGenerator {

    private final List<String> PRIMITIVES;

    private ObjectNode defenitionNode;

    private final String DRAFT = "http://json-schema.org/draft-07/schema#";

    public JsonSchemaGenerator(List<String> primitives) {
        this.PRIMITIVES = primitives;
    }    
    
    public JsonNode generateTableSchema(Table table) {
        var schema = JsonNodeFactory.instance.objectNode();
        defenitionNode = JsonNodeFactory.instance.objectNode();
        generateTableObject(table, schema);
        if (!defenitionNode.isEmpty()) 
            schema.putIfAbsent("definitions", defenitionNode);
        log.info(schema.toPrettyString());
        return schema;
    }

    private void generateTableObject(Table table, ObjectNode tableNode) {
        log.info("Json Schema Table Content generation: "+table.getName());
        addTableProps(tableNode, table);
        var properties = tableNode.putObject(Field.Prop.PROPERTIES);
        getProterties(table.getFields(), properties);
        if (Objects.nonNull(table.getRequiredFields())) {
            constructArrayNode(table.getRequiredFields(), tableNode.putArray(Table.Prop.REQUIRED));
        }
    }

    private void addTableProps(ObjectNode tableNode, Table table) {
        tableNode.put(Table.Prop.$SCHEMA, DRAFT);
        if (Objects.nonNull(table.getJsonSchemaId())) tableNode.put(Table.Prop.JSON_SCHEMA_ID, table.getJsonSchemaId());
        tableNode.put(Table.Prop.TITLE, table.getName());
        tableNode.put(Table.Prop.TYPE, "object");
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getDescription()))) tableNode.put(Table.Prop.DESCRIPTION, table.getDescription());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getOwner()))) tableNode.put(Table.Prop.OWNER, table.getOwner());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getSchemaType()))) tableNode.put(Table.Prop.SCHEMA_TYPE, table.getSchemaType().name());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getChannel()))) tableNode.put(Table.Prop.CHANNEL, table.getChannel());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getDomain()))) tableNode.put(Table.Prop.DOMAIN, table.getDomain());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getEmail()))) tableNode.put(Table.Prop.EMAIL, table.getEmail());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getStatus()))) tableNode.put(Table.Prop.STATUS, table.getStatus());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getComplianceOwner()))) tableNode.put(Table.Prop.COMPLIANCE_OWNER, table.getComplianceOwner());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getSubscribers()))) constructArrayNode(table.getSubscribers(), tableNode.putArray(Table.Prop.SUBSCRIBERS));
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getQualityRuleBase()))) tableNode.put(Table.Prop.QUALITY_RULE_BASE, table.getQualityRuleBase());
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getQualityRuleSql()))) tableNode.put(Table.Prop.QUALITY_RULE_SQL, table.getQualityRuleSql());
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getQualityRuleCel()))) tableNode.put(Table.Prop.QUALITY_RULE_CEL, table.getQualityRuleCel());
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getRequiredFields()))) constructArrayNode(table.getRequiredFields(), tableNode.putArray(Table.Prop.REQUIRED));
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getAdditionalProperties()))) tableNode.put(Table.Prop.ADDITIONAL_PROPERTIES, table.getAdditionalProperties());
    }

    private void getProterties(List<Field> fields, ObjectNode properties) {
        fields.forEach(field -> {
            var fieldNode = properties.putObject(field.getName());
                generateFieldObject(field, fieldNode);
        });
    }

    private void generateFieldObject(Field field, ObjectNode fieldNode) {
        log.info("Json Schema Field Content generation: "+field.getName());
        var dataType = field.getDataType();
        fieldNode.put(Field.Prop.TYPE, dataType);
        if (dataType.equals(Field.Prop.ARRAY)) {
            generateArrayObject(field, fieldNode);
        }
        else if (dataType.equals("union")) {
            // generate union type with "anyOf".
            fieldNode.remove(Field.Prop.TYPE);
            var unionTypes = fieldNode.putArray(Field.Prop.ANY_OF);
            field.getUnionTypes().forEach(fld -> unionTypes.add(JsonNodeFactory.instance.objectNode().put(Field.Prop.TYPE, fld.getDataType())));
        }
        else if (dataType.equals(Field.Prop.ENUM)) {
            fieldNode.put(Field.Prop.TYPE, "string");
            constructArrayNode(field.getSymbols(), fieldNode.putArray(Field.Prop.ENUM));
        }
        else if (Objects.nonNull(field.getContain())) {
            if (Objects.nonNull(field.getJsonSchemaRefId())) {
                fieldNode.removeAll();
                fieldNode.put(Field.Prop.$REF, field.getJsonSchemaRefId());
                if (field.getJsonSchemaRefId().startsWith("#/definitions")) {
                    var defNode = defenitionNode.putObject(field.getJsonSchemaRefId().split("/")[2]);
                    generateTableObject(field.getContain(), defNode);
                }
            } else {
                var typeNode = fieldNode.putObject(Field.Prop.TYPE);
                generateTableObject(field.getContain(), typeNode); 
            }
        }
        fieldNode.put(Field.Prop.DESCRIPTION, field.getDescription());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getIsPii()))) fieldNode.put(Field.Prop.IS_PII, field.getIsPii().toString());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getIsClassified()))) fieldNode.put(Field.Prop.IS_CLASSIFIED, field.getIsClassified().toString());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getDeprecated()))) fieldNode.put(Field.Prop.DEPRECATED, field.getDeprecated().toString());
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getReference()))) fieldNode.put(Field.Prop.REFERENCE, field.getReference());
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getClassificationLevel()))) fieldNode.put(Field.Prop.CLASSIFICATION_LEVEL, field.getClassificationLevel());
    }

    private void generateArrayObject(Field field, ObjectNode fieldNode) {
        var items = field.getItems();
        if (PRIMITIVES.contains(items)) {
            var itemNode = fieldNode.putObject(Field.Prop.ITEMS);
            itemNode.put(Field.Prop.TYPE, items);
        }
        else if (items.equals(Field.Prop.ARRAY)) {
            var itemNode = fieldNode.putObject(Field.Prop.ITEMS);
            itemNode.put(Field.Prop.TYPE, Field.Prop.ARRAY);
            generateArrayObject(field.getArrayField(), itemNode);
        }
        else if (items.equals("union")) {
            var unionTypes = fieldNode.putArray(Field.Prop.ITEMS);
            field.getUnionTypes().forEach(fld -> unionTypes.add(fld.getDataType()));
        }
        else if (items.equals(Field.Prop.ENUM)) {
            var itemNode = fieldNode.putObject(Field.Prop.ITEMS);
            itemNode.put(Field.Prop.TYPE, "string");
            constructArrayNode(field.getSymbols(), itemNode.putArray(Field.Prop.ENUM));
        }
        else if (Objects.nonNull(field.getContain())) {
            if (Objects.nonNull(field.getJsonSchemaRefId())) {
                var itemNode = fieldNode.putObject(Field.Prop.ITEMS);
                itemNode.put(Field.Prop.$REF, field.getJsonSchemaRefId());
                if (field.getJsonSchemaRefId().startsWith("#/definitions")) {
                    var defNode = defenitionNode.putObject(field.getJsonSchemaRefId().split("/")[2]);
                    generateTableObject(field.getContain(), defNode);
                }
            } else {
                var typeNode = fieldNode.putObject(Field.Prop.ITEMS);
                generateTableObject(field.getContain(), typeNode);
            }
        }
    }

    private void constructArrayNode(String[] values, ArrayNode arrayNode) {
        if (values.length > 0) for (String value : values) arrayNode.add(value);
    }
}
