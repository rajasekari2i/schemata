package com.opsbeach.connect.schemata.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.service.ModelService;
import com.opsbeach.connect.github.service.PullRequestService;
import com.opsbeach.connect.github.service.SchemaFileAuditService;
import com.opsbeach.connect.schemata.dto.SchemaValidationDto;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.utils.StringUtil;

import static com.opsbeach.connect.schemata.validate.SchemaTrigger.*;

@Component
public class SchemaValidator implements Function<Table, Result>, Validator<Table> {

  @Override
  public Result apply(Table schema) {

    List<String> errors = new ArrayList<>();
    for (Map.Entry<Rules, SchemaTrigger> ruleTrigger : schemaValidatorMap().entrySet()) {
      var result = test(ruleTrigger.getKey(), ruleTrigger.getValue(), schema);
      result.ifPresent(errors::add);
    }

    return errors.size() == 0 ? new Result(schema.getId(), schema.getName(), Status.SUCCESS, errors) 
                              : new Result(schema.getId(), schema.getName(), Status.ERROR, errors);
  }

  private Map<Rules, SchemaTrigger> schemaValidatorMap() {
    return Map.of(Rules.SCHEMA_DESCRIPTION_EMPTY, isDescriptionEmpty, Rules.SCHEMA_OWNER_EMPTY, isOwnerEmpty,
                  Rules.SCHEMA_DOMAIN_EMPTY, isDomainEmpty);
  }

  @Value("${github.construct-file-path}")
  private String githubFilePath;

  private final TableService tableService;
  private final SchemaFileAuditService schemaFileAuditService;
  private final ResponseMessage responseMessage;
  private final FieldValidator fieldValidator;
  private final PullRequestService pullRequestService;
  private final ModelService modelService;

  public SchemaValidator(@Lazy TableService tableService, @Lazy SchemaFileAuditService schemaFileAuditService, ResponseMessage responseMessage,
                         FieldValidator fieldValidator, PullRequestService pullRequestService, ModelService modelService) {
    this.tableService = tableService;
    this.schemaFileAuditService = schemaFileAuditService;
    this.responseMessage = responseMessage;
    this.fieldValidator = fieldValidator;
    this.pullRequestService = pullRequestService;
    this.modelService = modelService;
  }

    public SchemaValidationDto schemaCompare(Map<String, Table> pathTableMap, ClientRepo clientRepo, Long prId) {
        List<String> errors = new ArrayList<>();
        Map<String, Map<String, List<String>>> errorMap = new HashMap<>();
        ObjectNode changes = JsonNodeFactory.instance.objectNode();
        Set<String> tableFullNames = new HashSet<>();
        for (Map.Entry<String, Table> pathTable : pathTableMap.entrySet()) {
            Map<String, List<String>> tableErrorMap = new HashMap<>();
            var filePath = pathTable.getKey();
            var githubPath = githubFilePath.replace("{repoFullName}", clientRepo.getFullName())
                                           .replace("{branch}", clientRepo.getDefaultBranch())
                                           .replace("{filePath}", filePath);
            ObjectNode filechange = changes.putObject(githubPath);
            var schemaFileAudit = schemaFileAuditService.getSchemaFileAudit(githubPath, null);  //send prId is null then only it will fetch file details of main branch
            if (ObjectUtils.isEmpty(schemaFileAudit)) {
                validateNewSchema(pathTable.getValue(), filechange, tableFullNames, filePath, prId, tableErrorMap, errors);
                continue; // new schema, no need of any validations.
            }
            var tableOld = tableService.getModel(schemaFileAudit.getRootNodeId());
            var tableNew = pathTable.getValue();
            validateSchema(tableOld, tableNew, filechange, tableFullNames, filePath, prId, tableErrorMap);
            if (tableErrorMap.isEmpty()) continue;
            errorMap.put(filePath, tableErrorMap);
        }
        checkSchemaChangesInOtherFiles(changes, errors, tableFullNames, clientRepo);
        return constructMessage(errors, changes, errorMap);
    }

    private SchemaValidationDto constructMessage(List<String> errors, ObjectNode changes, Map<String, Map<String, List<String>>> errorMap) {
        if (errors.isEmpty() && errorMap.isEmpty()) {
            return SchemaValidationDto.builder().status(true).build();
        }
        return SchemaValidationDto.builder().status(false).errorMessages(errors)
                                  .changes(changes.toPrettyString()).errorMap(errorMap).build();
    }
    
