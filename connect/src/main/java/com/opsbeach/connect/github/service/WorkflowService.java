package com.opsbeach.connect.github.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.github.dto.WorkflowDto;
import com.opsbeach.connect.github.dto.WorkflowDto.FieldDto;
import com.opsbeach.connect.github.dto.WorkflowDto.TableDto;
import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.entity.PullRequest;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.entity.Workflow;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.repository.WorkflowRepository;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.connect.schemata.validate.FieldValidator;
import com.opsbeach.connect.schemata.validate.SchemaValidator;
import com.opsbeach.connect.schemata.validate.Status;
import com.opsbeach.sharedlib.exception.AlreadyExistException;
import com.opsbeach.sharedlib.exception.BadRequestException;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.utils.StringUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowService {
    
    private final WorkflowRepository workflowRepository;
    private final SchemaFileAuditService schemaFileAuditService;
    private final TableService tableService;
    private final GitHubService gitHubService;
    private final ResponseMessage responseMessage;
    private final ClientRepoService clientRepoService;
    private final ModelService modelService;
    private final SchemaValidator schemaValidator;
    private final FieldValidator fieldValidator;

    @Value("${github.construct-file-path}")
    private String githubFilePath;

    public WorkflowDto add(WorkflowDto workflowDto) {
        var workflow = addModel(workflowDto);
        return workflow.toDto(workflow);
    }

    public Workflow addModel(WorkflowDto workflowDto) {
        return workflowRepository.save(workflowDto.toDomain(workflowDto));
    }

    public Workflow addModel(Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    public WorkflowDto get(Long id) {
        var workflow = getModel(id);
        return workflow.toDto(workflow);
    }

    public Workflow getModel(Long id) {
        return workflowRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.WORKFLOW)));
    }

    public List<WorkflowDto> getAll() {
        var workflows = workflowRepository.findAll();
        return workflows.isEmpty() ? List.of() : workflows.stream().map(workflows.get(0)::toDto).toList();
    }

    public Workflow updateStatus(Long id, Workflow.Status status) {
        var workflow = getModel(id);
        workflow.setStatus(status);
        return addModel(workflow);
    }

    private WorkflowDto toDto(Workflow workflow, List<Field> fields) {
        return WorkflowDto.builder().id(workflow.getId())
                                    .domainId(workflow.getDomainId())
                                    .nodeId(workflow.getNodeId())
                                    .schemaName(workflow.getSchemaName())
                                    .stackHolders(workflow.getStackHolders())
                                    .purpose(workflow.getPurpose())
                                    .creator(workflow.getCreator())
                                    .additionalReference(workflow.getAdditionalReference())
                                    .status(workflow.getStatus())
                                    .rank(workflow.getRank())
                                    .title(workflow.getTitle())
                                    // .fields(Objects.isNull(fields) ? List.of() : fields.stream().map(fields.get(0)::toDto).toList())
                                    .build();
    }

    @Transactional
    public Object saveAndRaisePr(WorkflowDto workflowDto, Long clientRepoId) {
        var clientRepo = clientRepoService.getModel(clientRepoId);
        var workflow = Workflow.builder().status(Workflow.Status.NEW).title("changes-in-table-"+workflowDto.getTable().name())
                               .purpose("modifying schema "+workflowDto.getTable().name()).build();
        workflow = addModel(workflow);
        // if (clientRepo.getRepoType().equals(RepoType.PROTOBUF)) {
        //     protobufSchemaPrRaise(workflowDto.table(), clientRepo, workflow);
        // } else {
        AtomicBoolean isTableChanged = new AtomicBoolean(false);
        var table = saveTableDelta(workflowDto.getTable(), clientRepo.getRepoType(), isTableChanged);
        if (isTableChanged.get() == false) return "FIELD MAPPING SUCCESS"; 
        SchemaFileAudit schemaFileAudit = null;
        Model model = null;
        if (Objects.isNull(workflowDto.getTable().id())) {
            // if the ID is null means then it is new table added
            // so we need to create new file and model audit for that table.
            // need to save what type of repository in clientRepotable (ex avro, json and proto).
            var rootFilePath = clientRepo.getRepoType().equals(RepoType.PROTOBUF) ? Constants.PROTOBUF_SCHEMA_ROOT_FILE_PATH : null; 
            schemaFileAudit = schemaFileAuditService.createSchemaFileAuditForNewFile(clientRepo, table, rootFilePath);
            model = modelService.createModel(schemaFileAudit, clientRepo.getId(), table);
        }
        Map<SchemaFileAudit, String> fileContentMap = schemaFileAuditService.generateFileContentOfSchema(table.getId(), clientRepo.getRepoType());
        var pullRequest = gitHubService.commitAndRaisePr(fileContentMap, workflow);
        updatePrNumber(pullRequest, table);
        if (Objects.nonNull(schemaFileAudit) && Objects.nonNull(model)) {
            // update PR_ID in file and model audit record.
            schemaFileAudit.setPullRequestId(pullRequest.getId());
            schemaFileAuditService.addModel(schemaFileAudit);
            model.setPullRequestId(pullRequest.getId());
            modelService.addModel(model);
        }
        workflow.setStatus(Workflow.Status.PR_RAISED);
        addModel(workflow);
        return toDto(workflow, null);
    }

    public Table updatePrNumber(PullRequest pullRequest, Table table) {
        table.getFields().forEach(field -> {
            if (Boolean.TRUE.equals(field.getIsUserChanged()) && ObjectUtils.isEmpty(field.getPrId()))
            field.setPrId(pullRequest.getId());
        });
        if (Boolean.TRUE.equals(table.getIsUserChanged()) && ObjectUtils.isEmpty(table.getPrId())) {
            table.setPrId(pullRequest.getId());
        }
        return tableService.addTable(table);
    }

    private Table saveTableDelta(TableDto modifiedTable, RepoType repoType, AtomicBoolean isTableChanged) {
        var tableNew = modifiedTable.toDomain(modifiedTable);
        if (Objects.nonNull(modifiedTable.id())) {
            // ASSUMING THAT THE CHAGED TABLE WILL NOT CONTAIN ANY PREVIOUS CHANGES. (i.e. IT IS NEWLY CHANGED TABLE).
            var tableOld = tableService.getModel(modifiedTable.id());
            validateTable(tableNew, tableOld);
            if (!tableService.compareTable(tableNew, tableOld)) tableNew = null;  // return true if the table has any change.
            return addFieldsInTable(modifiedTable.fields(), modifiedTable.id(), tableNew, tableOld, repoType, isTableChanged);
        }
        isTableChanged.set(true);
        validateTable(tableNew, null);
        tableNew.setIsUserChanged(Boolean.TRUE);
        tableNew.setIsDeleted(Boolean.FALSE);
        tableNew.setClientId(SecurityUtil.getClientId());   // Add clientId for new table.
        var fullName = StringUtil.constructStringEmptySeparator(tableNew.getNameSpace(),".",tableNew.getName());
        List<Field> fields = new ArrayList<>();
        AtomicInteger i = new AtomicInteger(1);
        modifiedTable.fields().forEach(fieldDto -> {
            var field = fieldDto.toDomain(fieldDto);
            field.setIsPrimitiveType(Field.isPrimitiveType(field.getDataType()));
            field.setSchema(fullName);
            field.setIsUserChanged(Boolean.TRUE);
            field.setIsDeleted(Boolean.FALSE);
            field.setRowNumber(i.getAndIncrement());
            addComplexTypeToField(field, Boolean.TRUE);
            addReferenceField(field, fieldDto.referenceFieldId());
            fields.add(field);
        });
        tableNew.setFields(fields);
        var table = tableService.addTable(tableNew);
        return table;
    }

    // to save existing table delta.
    public Table addFieldsInTable(List<FieldDto> fieldDtos, Long tableId, Table modifiedTable, Table table, RepoType repoType, AtomicBoolean isTableChanged) {
        if (Objects.nonNull(modifiedTable)) {
            modifiedTable.setIsUserChanged(Boolean.TRUE);
            modifiedTable.setIsDeleted(Boolean.FALSE);
            modifiedTable.setId(null);
            modifiedTable.setFields(null);
            table.setModifiedTable(modifiedTable);
            table.setIsUserChanged(Boolean.TRUE);
            table.setIsDeleted(Boolean.TRUE);
            isTableChanged.set(true);
        }
        var fields = table.getFields().stream().collect(Collectors.toMap(Field::getId, Function.identity()));
        var fieldNames = fields.values().stream().map(Field::getName).toList();
        // List<Field> addedFields = fieldDtos.isEmpty() ? List.of() : fieldDtos.stream().map(fieldDtos.get(0)::toDomin).toList();
        List<Field> updatedFields = new ArrayList<>();
        AtomicInteger rowNumber = new AtomicInteger(fields.size()+1);
        fieldDtos.forEach(fieldDto -> {
            var oldField = fields.get(fieldDto.id());
            var addedField = fieldDto.toDomain(fieldDto);
            validateField(addedField, oldField, fieldNames);
            boolean isNewField = Boolean.TRUE;
            if (Objects.nonNull(oldField)) {
                var isFieldChanged = tableService.compareField(addedField, oldField, repoType);
                if (isFieldChanged) {
                    isTableChanged.set(true);
                    oldField.setIsUserChanged(Boolean.TRUE);
                    oldField.setIsDeleted(Boolean.TRUE);
                    addedField.setId(null);
                    transferFieldProps(oldField, addedField);
                    updatedFields.add(oldField);
                    fields.remove(oldField.getId());
                    isNewField = Boolean.FALSE;
                }
                if (Objects.nonNull(fieldDto.referenceFieldId())) {
                    addReferenceField(oldField, fieldDto.referenceFieldId());
                    updatedFields.add(oldField);
                    fields.remove(oldField.getId());
                }
                if (Boolean.FALSE.equals(isFieldChanged))
                    return;
            }
            isTableChanged.set(true);
            addedField.setIsUserChanged(Boolean.TRUE);
            addedField.setIsDeleted(Boolean.FALSE);
            addedField.setIsPrimitiveType(Field.isPrimitiveType(addedField.getDataType()));
            if (addedField.getRowNumber() == 0) addedField.setRowNumber(rowNumber.getAndIncrement());
            addComplexTypeToField(addedField, isNewField);
            addReferenceField(addedField, fieldDto.referenceFieldId());
            updatedFields.add(addedField);
        });
        updatedFields.addAll(fields.values());  // add the modified fields and non changed fields in one list.
        table.setFields(updatedFields);  // set fieldList as table fields.
        return tableService.addTable(table); 
    }

    private void addReferenceField(Field fieldNew, Long referenceFieldId) {
        if (Objects.nonNull(referenceFieldId)) {
            var field = tableService.getFieldModel(referenceFieldId);
            fieldNew.setReferenceField(field);
        }
    }

    private void addComplexTypeToField(Field fieldNew, Boolean isNewField) {
        // The schema mapping with field will be with another schema in same repositroy. so no need worry about mapping schema with different repo.
        if (fieldNew.getIsPrimitiveType().equals(Boolean.FALSE) && isNewField) {
            var fullName = fieldNew.getDataType();
            var nameSpace = fullName.substring(0, fullName.lastIndexOf("."));
            var name = fullName.substring(fullName.lastIndexOf(".")+1, fullName.length());
            var table = tableService.findByNameAndNameSpace(name, nameSpace);
            fieldNew.setContain(table);
            if (Boolean.FALSE.equals(ObjectUtils.isEmpty(table.getJsonSchemaId())))
                fieldNew.setJsonSchemaRefId(table.getJsonSchemaId());
        }
    } 

    private void transferFieldProps(Field fromField, Field toField) {
        // currently do not tranfer props like (description, isPii, isClassified) 
        // Because that props be changed by user. So that values should be incomming value.
        // we are transfering non editable props from existing field to same delta field.
        toField.setSchema(fromField.getSchema());
        toField.setRowNumber(fromField.getRowNumber());
        toField.setIsPrimaryKey(fromField.getIsPrimaryKey());
        toField.setIsPrimitiveType(fromField.getIsPrimitiveType());
        toField.setDefaultValue(fromField.getDefaultValue());
        // toField.setIsNullable(fromField.getIsNullable());
        toField.setSymbols(fromField.getSymbols());
        toField.setItems(fromField.getItems());
        toField.setValues(fromField.getValues());
        toField.setSize(fromField.getSize());
        toField.setArrayField(fromField.getArrayField());
        toField.setMapField(fromField.getMapField());
        toField.setUnionTypes(fromField.getUnionTypes());
        toField.setContain(fromField.getContain());
        toField.setReferenceField(fromField.getReferenceField());
    }

    private void validateTable(Table tableNew, Table tableOld) {
        if (Objects.nonNull(tableOld)) {
            if (Boolean.FALSE.equals(tableNew.getNameSpace().equals(tableOld.getNameSpace()))) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST, responseMessage.getErrorMessage(ErrorCode.BAD_REQUEST, "Table namespace change Not allowed"));
            }
            if (Boolean.FALSE.equals(tableNew.getName().equals(tableOld.getName()))) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST, responseMessage.getErrorMessage(ErrorCode.BAD_REQUEST, "Table name change Not allowd"));
            }
            if (Boolean.TRUE.equals(tableOld.getIsUserChanged())) {
                throw new BadRequestException(ErrorCode.TABLE_ALREADY_MODIFIED, responseMessage.getErrorMessage(ErrorCode.TABLE_ALREADY_MODIFIED, tableOld.getName()));
            }
            tableOld.getFields().stream().forEach(field -> {
                if (Boolean.TRUE.equals(field.getIsUserChanged())) {
                    throw new BadRequestException(ErrorCode.TABLE_ALREADY_MODIFIED, responseMessage.getErrorMessage(ErrorCode.TABLE_ALREADY_MODIFIED, tableOld.getName()));
                }
            });
        } else {
            var models = modelService.findModelByNameAndNameSpace(tableNew.getNameSpace(), tableNew.getName());
            if (models.size() > 0) {
                throw new AlreadyExistException(ErrorCode.ALREADY_EXISTS, responseMessage.getErrorMessage(ErrorCode.ALREADY_EXISTS, StringUtil.constructStringEmptySeparator("Table name ",tableNew.getNameSpace(),".", tableNew.getName(), "Already exists")));
            }
        }
        var schemaError = schemaValidator.apply(tableNew);   /// mock
        if (schemaError.status().equals(Status.ERROR)) {
            throw new BadRequestException(ErrorCode.TABLE_VALIDATION_ERROR, responseMessage.getErrorMessage(ErrorCode.TABLE_VALIDATION_ERROR, schemaError.errorMessages()));
        }
    }

    private void validateField(Field fieldNew, Field fieldOld, List<String> fieldNames) {
        if (Objects.nonNull(fieldOld)) {
            if (Boolean.FALSE.equals(fieldNew.getName().equals(fieldOld.getName()))) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST, responseMessage.getErrorMessage(ErrorCode.BAD_REQUEST, StringUtil.constructStringEmptySeparator("Field name change Not allowd from '", fieldOld.getName(), "' To '", fieldNew.getName(),"'")));
            }
            if (Boolean.FALSE.equals(fieldNew.getDataType().equals(fieldOld.getDataType()))) {
                throw new BadRequestException(ErrorCode.BAD_REQUEST, responseMessage.getErrorMessage(ErrorCode.BAD_REQUEST, StringUtil.constructStringEmptySeparator("Field Datatype change Not allowd in field '", fieldOld.getName(), "'")));
            }
        } else {
            if (fieldNames.contains(fieldNew.getName())) {
                throw new AlreadyExistException(ErrorCode.ALREADY_EXISTS, responseMessage.getErrorMessage(ErrorCode.ALREADY_EXISTS, StringUtil.constructStringEmptySeparator("Field name '", fieldNew.getName(), "' Already exists")));
            }
        }
        var error = fieldValidator.apply(fieldNew);  /// mock
            if (error.status().equals(Status.ERROR)) {
                throw new BadRequestException(ErrorCode.FIELD_VALIDATION_ERROR, responseMessage.getErrorMessage(ErrorCode.FIELD_VALIDATION_ERROR, error.errorMessages()));
        }
    }

    // public Long protobufSchemaPrRaise(TableDto modifiedTable, ClientRepo clientRepo, Workflow workflow) {
    //     var tableNew = modifiedTable.toDomain(modifiedTable);
    //     String message;
    //     if (Objects.nonNull(modifiedTable.id())) {
    //         var tableOld = tableService.getModel(modifiedTable.id());
    //         message = compareAndGenerateMessageForExistingTable(tableNew, tableOld);
    //     } else {
    //         validateTable(tableNew, null);
    //         message = generateMessageForNewTable(tableNew);
    //     }
    //     var prCount = pullRequestService.getCountWithWorkflow(clientRepo.getId());
    //     var fileName = StringUtil.constructStringEmptySeparator("V", Long.toString(prCount+1), ".0__schemalabs_pr.txt");
    //     var githubPath = githubFilePath.replace("{repoFullName}", clientRepo.getFullName())
    //                                        .replace("{branch}", clientRepo.getDefaultBranch())
    //                                        .replace("{filePath}", fileName);
    //     var schemaFileAudit = SchemaFileAudit.builder().clientRepoId(clientRepo.getId()).path(githubPath).build();
    //     Map<SchemaFileAudit, String> fileContentMap = new HashMap<>();
    //     fileContentMap.put(schemaFileAudit, message);
    //     System.out.println("path = "+githubPath+" fileName = "+fileName);
    //     System.out.println("Message = "+message);
    //     var pullRequest = gitHubService.commitAndRaisePr(fileContentMap, workflow);
    //     return pullRequest.getId();
    // }

    // public String generateMessageForNewTable(Table tableNew) {
    //     StringBuilder message = new StringBuilder("New Table Added: \n");
    //     message.append("1. Table Name: ").append(tableNew.getName()).append("\n");
    //     message.append("2. Table description: ").append(tableNew.getDescription()).append("\n");
    //     message.append("3. Table owner: ").append(tableNew.getOwner()).append("\n");
    //     int sNo = 4;
    //     if (Objects.nonNull(tableNew.getDomain()))
    //         message.append(sNo++).append(". Table domain: ").append(tableNew.getDomain()).append("\n");
    //     if (Objects.nonNull(tableNew.getChannel())) 
    //         message.append(sNo++).append(". Table channel: ").append(tableNew.getChannel()).append("\n");
    //     if (Objects.nonNull(tableNew.getEmail())) 
    //         message.append(sNo++).append(". Table E=mail: ").append(tableNew.getEmail()).append("\n");
    //     if (Objects.nonNull(tableNew.getComplianceOwner()))
    //         message.append(sNo++).append(". Table Compliance owner: ").append(tableNew.getComplianceOwner()).append("\n");
    //     if (Objects.nonNull(tableNew.getStatus()))
    //         message.append(sNo++).append(". Table status: ").append(tableNew.getStatus()).append("\n");
    //     if (Objects.nonNull(tableNew.getQualityRuleBase()))
    //         message.append(sNo++).append(". Table Quality Rule Base: ").append(tableNew.getQualityRuleBase()).append("\n");
    //     if (Objects.nonNull(tableNew.getQualityRuleSql()))
    //         message.append(sNo++).append(". Table Quality Rule Sql: ").append(tableNew.getQualityRuleSql()).append("\n");
    //     if (Objects.nonNull(tableNew.getQualityRuleCel()))
    //         message.append(sNo++).append(". Table Quality Rule Cel: ").append(tableNew.getQualityRuleCel()).append("\n");
    //     if (Objects.nonNull(tableNew.getSubscribers()))
    //         message.append(sNo++).append(". Table subscibers: ").append(Arrays.toString(tableNew.getSubscribers())).append("\n");
    //     message.append("\nField Added: \n"); sNo = 1;
    //     for (Field field : tableNew.getFields()) {
    //         message.append("field ").append(sNo++).append(": \n");
    //         message.append("    * name = ").append(field.getName()).append("\n");
    //         message.append("    * description = ").append(field.getDescription()).append("\n");
    //         message.append("    * is_pii = ").append(field.getIsPii()).append("\n");
    //         message.append("    * is_classified = ").append(field.getIsClassified()).append("\n");
    //         message.append("    * depricated = ").append(field.getDeprecated()).append("\n");
    //     }
    //     return message.toString();
    // }

    // public String compareAndGenerateMessageForExistingTable(Table tableNew, Table tableOld) {
    //     validateTable(tableNew, tableOld);
    //     var schemaFileAudit = schemaFileAuditService.getByModelNodeId(tableOld.getId());
    //     StringBuilder message = new StringBuilder("Changes in Table '").append(tableOld.getName()).append("' in file - ").append(schemaFileAudit.get(0).getPath()).append("\n");
    //     int sNo = 1;
    //     if (tableService.compareString(tableNew.getDescription(), tableOld.getDescription()))
    //         message.append(sNo++).append(". Changes in Table description from - '").append(tableOld.getDescription())
    //                 .append("' To -'").append(tableNew.getDescription()).append("' \n");
    //     if (tableService.compareString(tableNew.getOwner(), tableOld.getOwner()))
    //         message.append(sNo++).append(". Changes in Table metadata Owner from = '").append(tableOld.getOwner())
    //                 .append("' To -'").append(tableNew.getOwner()).append("' \n");
    //     if (tableService.compareString(tableNew.getDomain(), tableOld.getDomain()))
    //         message.append(sNo++).append(". Changes in Table metadata Domain from = '").append(tableOld.getDomain())
    //                 .append("' To -'").append(tableNew.getDomain()).append("' \n");
    //     if (tableService.compareString(tableNew.getComplianceOwner(), tableOld.getComplianceOwner()))
    //         message.append(sNo++).append(". Changes in Table metadata ComplianceOwner from = '").append(tableOld.getComplianceOwner())
    //                 .append("' To -'").append(tableNew.getComplianceOwner()).append("' \n");
    //     if (tableService.compareString(tableNew.getChannel(), tableOld.getChannel()))
    //         message.append(sNo++).append(". Changes in Table metadata Channel from = '").append(tableOld.getChannel())
    //                 .append("' To -'").append(tableNew.getChannel()).append("' \n");
    //     if (tableService.compareString(tableNew.getEmail(), tableOld.getEmail()))
    //         message.append(sNo++).append(". Changes in Table metadata E-mail from = '").append(tableOld.getEmail())
    //                 .append("' To -'").append(tableNew.getEmail()).append("' \n");
    //     if (tableService.compareString(tableNew.getStatus(), tableOld.getStatus()))
    //         message.append(sNo++).append(". Changes in Table metadata status from = '").append(tableOld.getStatus())
    //                 .append("' To -'").append(tableNew.getStatus()).append("' \n");
    //     if (tableService.compareString(tableNew.getQualityRuleBase(), tableOld.getQualityRuleBase()))
    //         message.append(sNo++).append(". Changes in Table metadata Quality Rule Base from = '").append(tableOld.getQualityRuleBase())
    //                 .append("' To -'").append(tableNew.getQualityRuleBase()).append("' \n");
    //     if (tableService.compareString(tableNew.getQualityRuleSql(), tableOld.getQualityRuleSql()))
    //         message.append(sNo++).append(". Changes in Table metadata Quality Rule Sql from = '").append(tableOld.getQualityRuleSql())
    //                 .append("' To -'").append(tableNew.getQualityRuleSql()).append("' \n");
    //     if (tableService.compareString(tableNew.getQualityRuleCel(), tableOld.getQualityRuleCel()))
    //         message.append(sNo++).append(". Changes in Table metadata Quality Rule Cel from = '").append(tableOld.getQualityRuleCel())
    //                 .append("' To -'").append(tableNew.getQualityRuleCel()).append("' \n");
    //     if (!Arrays.deepEquals(tableNew.getSubscribers(), tableOld.getSubscribers()))
    //         message.append(sNo++).append(". Changes in Table metadata subscribers from = '").append(Arrays.toString(tableOld.getSubscribers()))
    //                 .append("' To -'").append(Arrays.toString(tableNew.getSubscribers())).append("' \n");

    //     var oldFields = tableOld.getFields().stream().collect(Collectors.toMap(Field::getName, Function.identity()));
    //     for (Field fieldNew : tableNew.getFields()) {
    //         var fieldOld = oldFields.get(fieldNew.getName());
    //         if (Objects.nonNull(fieldOld)) {
    //             if (tableService.compareField(fieldNew, fieldOld, RepoType.PROTOBUF)) {
    //                 message.append(sNo++).append(". Changes in metadatas of field - '").append(fieldNew.getName()).append("': \n");
    //                 if (tableService.compareString(fieldNew.getDescription(), fieldOld.getDescription())) {
    //                     message.append("    * metadata description from = '").append(Arrays.toString(tableOld.getSubscribers()))
    //                             .append("'' To -'").append(Arrays.toString(tableNew.getSubscribers())).append("' \n");
    //                 }
    //                 if (tableService.compareBoolean(fieldNew.getIsPii(), fieldOld.getIsPii())) {
    //                     message.append("    * metadata is_pii from = '").append(fieldOld.getIsPii())
    //                             .append("'' To -'").append(fieldNew.getIsPii()).append("' \n");
    //                 }
    //                 if (tableService.compareBoolean(fieldNew.getIsClassified(), fieldOld.getIsClassified())) {
    //                     message.append("    * metadata is_classified from = '").append(fieldOld.getIsClassified())
    //                             .append("'' To -'").append(fieldNew.getIsClassified()).append("' \n");
    //                 }
    //                 if (tableService.compareBoolean(fieldNew.getDeprecated(), fieldOld.getDeprecated())) {
    //                     message.append("    * metadata Deprecated from = '").append(fieldOld.getDeprecated())
    //                             .append("'' To -'").append(fieldNew.getDeprecated()).append("' \n");
    //                 }
    //             }
    //         } else {
    //             message.append(sNo++).append(". New Field Added: \n");
    //             message.append("    * name = ").append(fieldNew.getName()).append("\n");
    //             message.append("    * description = ").append(fieldNew.getDescription()).append("\n");
    //             message.append("    * is_pii = ").append(fieldNew.getIsPii()).append("\n");
    //             message.append("    * is_classified = ").append(fieldNew.getIsClassified()).append("\n");
    //             message.append("    * depricated = ").append(fieldNew.getDeprecated()).append("\n");
    //         }
    //     }
    //     return message.toString();
    // }
}
