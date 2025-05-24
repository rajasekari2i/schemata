package com.opsbeach.connect.schemata.processor.protobuf;

import com.google.protobuf.Descriptors;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.EventType;
import com.opsbeach.connect.schemata.enums.SchemaType;
import com.opsbeach.sharedlib.security.SecurityUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.schemata.schema.SchemataBuilder;

public class ProtoProcessor {

  private static final Set<String> INCLUDED_PRIMITIVE_TYPES = Set.of("google.protobuf.Timestamp");
  
  public List<Table> parse(List<Descriptors.Descriptor> descriptors, Map<Field, String> fieldTableMap) {
    return descriptors
        .stream()
            .filter(this::isGoogleDefaultProto)
            .filter(this::isSchemataDefaultProto)
        .map(descriptor -> parseSingleSchema(descriptor, fieldTableMap))
        .toList();
  }

  public Table parseSingleSchema(Descriptors.Descriptor descriptor, Map<Field, String> fieldTableMap) {
    String schemaName = descriptor.getFullName();
    // Extract all the metadata for the fieldList
    var fieldList = extractFields(descriptor.getFields(), schemaName, fieldTableMap);
    return extractSchema(descriptor, schemaName, fieldList);
  }

  public Table extractSchema(Descriptors.Descriptor descriptorType, String schema, List<Field> fieldList) {
    var builder = Table.builder().name(descriptorType.getName()).nameSpace(schema.substring(0, schema.lastIndexOf(".")))
                       .clientId(SecurityUtil.getClientId()).fields(fieldList);
    for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : descriptorType.getOptions().getAllFields().entrySet()) {

      switch (entry.getKey().getName()) {
        case "message_core" -> {
          SchemataBuilder.CoreMetadata coreMetadata = (SchemataBuilder.CoreMetadata) entry.getValue();
          builder.description(coreMetadata.getDescription());
          // builder.comment(coreMetadata.getComment());
          // builder.seeAlso(coreMetadata.getSeeAlso());
          // builder.reference(coreMetadata.getReference());
        }
        case "owner" -> builder.owner(Objects.toString(entry.getValue(), null));
        case "domain" -> builder.domain(Objects.toString(entry.getValue(), null));
        case "schema_type" -> builder.schemaType(SchemaType.get(entry.getValue().toString()));
        case "event_type" -> builder.eventType(EventType.get(entry.getValue().toString()));
        case "status" -> builder.status(Objects.toString(entry.getValue(), null));
        // case "team_channel" -> builder.teamChannel(Objects.toString(entry.getValue(), ""));
        // case "alert_channel" -> builder.alertChannel(Objects.toString(entry.getValue(), ""));
        case "compliance_owner" -> builder.complianceOwner(Objects.toString(entry.getValue(), null));
        // case "compliance_channel" -> builder.complianceChannel(Objects.toString(entry.getValue(), ""));
        case "channel" -> builder.channel(Objects.toString(entry.getValue(), null));
        case "email" -> builder.email(Objects.toString(entry.getValue(), null));
        case "quality_rule_base" -> builder.email(Objects.toString(entry.getValue(), null));
        // case "quality_rule_sql" -> builder.email(Objects.toString(entry.getValue(), null));
        // case "quality_rule_cel" -> builder.email(Objects.toString(entry.getValue(), null));
        case "subscribers" -> builder.subscribers(extractSubscribers(entry.getValue()));
      }
    }
    return builder.build();
  }

  private String[] extractSubscribers(Object value) {
    SchemataBuilder.Subscribers subscribers = (SchemataBuilder.Subscribers) value;
    return subscribers.getNameList().stream().toList().toArray(new String[0]);
  }

  public List<Field> extractFields(List<Descriptors.FieldDescriptor> fieldDescriptorList, String schema, Map<Field, String> fieldTableMap) {
    List<Field> fields = new ArrayList<>();
    AtomicInteger rowNumber = new AtomicInteger(1);
    for (Descriptors.FieldDescriptor entry : fieldDescriptorList) {
      String type = entry.getType() == Descriptors.FieldDescriptor.Type.MESSAGE ? entry.getMessageType().getFullName()
          : entry.getType().name();
      var builder = Field.builder().name(entry.getName()).schema(schema).dataType(type).isPrimitiveType(isPrimitiveType(entry.getType(), type));
      for (Map.Entry<Descriptors.FieldDescriptor, Object> fieldEntry : entry.getOptions().getAllFields().entrySet()) {
        switch (fieldEntry.getKey().getName()) {
          case "field_core" -> {
            SchemataBuilder.CoreMetadata coreMetadata = (SchemataBuilder.CoreMetadata) fieldEntry.getValue();
            builder.description(coreMetadata.getDescription());
            // builder.comment(coreMetadata.getComment());
            // builder.seeAlso(coreMetadata.getSeeAlso());
            // builder.reference(coreMetadata.getReference());
          }
          case "is_classified" -> builder.isClassified(Boolean.parseBoolean(fieldEntry.getValue().toString()));
          case "is_pii" -> builder.isPii(Boolean.parseBoolean(fieldEntry.getValue().toString()));
          case "depricated" -> builder.deprecated(Boolean.parseBoolean(fieldEntry.getValue().toString()));
          // case "classification_level" -> builder.classificationLevel(Objects.toString(fieldEntry.getValue(), ""));
          // case "product_type" -> builder.productType(Objects.toString(fieldEntry.getValue(), ""));
          case "is_primary_key" -> builder.isPrimaryKey(Boolean.parseBoolean(fieldEntry.getValue().toString()));
        }
      }
      var field = builder.rowNumber(rowNumber.getAndIncrement()).build();
      if (type == Descriptors.FieldDescriptor.Type.ENUM.name()) {
        field.setEnumFilePath("main/schema/"+entry.getEnumType().getFile().getFullName());
        field.setEnumName(entry.getEnumType().getName());
        field.setEnumPackage(entry.getEnumType().getFile().getPackage());
        field.setSymbols(entry.getEnumType().getValues().stream().map(v -> v.getName()).toList().toArray(new String[0]));
      }
      if (field.getIsPrimitiveType().equals(Boolean.FALSE) && entry.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
        fieldTableMap.put(field, type);
      }
      fields.add(field);
    }
    return fields;
  }

  private boolean isGoogleDefaultProto(Descriptors.Descriptor descriptor) {
    return !descriptor.getFullName().startsWith("google.protobuf");
  }
  private boolean isSchemataDefaultProto(Descriptors.Descriptor descriptor) {
    return !descriptor.getFullName().startsWith("org.schemata");
  }

  private boolean isPrimitiveType(Descriptors.FieldDescriptor.Type type, String typeName) {
    return type != Descriptors.FieldDescriptor.Type.MESSAGE || INCLUDED_PRIMITIVE_TYPES.contains(typeName);
  }
}