    public void validateSchema(Table tableOld, Table tableNew, ObjectNode fileChange, Set<String> tableFullNames,
                               String filePath, Long prId, Map<String, List<String>> tableErrorMap) {
        // we dont allow schema name change
        var tableFullName = tableOld.getNameSpace()+'.'+tableOld.getName();
        List<String> errors = new ArrayList<>();
        if (!tableNew.getName().equals(tableOld.getName())) {
            errors.add(responseMessage.getErrorMessage(ErrorCode.TABLE_NAME_CHANGE_NOT_ALLOWED_FROM_TO, tableOld.getName(), tableNew.getName())); 
        }
        if (!ObjectUtils.isEmpty(tableNew.getJsonSchemaId()) && !tableNew.getJsonSchemaId().equals(tableOld.getJsonSchemaId())) {
            errors.add(responseMessage.getErrorMessage(ErrorCode.JSON_SCHEMA_ID_CHANGE_NOT_ALLOWED_FROM_TO, tableOld.getJsonSchemaId(), tableNew.getJsonSchemaId())); 
        }
        if (!tableNew.getNameSpace().equals(tableOld.getNameSpace())) {
            errors.add(responseMessage.getErrorMessage(ErrorCode.TABLE_NAMESPACE_CHANGE_NOT_ALLOWED_FROM_TO, tableOld.getNameSpace(), tableNew.getNameSpace())); 
        }
        if (Objects.nonNull(tableOld.getOwner()) && Boolean.FALSE.equals(tableOld.getOwner().equals(tableNew.getOwner()))) {
            errors.add(responseMessage.getErrorMessage(ErrorCode.TABLE_OWNER_METADATA_CHANGED_FROM_TO_NOT_ALLOWD_TABLE_NAME, tableOld.getOwner(), tableNew.getOwner(), tableOld.getName()));
        }
        if (Objects.nonNull(tableOld.getDomain()) && Boolean.FALSE.equals(tableOld.getDomain().equals(tableNew.getDomain()))) {
            errors.add(responseMessage.getErrorMessage(ErrorCode.TABLE_DOMAIN_METADATA_CHANGED_FROM_TO_NOT_ALLOWD_TABLE_NAME, tableOld.getDomain(), tableNew.getDomain(), tableOld.getName()));
        }
        Map<String, Field> newTableFields = tableNew.getFields().stream().collect(Collectors.toMap(Field::getName, Function.identity()));
        Map<String, Field> oldTableFields = tableOld.getFields().stream().filter(field -> (field.getIsDeleted() == true || field.getPrId() == null))
                                                                         .collect(Collectors.toMap(Field::getName, Function.identity())); // filter is to remove fields in PR.
        int oldTableSize = oldTableFields.size();
        int newTableSize = newTableFields.size();
        if (oldTableSize > newTableSize) { //field deleted
            errors.add(responseMessage.getErrorMessage(ErrorCode.FIELD_DELETED_NOT_ALLOWED)); 
        }
    
        ObjectNode tableChange = fileChange.putObject(tableFullName);
        ObjectNode fieldChanges = tableChange.putObject("fields");
    
        for (Map.Entry<String, Field> oldFieldEntry : oldTableFields.entrySet()) {
            // field name dont change
            if (newTableFields.get(oldFieldEntry.getKey()) == null) {
                var message = responseMessage.getErrorMessage(ErrorCode.FIELD_NAME_UPDATION_NOT_ALLOWED, oldFieldEntry.getKey());
                errors.add(message); 
            } else {
                validateField(newTableFields.get(oldFieldEntry.getKey()), oldFieldEntry.getValue(), tableNew, errors, fieldChanges, fileChange, tableFullNames, filePath, prId, tableErrorMap);
                newTableFields.remove(oldFieldEntry.getKey());
            }
        }
        // this loop is for validate nested table of added new field.
        newTableFields.entrySet().stream().forEach(newFieldEntry -> checkNestedTableForNewField(newFieldEntry.getValue(), fileChange, fieldChanges, tableFullNames, filePath, tableNew.getName(), prId, tableErrorMap, errors));
        if (fieldChanges.isEmpty()) tableChange.remove("fields");
        if (tableService.compareTable(tableNew, tableOld)) {
            StringUtil.addToJsonNode(tableChange, Table.Prop.DESCRIPTION, tableNew.getDescription());  // need to add more table props which can change.
            StringUtil.addToJsonNode(tableChange, Table.Prop.OWNER, tableNew.getOwner());
            StringUtil.addToJsonNode(tableChange, Table.Prop.DOMAIN, tableNew.getDomain());
            StringUtil.addToJsonNode(tableChange, Table.Prop.EMAIL, tableNew.getEmail());
            StringUtil.addToJsonNode(tableChange, Table.Prop.COMPLIANCE_OWNER, tableNew.getComplianceOwner());
            StringUtil.addToJsonNode(tableChange, Table.Prop.CHANNEL, tableNew.getChannel());
            StringUtil.addToJsonNode(tableChange, Table.Prop.STATUS, tableNew.getStatus());
            StringUtil.addToJsonNode(tableChange, Table.Prop.QUALITY_RULE_BASE, tableNew.getQualityRuleBase());
            StringUtil.addToJsonNode(tableChange, Table.Prop.QUALITY_RULE_SQL, tableNew.getQualityRuleSql());
            StringUtil.addToJsonNode(tableChange, Table.Prop.QUALITY_RULE_CEL, tableNew.getQualityRuleCel());
            StringUtil.addArrayToJsonNode(tableChange, Table.Prop.SUBSCRIBERS, tableNew.getSubscribers());
            StringUtil.addArrayToJsonNode(tableChange, Table.Prop.REQUIRED, tableNew.getRequiredFields());
            // addStringToNode(tableChange, Table.Prop.ADDITIONAL_PROPERTIES,);                     
        }
        if (tableChange.isEmpty()) {
            fileChange.remove(tableFullName);
        } else {
            tableFullNames.add(tableNew.getNameSpace()+'.'+tableNew.getName());
            // if the changes is done in this table means need to check it and validate.
            checkTableChangeInAnotherPr(tableOld, tableNew, prId, errors, filePath);
        }
        if (errors.isEmpty()) return;
        tableErrorMap.put(tableFullName, errors);
    }

