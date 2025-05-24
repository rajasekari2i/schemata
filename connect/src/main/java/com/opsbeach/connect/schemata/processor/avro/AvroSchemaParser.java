package com.opsbeach.connect.schemata.processor.avro;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.EventType;
import com.opsbeach.connect.schemata.enums.SchemaType;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.utils.JsonUtil;

public class AvroSchemaParser {

    private final TableService tableService;

    private final Map<String, Schema.Type> PRIMITIVES;

    public AvroSchemaParser(TableService tableService, Map<String, Schema.Type> primitives) {
        this.tableService = tableService;
        this.PRIMITIVES = primitives;
    }
    
    public Table parseSchema(Schema avroSchema, Map<String, Table> tableMap, Boolean initialPull) {
        List<Field> fields = new ArrayList<>();
        var avroFields = avroSchema.getFields();
        int rowNumber = 1;
        for (Schema.Field avroField : avroFields) {
          fields.add(parseField(avroSchema.getFullName(), avroField, rowNumber, tableMap, initialPull));
          rowNumber++;
        }
        return parseTable(avroSchema, fields);
      } 
    
      private Table parseTable(Schema schema,List<Field> fields) {
        return Table.builder().name(schema.getName())
                                .nameSpace(schema.getNamespace())
                                .clientId(SecurityUtil.getClientId())
                                .type(schema.getType().name())
                                .fields(fields)
                                .description(schema.getDoc())
                                // .comment(schema.getProp(Table.Prop.COMMENT))
                                // .seeAlso(schema.getProp(Table.Prop.SEE_ALSO))
                                // .reference(schema.getProp(Table.Prop.REFERENCE))
                                .owner(schema.getProp(Table.Prop.OWNER))
                                .domain(schema.getProp(Table.Prop.DOMAIN))
                                .status(ObjectUtils.isEmpty(schema.getProp(Table.Prop.STATUS)) ? "Active" : schema.getProp(Table.Prop.STATUS))
                                .schemaType(handleEmptyTableType(schema))
                                .eventType(handleEmptyEventType(schema))
                                .channel(schema.getProp(Table.Prop.CHANNEL))
                                .subscribers(handleSubscribers(schema))
                                .email(schema.getProp(Table.Prop.EMAIL))
                                // .teamChannel(schema.getProp(Table.Prop.TEAM_CHANNEL))
                                // .alertChannel(schema.getProp(Table.Prop.ALERT_CHANNEL))
                                .complianceOwner(schema.getProp(Table.Prop.COMPLIANCE_OWNER))
                                // .complianceChannel(schema.getProp(Table.Prop.COMPLIANCE_CHANNEL))
                                .qualityRuleBase(schema.getProp(Table.Prop.QUALITY_RULE_BASE))
                                .qualityRuleSql(schema.getProp(Table.Prop.QUALITY_RULE_SQL))
                                .qualityRuleCel(schema.getProp(Table.Prop.QUALITY_RULE_CEL))
                                .build();
      }
  
      private Table parseNestedTable(Schema schema, Map<String, Table> tableMap, Boolean initialPull) {
        if (Boolean.TRUE.equals(initialPull)) {
          // check the table present in DB already, find it by name and nameSpace of schema.
          var table = tableService.findByNameAndNameSpace(schema.getName(), schema.getNamespace());
          if (ObjectUtils.isEmpty(table)) {
            // If table is not present in DB then parse it and save.
            table = parseSchema(schema, tableMap, initialPull);
            table = tableService.addTable(table);
          }
          else { 
            // Need to parse the table if it present in DB, because we need nested table Id of this table.
            parseSchema(schema, tableMap, initialPull); 
          }
          tableMap.putIfAbsent(table.getId().toString(), table);
          return table;
        }
        var table = parseSchema(schema, tableMap, initialPull);
        tableMap.put(table.getName(), table);
        return table;
      }
  
