package com.opsbeach.connect.schemata.processor.protobuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.service.ModelService;
import com.opsbeach.connect.github.service.SchemaFileAuditService;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.utils.StringUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProtobufFileGenerator {

    private final SchemaFileAuditService schemaFileAuditService;
    private final ModelService modelService;
    private final TableService tableService;
    
    public Map<SchemaFileAudit, String> getSchema(Long tableId) {
        var schemaFileAudit = schemaFileAuditService.getByModelNodeId(tableId).get(0); // protobuf schema contain only one file for one table.
        var models = modelService.findBySchemaFileAudit(schemaFileAudit.getId());
        var tableIds = models.stream().map(Model::getNodeId).toList();
        var tables = tableService.findAllById(tableIds);
        Set<String> imports = new HashSet<>();
        imports.add("\"schemata/protobuf/schemata.proto\"");
        List<String> messages = new ArrayList<>(tableIds.size());
        Map<String, String> enums = new HashMap<>();
        tables.forEach(table -> {
            System.out.println("processing table "+table.getName());
            table = schemaFileAuditService.filterFields(table);
            createMessage(table, imports, schemaFileAudit.getPath(), messages, enums);
        });
        StringBuilder content = new StringBuilder("syntax = \"proto3\";\n");
        content.append("package ").append(tables.get(0).getNameSpace()).append(";\n\n");
        imports.forEach(i -> content.append("import ").append(i).append(";\n"));
        content.append("\n");
        enums.values().forEach(message -> content.append(message).append("\n"));
        messages.forEach(message -> content.append(message).append("\n"));
        System.out.println(content.toString());
        return Map.of(schemaFileAudit, content.toString());
    }

    private void createMessage(Table table, Set<String> imports, String filePath, List<String> messages, Map<String, String> enums) {
        StringBuilder message = new StringBuilder("message ");
        message.append(table.getName()).append(" {\n");
        message.append("  option(org.schemata.schema.message_core).description = \"").append(table.getDescription()).append("\";\n");
        message.append("  option(org.schemata.schema.owner) = \"").append(table.getOwner()).append("\";\n");
        message.append("  option(org.schemata.schema.domain) = \"").append(table.getDomain()).append("\";\n");
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getSchemaType())))
            message.append("  option(org.schemata.schema.schema_type) = ").append(table.getSchemaType()).append(";\n");
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getChannel())))
            message.append("  option(org.schemata.schema.channel) = \"").append(table.getChannel()).append("\";\n");
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getEmail())))
            message.append("  option(org.schemata.schema.email) = \"").append(table.getEmail()).append("\";\n");
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getStatus())))
            message.append("  option(org.schemata.schema.status) = \"").append(table.getStatus()).append("\";\n");
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getComplianceOwner())))
            message.append("  option(org.schemata.schema.compliance_owner) = \"").append(table.getComplianceOwner()).append("\";\n");
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getQualityRuleBase())))
            message.append("  option(org.schemata.schema.quality_rule_base) = \"").append(table.getQualityRuleBase()).append("\";\n");
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getQualityRuleSql())))
        //     message.append("  option(org.schemata.schema.quality_rule_sql) = \"").append(table.getQualityRuleSql()).append("\";\n");
        // if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getQualityRuleCel())))
        //     message.append("  option(org.schemata.schema.quality_rule_cel) = \"").append(table.getQualityRuleCel()).append("\";\n");
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getSubscribers()))) {
            message.append("  option(org.schemata.schema.subscribers) = {\n");
            int n = table.getSubscribers().length;
            for (int i=0; i<n; i++) {
                message.append("    name: \"").append(table.getSubscribers()[i]).append("\"");
                if (i < (n-1)) message.append(",\n");
            }
            message.append("\n  };\n\n");
        }
        for (Field field : table.getFields()) {
            System.out.println("Processing field "+field.getName()+" of table "+table.getName());
            var dataType = addImportPath(imports, field, filePath, enums);
            message.append("  ").append(dataType).append(" ").append(field.getName()).append(" = ").append(field.getRowNumber()).append("\n");
            message.append("  [").append("(org.schemata.schema.field_core).description = \"").append(field.getDescription()).append("\",\n");
            message.append("   (org.schemata.schema.is_pii) = ").append(field.getIsPii()).append(",");
            message.append("(org.schemata.schema.is_classified) = ").append(field.getIsClassified()).append(",");
            message.append("(org.schemata.schema.depricated) = ").append(field.getDeprecated()).append("];\n\n");
        }
        messages.add(message.append("}\n").toString());
    }

    private String getDataType(String dataType) {
        return switch (dataType) {
            case "DOUBLE", "FLOAT", "INT32", "INT64", "UINT32", "UINT64", "SINT32", "SINT64", "FIXED32",
                 "FIXED64", "SFIXED32", "SFIXED64", "BOOL", "STRING", "BYTES" -> dataType.toLowerCase();
            default -> dataType;
        };
    }

    private boolean isPrimitiveType(String dataType) {
        return switch (dataType) {
            case "double", "float", "int32", "int64", "uint32", 
                 "uint64", "sint32", "sint64", "fixed32", "fixed64", "sfixed32", "sfixed64", "bool", "string", "bytes" -> true;
            default -> false;
        };
    }

    private String addImportPath(Set<String> imports, Field field, String filePath, Map<String, String> enums) {
        var dataType = getDataType(field.getDataType());
        var isSchemataType = field.getDataType().startsWith("google.protobuf") || field.getDataType().startsWith("org.schemata");
        System.out.println(dataType+"   "+isSchemataType);
        if (isPrimitiveType(dataType) == false && !field.getDataType().equals("ENUM") && !isSchemataType) {
            var fullName = field.getDataType();
            var nameSpace = fullName.substring(0, fullName.lastIndexOf("."));
            var name = fullName.substring(fullName.lastIndexOf(".")+1);
            var schemaFileAudits = schemaFileAuditService.getByModelNodeId(tableService.findByNameAndNameSpace(name, nameSpace).getId());
            if (filePath.equals(schemaFileAudits.get(0).getPath())) return name;
            imports.add(getImportPath(filePath, schemaFileAudits.get(0).getPath()));
            return name;
        }
        if (dataType.equals("ENUM")) {
            dataType = field.getEnumPackage()+"."+field.getEnumName();
            if (field.getEnumFilePath().contains("schemata/protobuf/schemata.proto") ||
                           field.getEnumFilePath().contains("google/protobuf/")) return dataType;
            var index = filePath.indexOf("/main/schema/");
            System.out.println(filePath+"  index  "+index);
            var path = filePath.substring(index+1);
            if (path.equals(field.getEnumFilePath())) {
                if (Boolean.FALSE.equals(enums.containsKey(field.getEnumName()))) {
                    StringBuilder message = new StringBuilder("enum ");
                    message.append(field.getEnumName()).append(" {\n");
                    int n = field.getSymbols().length;
                    for (int i=0; i<n; i++) {
                        message.append("  ").append(field.getSymbols()[i]).append(" = ").append(i).append(";");
                        if (i < (n-1)) message.append("\n");
                    }
                    message.append("\n}\n");
                    enums.put(field.getEnumName(), message.toString());
                }
            } else {
                imports.add(getImportPath(path, field.getEnumFilePath()));
            }
        }
        if (dataType.equalsIgnoreCase("google.protobuf.Timestamp"))
            imports.add("\"google/protobuf/timestamp.proto\"");
        return dataType;
    }

    private String getImportPath(String workingFilePath, String referenceFilePath) {
        if (StringUtil.isEmpty(workingFilePath) || StringUtil.isEmpty(referenceFilePath)) return null;
        int n = 0;
        if (workingFilePath.length() > referenceFilePath.length())
            n = workingFilePath.length();
        else
            n = referenceFilePath.length();
        int i = 0;
        int j = 0;
        while (i < n) {
            if (workingFilePath.charAt(i) == referenceFilePath.charAt(i)) {
                if (workingFilePath.charAt(i) == '/') j = ++i;
                else i++;
            }
            else
                break;
        }
        return '"'+referenceFilePath.substring(j)+'"';
    }
}
