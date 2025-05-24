package com.opsbeach.connect.schemata.processor.avro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AvroSchemaGenerator {

    private final Map<String, Schema.Type> PRIMITIVES;

    public AvroSchemaGenerator(Map<String, Schema.Type> primitives) {
      this.PRIMITIVES = primitives;
    }
    
    public Schema generateTableSchema(Table table) {
        List<Schema.Field> fieldSchemas = new ArrayList<>();
        table.getFields().forEach(field -> {
          Object defval = ObjectUtils.isEmpty(field.getDefaultValue()) ? null : parseDefaultValue(field.getDefaultValue(), field.getDataType());
          var fieldSchema = new Schema.Field(field.getName(), generateFieldSchema(field, field.getDataType()), field.getDescription(), defval);
          fieldSchemas.add(addProps(fieldSchema, field));
        });
        var schema = Schema.createRecord(table.getName(), table.getDescription(), table.getNameSpace(), false, fieldSchemas);
        return addProps(schema, table);
      }
  
      private Schema addProps(Schema schema, Table table) {
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getOwner()))) schema.addProp(Table.Prop.OWNER, table.getOwner());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getSchemaType()))) schema.addProp(Table.Prop.SCHEMA_TYPE, table.getSchemaType().name());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getChannel()))) schema.addProp(Table.Prop.CHANNEL, table.getChannel());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getDomain()))) schema.addProp(Table.Prop.DOMAIN, table.getDomain());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getEmail()))) schema.addProp(Table.Prop.EMAIL, table.getEmail());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getStatus()))) schema.addProp(Table.Prop.STATUS, table.getStatus());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getComplianceOwner()))) schema.addProp(Table.Prop.COMPLIANCE_OWNER, table.getComplianceOwner());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getSubscribers()))) schema.addProp(Table.Prop.SUBSCRIBERS, Arrays.asList(table.getSubscribers()));
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getQualityRuleBase()))) schema.addProp(Table.Prop.QUALITY_RULE_BASE, table.getQualityRuleBase());
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getQualityRuleSql()))) schema.addProp(Table.Prop.QUALITY_RULE_SQL, table.getQualityRuleSql());
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getQualityRuleCel()))) schema.addProp(Table.Prop.QUALITY_RULE_CEL, table.getQualityRuleCel());
        return schema;
      }

      private Schema.Field addProps(Schema.Field fieldSchema, Field field) {
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getIsPii()))) fieldSchema.addProp(Field.Prop.IS_PII, field.getIsPii().toString());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getIsClassified()))) fieldSchema.addProp(Field.Prop.IS_CLASSIFIED, field.getIsClassified().toString());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getDeprecated()))) fieldSchema.addProp(Field.Prop.DEPRECATED, field.getDeprecated().toString());
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getReference()))) fieldSchema.addProp(Field.Prop.REFERENCE, field.getReference());
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getClassificationLevel()))) fieldSchema.addProp(Field.Prop.CLASSIFICATION_LEVEL, field.getClassificationLevel());
        return fieldSchema;
      }
  
      private Schema generateFieldSchema(Field field, String dataType) {
        log.info("Processing field --"+field.getName()+" of schema --"+field.getSchema());
        if (Boolean.TRUE.equals(field.getIsPrimitiveType())) {
          return Schema.create(Schema.Type.valueOf(PRIMITIVES.get(dataType).name()));
        } 
        else {
          if (dataType.equalsIgnoreCase(Schema.Type.ARRAY.name())) {
            return generateArraySchema(field);
          }
          else if (dataType.equalsIgnoreCase(Schema.Type.MAP.name())) {
            return generateMapSchema(field);
          }
          else if (dataType.equalsIgnoreCase(Schema.Type.ENUM.name())) {
            return generateEnumSchema(field);
          }
          else if (dataType.equalsIgnoreCase(Schema.Type.UNION.name())) {
            return Schema.createUnion(field.getUnionTypes().stream().map(type -> generateFieldSchema(type, type.getDataType())).toList());
          }
          else if (dataType.equalsIgnoreCase(Schema.Type.FIXED.name())) {
            return Schema.createFixed(field.getName(), field.getDescription(), field.getSchema(), field.getSize());
          }
          else {
            return generateTableSchema(field.getContain());
          }
        }
      }

      public boolean isPrimitiveType(String name) {
        return PRIMITIVES.containsKey(name);
      }

      public Schema generateEnumSchema(Field field) {
        var defaultVal = ObjectUtils.isEmpty(field.getDefaultValue()) ? null : field.getDefaultValue();
        return Schema.createEnum(field.getName(), field.getDescription(), field.getSchema(), Arrays.asList(field.getSymbols()), defaultVal);
      }
  
      private Schema generateArraySchema(Field field) {
        var items = field.getItems();
        Schema itemSchema = null;  // (itemSchema is same as "elementType" of array schema)
        if (Boolean.TRUE.equals(isPrimitiveType(items))) {
          itemSchema = Schema.create(Schema.Type.valueOf(PRIMITIVES.get(items).name()));
        } 
        else {
          if (items.equalsIgnoreCase(Schema.Type.ARRAY.name())) {
            itemSchema = generateArraySchema(field.getArrayField());
          }
          else if (items.equalsIgnoreCase(Schema.Type.MAP.name())) {
            itemSchema = generateMapSchema(field.getMapField());
          }
          else if (items.equalsIgnoreCase(Schema.Type.ENUM.name())) {
            itemSchema = generateEnumSchema(field);
          }
          else if (items.equalsIgnoreCase(Schema.Type.UNION.name())) {
            itemSchema = Schema.createUnion(field.getUnionTypes().stream().map(type -> generateFieldSchema(type, type.getDataType())).toList());
          }
          else if (items.equalsIgnoreCase(Schema.Type.FIXED.name())) {
            itemSchema = Schema.createFixed(field.getName(), field.getDescription(), field.getSchema(), field.getSize());
          }
          else {
            itemSchema = generateTableSchema(field.getContain());
          }
        }
        return Schema.createArray(itemSchema);
      }
  
      private Schema generateMapSchema(Field field) {
        var values = field.getValues();
        Schema valueSchema = null;  // (valueSchema is same as "valueType" of map schema)
        if (Boolean.TRUE.equals(isPrimitiveType(values))) {
          valueSchema = Schema.create(Schema.Type.valueOf(PRIMITIVES.get(values).name()));
        } 
        else {
          if (values.equalsIgnoreCase(Schema.Type.ARRAY.name())) {
            valueSchema = generateArraySchema(field.getArrayField());
          }
          else if (values.equalsIgnoreCase(Schema.Type.MAP.name())) {
            valueSchema = generateMapSchema(field.getMapField());
          }
          else if (values.equalsIgnoreCase(Schema.Type.ENUM.name())) {
            valueSchema = generateEnumSchema(field);
          }
          else if (values.equalsIgnoreCase(Schema.Type.UNION.name())) {
            valueSchema = Schema.createUnion(field.getUnionTypes().stream().map(type -> generateFieldSchema(type, type.getDataType())).toList());
          }
          else if (values.equalsIgnoreCase(Schema.Type.FIXED.name())) {
            valueSchema = Schema.createFixed(field.getName(), field.getDescription(), field.getSchema(), field.getSize());
          }
          else {
            valueSchema = generateTableSchema(field.getContain());
          }
        }
        return Schema.createMap(valueSchema);
      }
  
      private Object parseDefaultValue(String defaultValue, String dataType) {
        if (dataType.equalsIgnoreCase(Schema.Type.INT.name())) {
          return Integer.parseInt(defaultValue);
        }
        if (dataType.equalsIgnoreCase(Schema.Type.FLOAT.name())) {
          return Float.parseFloat(defaultValue);
        }
        if (dataType.equalsIgnoreCase(Schema.Type.LONG.name())) {
          return Long.parseLong(defaultValue);
        }
        if (dataType.equalsIgnoreCase(Schema.Type.DOUBLE.name())) {
          return Double.parseDouble(defaultValue);
        }
        // if (dataType.equalsIgnoreCase(Schema.Type.ENUM.name())) {
        //   return null;
        // }
        return defaultValue;
      }
}