      private Field parseField(String schemaName, Schema.Field avroField, int rowNumber, Map<String, Table> tableMap, Boolean initialPull) {
        var builder = Field.builder();
        String dataType = avroField.schema().getType().getName();
        var val = isPrimitiveType(dataType) ? avroField.defaultVal() : avroField.schema().getObjectProps().get(Field.Prop.DEFAULT);
        String defaultVal = ObjectUtils.isEmpty(val) ? null : val.toString();
        
        // if the field is RECORD then create new table and add dataType as table name
        if (dataType.equalsIgnoreCase(Schema.Type.RECORD.name())) {
          var table = parseNestedTable(avroField.schema(), tableMap, initialPull);
          dataType = avroField.schema().getFullName();
          builder.contain(table);
        }
        // if the field is ENUM then add symbols(enum values).
        else if (dataType.equalsIgnoreCase(Schema.Type.ENUM.name())) {
          builder.symbols(avroField.schema().getEnumSymbols().toArray(new String[0]));
          defaultVal = avroField.schema().getEnumDefault();
        }
        // if the field is MAP then parse it.
        else if (dataType.equalsIgnoreCase(Schema.Type.MAP.name())) {
          builder = parseMapField(avroField.name(), avroField.schema(), builder, tableMap, initialPull);
        }
        // if the field is ARRAY then parse it.
        else if (dataType.equalsIgnoreCase(Schema.Type.ARRAY.name())) {
          builder = parseArrayField(avroField.name(), avroField.schema(), builder, tableMap, initialPull);
        }
        // if the field is UNION then parse it.
        else if (dataType.equalsIgnoreCase(Schema.Type.UNION.name())) {
          builder = parseUnionFields(avroField.name(), avroField.schema().getTypes(), builder, tableMap, initialPull);
        }
        else if (dataType.equalsIgnoreCase(Schema.Type.FIXED.name())) {
          builder.size(avroField.schema().getFixedSize());
        }    
        return builder.schema(schemaName)
                      .name(avroField.name())
                      .rowNumber(rowNumber)
                      .dataType(dataType)
                      .isPrimitiveType(isPrimitiveType(dataType))
                      .description(avroField.doc())
                      .defaultValue(defaultVal)
                      // .comment(avroField.getProp(Field.Prop.COMMENT))
                      // .seeAlso(avroField.getProp(Field.Prop.SEE_ALSO))
                      // .reference(avroField.getProp(Field.Prop.REFERENCE))
                      .isClassified(Boolean.parseBoolean(avroField.getProp(Field.Prop.IS_CLASSIFIED)))
                      .isPrimaryKey(Boolean.parseBoolean(avroField.getProp(Field.Prop.IS_PRIMARY_KEY)))
                      .isPii(Boolean.parseBoolean(avroField.getProp(Field.Prop.IS_PII)))
                      .deprecated(Boolean.parseBoolean(avroField.getProp(Field.Prop.DEPRECATED)))
                      // .classificationLevel(avroField.getProp(Field.Prop.CLASSIFICATION_LEVEL))
                      // .productType(avroField.getProp(Field.Prop.PRODUCT_TYPE))
                      .build();
      }
  
      private Field.FieldBuilder parseUnionFields(String name, List<Schema> schemas, Field.FieldBuilder builder, Map<String, Table> tableMap, Boolean initialPull) {
          List<Field> unionTypes = new ArrayList<>();
  
          // UNION field hava more than one DATA TYPE so we need to create new FIELD foreach and every DATA TYPE and 
          // add these as new list of relation fields to UNION FIELD object.
          schemas.forEach(type -> unionTypes.add(parseUnionField(name, type, tableMap, initialPull)));
          builder.unionTypes(unionTypes);
          return builder;
      }
  
      // This method is for creating new FIELD object for every UNION FIELDS
      private Field parseUnionField(String name, Schema schema, Map<String, Table> tableMap, Boolean initialPull) {
        var builder = Field.builder();
        // Need to check the union field belongs to which type then parse the respective type
        String dataType = schema.getType().getName();
        if (dataType.equalsIgnoreCase(Schema.Type.RECORD.name())) {
          var table = parseNestedTable(schema, tableMap, initialPull);
          dataType = schema.getFullName();
          builder.contain(table);
        }
        else if (dataType.equalsIgnoreCase(Schema.Type.ARRAY.name())) {
          // We don't know array contains nested field, so pass current builder add values and return
          builder = parseArrayField(name, schema, builder, tableMap, initialPull);
        }
        else if (dataType.equalsIgnoreCase(Schema.Type.MAP.name())) {
          // We don't know map contains nested field, so pass current builder add values and return
          builder = parseMapField(name, schema, builder, tableMap, initialPull);
        }
        else if (dataType.equalsIgnoreCase(Schema.Type.UNION.name())) {
          builder = parseUnionFields(name, schema.getTypes(), builder, tableMap, initialPull);
        }
        else if (dataType.equalsIgnoreCase(Schema.Type.ENUM.name())) {
          builder.symbols(schema.getEnumSymbols().toArray(new String[0]));
        }
        else if (dataType.equalsIgnoreCase(Schema.Type.FIXED.name())) {
          builder.size(schema.getFixedSize());
        }
        return builder.name(name).dataType(dataType)
                              .isPrimitiveType(isPrimitiveType(dataType))
                              .build();
      }
  