    private void checkTableChangeInAnotherPr(Table tableOld, Table tableNew, Long prId, List<String> errors, String filePath) {
        if (!ObjectUtils.isEmpty(tableOld.getPrId()) && !tableOld.getPrId().equals(prId)) {
            var message = responseMessage.getErrorMessage(ErrorCode.TABLE_ALREADY_MODIFIED_IN_PR, tableOld.getName(), pullRequestService.getModel(tableOld.getPrId()).getNumber());
            errors.add(message);
        }
        var oldPr = tableOld.getFields().stream().filter(field -> !ObjectUtils.isEmpty(field.getPrId())).findFirst();
        if (oldPr.isPresent() && Boolean.FALSE.equals(prId.equals(oldPr.get().getPrId()))) {
            var message = responseMessage.getErrorMessage(ErrorCode.TABLE_ALREADY_MODIFIED_IN_PR, tableOld.getName(), pullRequestService.getModel(oldPr.get().getPrId()).getNumber());
            errors.add(message);
        }
        // NEED TO CHECK NECCESSORY FIELDS IS PRESENT IN TABLE AND FIELD.
        checkMetadataIsEmpty(tableNew, errors);
    }

    private void checkMetadataIsEmpty(Table table, List<String> errors) {
        var schemaError = apply(table);
        if (schemaError.status().equals(Status.ERROR)) {
            var message = schemaError.errorMessages().stream().map(msg -> responseMessage.getErrorMessage(ErrorCode.TABLE_METADATA_MISSING_NAME, msg, table.getName())).toList();
            errors.add(String.join(" ", message));
        }
        table.getFields().stream().forEach(field -> {
            var error = fieldValidator.apply(field);
            if (error.status().equals(Status.ERROR)) {
                var message = error.errorMessages().stream().map(msg -> responseMessage.getErrorMessage(ErrorCode.FIELD_METADATA_MISSING_NAME_IN_TABLE, msg, field.getName(), table.getName())).toList();
                errors.add(String.join(" ", message));
            }
        });
    }
    
    private void addBooleanToNode(ObjectNode node, String key, Boolean value) {
        if (value == null) {
            node.putNull(key);
        } else {
            node.put(key, value);
        }
    }
    
    private void validateNewSchema(Table tableNew, ObjectNode fileChange, Set<String> tableFullNames,
                    String filePath, Long prId, Map<String, List<String>> tableErrorMap, List<String> errors) {
        var tableOld = tableService.findByNameAndNameSpace(tableNew.getName(), tableNew.getNameSpace());
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(tableOld)) && isMainBranchTable(tableOld)) {
            validateSchema(tableOld, tableNew, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        } else {
            var tableFullName = tableNew.getNameSpace()+'.'+tableNew.getName();
            ObjectNode tableChange = fileChange.putObject(tableFullName);
            ObjectNode fieldChanges = tableChange.putObject("fields");
    
            tableNew.getFields().stream().forEach(field -> checkNestedTableForNewField(field, fileChange, fieldChanges, tableFullNames, filePath, tableNew.getName(), prId, tableErrorMap, errors));
            checkMetadataIsEmpty(tableNew, errors);
            if (fieldChanges.isEmpty()) tableChange.remove("fields");
            StringUtil.addToJsonNode(tableChange, Table.Prop.DESCRIPTION, tableNew.getDescription());  // need to add more table props which can change.
            StringUtil.addToJsonNode(tableChange, Table.Prop.OWNER, tableNew.getOwner());
            StringUtil.addToJsonNode(tableChange, Table.Prop.DOMAIN, tableNew.getDomain());
            StringUtil.addToJsonNode(tableChange, Table.Prop.EMAIL, tableNew.getEmail());
            StringUtil.addToJsonNode(tableChange, Table.Prop.COMPLIANCE_OWNER, tableNew.getComplianceOwner());
            StringUtil.addToJsonNode(tableChange, Table.Prop.CHANNEL, tableNew.getChannel());
            StringUtil.addToJsonNode(tableChange, Table.Prop.STATUS, tableNew.getStatus());
            StringUtil.addToJsonNode(tableChange, Table.Prop.QUALITY_RULE_BASE, tableNew.getQualityRuleBase());
            StringUtil.addToJsonNode(tableChange, Table.Prop.QUALITY_RULE_SQL, tableNew.getQualityRuleSql());
            StringUtil.addToJsonNode(tableChange, Table.Prop.QUALITY_RULE_CEL, tableNew.getQualityRuleCel());
            StringUtil.addArrayToJsonNode(tableChange, Table.Prop.SUBSCRIBERS, tableNew.getSubscribers());
            StringUtil.addArrayToJsonNode(tableChange, Table.Prop.REQUIRED, tableNew.getRequiredFields());
            tableFullNames.add(tableNew.getNameSpace()+'.'+tableNew.getName());
        }
    }

    private Boolean isMainBranchTable(Table table) {
        return ((table.getIsDeleted().equals(Boolean.TRUE) && table.getIsUserChanged().equals(Boolean.TRUE)) || 
                (table.getIsDeleted().equals(Boolean.FALSE) && table.getIsUserChanged().equals(Boolean.FALSE)));
    }
    
    private void checkNestedTableForNewField(Field field, ObjectNode fileChange, ObjectNode fieldChanges, Set<String> tableFullNames, 
                        String filePath, String tableName, Long prId, Map<String, List<String>> tableErrorMap, List<String> errors) {
        if (field.getIsPrimitiveType().equals(Boolean.FALSE)) {
            if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getContain()))) {
                validateNewSchema(field.getContain(), fileChange, tableFullNames, filePath, prId, tableErrorMap, errors);
            }
            switch(field.getDataType()) {
                case "array" -> checkNestedTableForNewArrayField(field, fileChange, fieldChanges, tableFullNames, filePath, tableName, prId, tableErrorMap, errors);
                case "map" -> checkNestedTableForNewMapField(field, fileChange, fieldChanges, tableFullNames, filePath, tableName, prId, tableErrorMap, errors);
                case "union" -> checkNestedTableForNewUnionField(field, fileChange, fieldChanges, tableFullNames, filePath, tableName, prId, tableErrorMap, errors);
            }
        }
        if (fieldChanges != null) {
            ObjectNode currentFieldChange = fieldChanges.putObject(field.getName());
                StringUtil.addToJsonNode(currentFieldChange, Field.Prop.DESCRIPTION, field.getDescription());
                StringUtil.addToJsonNode(currentFieldChange, Field.Prop.DATA_TYPE, field.getDataType());
                StringUtil.addArrayToJsonNode(currentFieldChange, Field.Prop.ENUM, field.getSymbols());
                addBooleanToNode(currentFieldChange, Field.Prop.IS_PII, field.getIsPii());
                addBooleanToNode(currentFieldChange, Field.Prop.IS_CLASSIFIED, field.getIsClassified());
                addBooleanToNode(currentFieldChange, Field.Prop.DEPRECATED, field.getDeprecated());
        }
    }
    
    private void checkNestedTableForNewArrayField(Field field, ObjectNode fileChange, ObjectNode fieldChanges, Set<String> tableFullNames,
                            String filePath, String tableName, Long prId, Map<String, List<String>> tableErrorMap, List<String> errors) {
        var items = field.getItems();
        if (items.equalsIgnoreCase("array")) checkNestedTableForNewArrayField(field.getArrayField(), fileChange, fieldChanges, tableFullNames, filePath, tableName, prId, tableErrorMap, errors);
        else if (items.equalsIgnoreCase("map")) checkNestedTableForNewMapField(field.getMapField(), fileChange, fieldChanges, tableFullNames, filePath, tableName, prId, tableErrorMap, errors);
        else if (items.equalsIgnoreCase("union")) checkNestedTableForNewUnionField(field, fileChange, fieldChanges, tableFullNames, filePath, tableName, prId, tableErrorMap, errors);
        else if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getContain()))) validateNewSchema(field.getContain(), fileChange, tableFullNames, filePath, prId, tableErrorMap, errors);
    }
    
    private void checkNestedTableForNewMapField(Field field, ObjectNode fileChange, ObjectNode fieldChanges, Set<String> tableFullNames,
                          String filePath, String tableName, Long prId, Map<String, List<String>> tableErrorMap, List<String> errors) {
        var values = field.getValues();
        if (values.equalsIgnoreCase("array")) checkNestedTableForNewArrayField(field.getArrayField(), fileChange, fieldChanges, tableFullNames, filePath, tableName, prId, tableErrorMap, errors);
        else if (values.equalsIgnoreCase("map")) checkNestedTableForNewMapField(field.getMapField(), fileChange, fieldChanges, tableFullNames, filePath, tableName, prId, tableErrorMap, errors);
        else if (values.equalsIgnoreCase("union")) checkNestedTableForNewUnionField(field, fileChange, fieldChanges, tableFullNames, filePath, tableName, prId, tableErrorMap, errors);
        else if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getContain()))) validateNewSchema(field.getContain(), fileChange, tableFullNames, filePath, prId, tableErrorMap, errors);
    }
    
    private void checkNestedTableForNewUnionField(Field field, ObjectNode fileChange, ObjectNode fieldChanges, Set<String> tableFullNames,
                            String filePath, String tableName, Long prId, Map<String, List<String>> tableErrorMap, List<String> errors) {
        field.getUnionTypes().stream().forEach(unionField -> checkNestedTableForNewField(unionField, fileChange, null, tableFullNames, filePath, tableName, prId, tableErrorMap, errors));
    }
    
    public void validateField(Field fieldNew, Field fieldOld, Table tableNew, List<String> errors, ObjectNode fieldChanges, ObjectNode fileChange,
                              Set<String> tableFullNames, String filePath, Long prId, Map<String, List<String>> tableErrorMap) {
    
        // field type dont change
        if (!fieldOld.getDataType().equals(fieldNew.getDataType())) {
            var message = responseMessage.getErrorMessage(ErrorCode.FIELD_DATATYPE_UPDATION_NOT_ALLOWED_FOR_FIELD, fieldOld.getName());
            errors.add(message);
        }
        else if (fieldOld.getDataType().equals("union")) {
            // check union filed
            validateUnionField(fieldNew, fieldOld, tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
        else if (fieldOld.getDataType().equals("array")) {
            // check array field
            validateArrayField(fieldNew, fieldOld, tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
        else if (fieldOld.getDataType().equals("map")) {
            // check map field
            validateMapField(fieldNew, fieldOld, tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
        else if (Boolean.FALSE.equals(ObjectUtils.isEmpty(fieldNew.getContain()))) {
            validateNestedSchema(fieldNew, fieldOld, tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
        if (fieldChanges != null && tableService.compareField(fieldNew, fieldOld, RepoType.AVRO)) {  // Here is repotype hardcoded because here protobuf will no get validated.
            ObjectNode fieldChange = fieldChanges.putObject(fieldNew.getName());
                StringUtil.addToJsonNode(fieldChange, Field.Prop.DESCRIPTION, fieldNew.getDescription());
                StringUtil.addToJsonNode(fieldChange, Field.Prop.DATA_TYPE, fieldNew.getDataType());
                StringUtil.addArrayToJsonNode(fieldChange, Field.Prop.ENUM, fieldNew.getSymbols());
                addBooleanToNode(fieldChange, Field.Prop.IS_PII, fieldNew.getIsPii());
                addBooleanToNode(fieldChange, Field.Prop.IS_CLASSIFIED, fieldNew.getIsClassified());
                addBooleanToNode(fieldChange, Field.Prop.DEPRECATED, fieldNew.getDeprecated());
        }
    }
    
    private void validateNestedSchema(Field fieldNew, Field fieldOld, Table tableNew, List<String> errors, ObjectNode fileChange, 
                                      Set<String> tableFullNames, String filePath, Long prId, Map<String, List<String>> tableErrorMap) {
        Map<String, List<String>> errorMap = new HashMap<>();
        validateSchema(fieldOld.getContain(), fieldNew.getContain(), fileChange, tableFullNames, filePath, prId, errorMap);
        if (!errorMap.isEmpty()) {
            var message = responseMessage.getErrorMessage(ErrorCode.FIELD_NAME_OF_TABLE_HAS_ERROR, fieldOld.getName(), tableNew.getName());
            errors.add(message);
            tableErrorMap.putAll(errorMap);
        }
    }
    
    private void validateUnionField(Field fieldNew, Field fieldOld, Table tableNew, List<String> errors, ObjectNode fileChange,
                                    Set<String> tableFullNames, String filePath, Long prId, Map<String, List<String>> tableErrorMap) {
        Map<String, Field> oldUnionFields = fieldOld.getUnionTypes().stream().collect(Collectors.toMap(Field::getDataType, Function.identity()));
        Map<String, Field> newUnionFields = fieldNew.getUnionTypes().stream().collect(Collectors.toMap(Field::getDataType, Function.identity()));
        if (oldUnionFields.size() == newUnionFields.size()) {
            for (Map.Entry<String, Field> oldFieldEntry : oldUnionFields.entrySet()) {
                if (newUnionFields.get(oldFieldEntry.getKey()) == null) {
                    var message = responseMessage.getErrorMessage(ErrorCode.UNION_FIELD_DATATYPE_UPDATION_NOT_ALLOWED_FOR_FIELD, fieldOld.getName());
                    errors.add(message);
                } else {
                    validateField(newUnionFields.get(oldFieldEntry.getKey()), oldFieldEntry.getValue(), tableNew, errors, null, fileChange, tableFullNames, filePath, prId, tableErrorMap);
                }
            }
        } else {
            var message = responseMessage.getErrorMessage(ErrorCode.UNION_FIELD_DATATYPE_UPDATION_NOT_ALLOWED_FOR_FIELD, fieldOld.getName());
            errors.add(message);
        }
    }
    
    public void validateArrayField(Field fieldNew, Field fieldOld, Table tableNew, List<String> errors, ObjectNode fileChange,
                                   Set<String> tableFullNames, String filePath, Long prId, Map<String, List<String>> tableErrorMap) {
        // compare the array type
        if (!fieldOld.getItems().equals(fieldNew.getItems())) {
            var message = responseMessage.getErrorMessage(ErrorCode.ARRAY_FIELD_TYPE_UPDATION_NOT_ALLOWED_FOR_FIELD, fieldOld.getName());
            errors.add(message);
        }
        else if(fieldOld.getItems().equals("union")) {
            // check union filed
            validateUnionField(fieldNew, fieldOld, tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
        else if (fieldOld.getItems().equals("array")) {
            // check array field
            validateArrayField(fieldNew.getArrayField(), fieldOld.getArrayField(), tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
        else if (fieldOld.getItems().equals("map")) {
            // check map field
            validateMapField(fieldNew.getMapField(), fieldOld.getMapField(), tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
        else if (Boolean.FALSE.equals(ObjectUtils.isEmpty(fieldNew.getContain()))) {
            validateNestedSchema(fieldNew, fieldOld, tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
    }
    
    public void validateMapField(Field fieldNew, Field fieldOld, Table tableNew, List<String> errors, ObjectNode fileChange, 
                                 Set<String> tableFullNames, String filePath, Long prId, Map<String, List<String>> tableErrorMap) {
        // compare the array type
        if (!fieldOld.getValues().equals(fieldNew.getValues())) {
            var message = responseMessage.getErrorMessage(ErrorCode.MAP_FIELD_TYPE_UPDATION_NOT_ALLOWED_FOR_FIELD, fieldOld.getName());
            errors.add(message); 
        }
        else if(fieldOld.getValues().equals("union")) {
            // check union filed
            validateUnionField(fieldNew, fieldOld, tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
        else if (fieldOld.getValues().equals("array")) {
            // check array field
            validateArrayField(fieldNew.getArrayField(), fieldOld.getArrayField(), tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
        else if (fieldOld.getValues().equals("map")) {
            // check map field
            validateMapField(fieldNew.getMapField(), fieldOld.getMapField(), tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
        else if (Boolean.FALSE.equals(ObjectUtils.isEmpty(fieldNew.getContain()))) {
            validateNestedSchema(fieldNew, fieldOld, tableNew, errors, fileChange, tableFullNames, filePath, prId, tableErrorMap);
        }
    }
    
    private void checkSchemaChangesInOtherFiles(ObjectNode changes, List<String> errors, Set<String> tableFullNames, ClientRepo clientRepo) {
        System.out.println(changes.toPrettyString());
        var models = modelService.findByFullNames(tableFullNames, clientRepo.getId());
        Multimap<String, String> namePathMap = LinkedHashMultimap.create();
        models.stream().forEach(model -> {
            namePathMap.put(model.getNameSpace()+"."+model.getName(), model.getPath());
        });
        var basePath = StringUtil.constructStringEmptySeparator("https://github.com/",clientRepo.getFullName(),"/tree/main/");
        tableFullNames.stream().forEach(tableFullName -> {
            // this contain the paths of other files which contain this table.
            var paths = namePathMap.get(tableFullName).toArray(new String[0]);
            if (paths.length == 0) {
                // length will be 0 when there is new table.
                var newPaths = new ArrayList<String>();
                var pathIterator = changes.fieldNames();
                // Add new table path to 'paths' variable 
                // So that we can check the new table changes is reflected in other file which contain same table.
                while(pathIterator.hasNext()) {
                    var path = pathIterator.next();
                    if (changes.get(path).has(tableFullName)) newPaths.add(path);
                }
                paths = newPaths.toArray(new String[0]);
            }
            for (int i=0; i<paths.length; i++) {
                var path1 = paths[i];
                if (Boolean.FALSE.equals(changes.has(path1)) || Boolean.FALSE.equals(changes.get(path1).has(tableFullName))) {
                    errors.add(responseMessage.getErrorMessage(ErrorCode.SCHEMA_CHANGES_NOT_PRESENT_IN_FILE, tableFullName, path1.substring(basePath.length(), path1.length())));   
                    continue;
                }
                for (int j=i+1; j<paths.length; j++) {
                    var path2 = paths[j];
                    if (Boolean.FALSE.equals(changes.has(path2)) || Boolean.FALSE.equals(changes.get(path2).has(tableFullName))) {
                        errors.add(responseMessage.getErrorMessage(ErrorCode.SCHEMA_CHANGES_NOT_PRESENT_IN_FILE, tableFullName, path2.substring(basePath.length(), path2.length())));
                        i++; 
                        continue;
                    }
                    //compare Schema in files
                    var table1 = changes.get(path1).get(tableFullName);
                    var table2 = changes.get(path2).get(tableFullName);
                    if (Boolean.FALSE.equals(table1.equals(table2))) {
                        errors.add(responseMessage.getErrorMessage(ErrorCode.SCHEMA_CHANGES_NAME_OF_FILE1_NOT_SAME_AS_SCHEMA_IN_FILE2, tableFullName, path1.substring(basePath.length(), path1.length()), path2.substring(basePath.length(), path2.length())));
                    }
                    i++;
                }
            }
        });
    }
}