      // This method for parsing ARRAY and NESTED ARRAY fields of any type.
      private Field.FieldBuilder parseArrayField(String fieldName, Schema schema, Field.FieldBuilder builder, Map<String, Table> tableMap, Boolean initialPull) {
        var dataType = schema.getType().getName();
        var items = schema.getElementType().getType().getName();
        if (items.equalsIgnoreCase(Schema.Type.RECORD.name())) {
          var table = parseNestedTable(schema.getElementType(), tableMap, initialPull);
          items = schema.getElementType().getFullName();
          builder.contain(table);
        } 
        else if (items.equalsIgnoreCase(Schema.Type.ARRAY.name())) {
          var arrayBuilder = parseArrayField(fieldName, schema.getElementType(), Field.builder(), tableMap, initialPull);
          builder.arrayField(arrayBuilder.build());
        }
        else if (items.equalsIgnoreCase(Schema.Type.MAP.name())) {
          var mapBuilder = parseMapField(fieldName, schema.getElementType(), Field.builder(), tableMap, initialPull);
          builder.mapField(mapBuilder.build());
        }
        else if (items.equalsIgnoreCase(Schema.Type.UNION.name())) {
          builder = parseUnionFields(fieldName, schema.getElementType().getTypes(), builder, tableMap, initialPull);
        }
        else if (items.equalsIgnoreCase(Schema.Type.ENUM.name())) {
          builder.symbols(schema.getElementType().getEnumSymbols().toArray(new String[0]));
        }
        else if (items.equalsIgnoreCase(Schema.Type.FIXED.name())) {
          builder.size(schema.getElementType().getFixedSize());
        }
        builder.name(fieldName).dataType(dataType).items(items);
        return builder;
      }
  
      // This method for parsing MAP and NESTED MAP fields of any type. 
      // (NOTE: the key of avro map should be always string.)
      private Field.FieldBuilder parseMapField(String fieldName, Schema schema, Field.FieldBuilder builder, Map<String, Table> tableMap, Boolean initialPull) {
        var dataType = schema.getType().getName();
        var values = schema.getValueType().getType().getName();
        if (values.equalsIgnoreCase(Schema.Type.RECORD.name())) {
          var table = parseNestedTable(schema.getValueType(), tableMap, initialPull);
          values = schema.getValueType().getFullName();
          builder.contain(table);
        } 
        else if (values.equalsIgnoreCase(Schema.Type.ARRAY.name())) {
          var arrayBuilder = parseArrayField(fieldName, schema.getValueType(), Field.builder(), tableMap, initialPull);
          builder.arrayField(arrayBuilder.build());
        }
        else if (values.equalsIgnoreCase(Schema.Type.MAP.name())) {
          var mapBuilder = parseMapField(fieldName, schema.getValueType(), Field.builder(), tableMap, initialPull);
          builder.mapField(mapBuilder.build());
        }
        else if (values.equalsIgnoreCase(Schema.Type.UNION.name())) {
          builder = parseUnionFields(fieldName, schema.getValueType().getTypes(), builder, tableMap, initialPull);
        }
        else if (values.equalsIgnoreCase(Schema.Type.ENUM.name())) {
          builder.symbols(schema.getValueType().getEnumSymbols().toArray(new String[0]));
        }
        else if (values.equalsIgnoreCase(Schema.Type.FIXED.name())) {
          builder.size(schema.getValueType().getFixedSize());
        }
        builder.name(fieldName).dataType(dataType).values(values);
        return builder;
      }
  
      private EventType handleEmptyEventType(Schema schema) {
        return schema.getProp(Table.Prop.EVENT_TYPE) == null ? EventType.NONE
            : EventType.get(schema.getProp(Table.Prop.EVENT_TYPE));
      }
  
      private SchemaType handleEmptyTableType(Schema schema) {
        return schema.getProp(Table.Prop.SCHEMA_TYPE) == null ? SchemaType.UNKNOWN
            : SchemaType.get(schema.getProp(Table.Prop.SCHEMA_TYPE));
      }

      private String[] handleSubscribers(Schema schema) {
        if (schema.getObjectProp(Table.Prop.SUBSCRIBERS) == null) return null;
        var subscribers = JsonUtil.jsonArrayToObjectList(JsonUtil.convertObjectIntoJson(schema.getObjectProp(Table.Prop.SUBSCRIBERS)), String.class);
        return subscribers.toArray(new String[0]);
      }
  
      public boolean isPrimitiveType(String name) {
        return PRIMITIVES.containsKey(name);
      }
}
