package com.opsbeach.connect.github.service;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.PullRequest;
import com.opsbeach.connect.github.entity.ClientRepo.RepoSource;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.repository.SchemaFileAuditRepository;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.processor.avro.AvroSchema;
import com.opsbeach.connect.schemata.processor.json.JsonSchema;
import com.opsbeach.connect.schemata.processor.protobuf.ProtoSchema;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.exception.SchemaParserException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.utils.FileUtil;
import com.opsbeach.sharedlib.utils.StringUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaFileAuditService {

    private final SchemaFileAuditRepository schemaFileAuditRepository;
    private final DomainService domainService;
    private final ModelService modelService;
    private final TableService tableService;
    private final IdSpecifications<SchemaFileAudit> scmFileAuditpecifications;
    private final EntityManager entityManager;
    private final ResponseMessage responseMessage;
    @Lazy
    @Autowired
    private AvroSchema avroSchema;
    @Lazy
    @Autowired
    private JsonSchema jsonSchema;
    @Lazy
    @Autowired
    private ProtoSchema protoSchema;

    @Value("${server.home-path}")
    private String homePath;
    @Value("${github.construct-file-path}")
    private String githubFilePath;

    private static final String PULL_REQUEST_ID = "pullRequestId";

    public SchemaFileAudit addModel(SchemaFileAudit schemaFileAudit) {
        return schemaFileAuditRepository.save(schemaFileAudit);
    }

    public SchemaFileAudit createSchemaFileAuditWhileInitialLoading(String filePath, ClientRepo clientRepo, Long rootNodeId) {
        var name = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
        var fileType = filePath.substring(filePath.lastIndexOf(".") + 1);
        var schemaFileAudit =  SchemaFileAudit.builder().name(name).fileType(fileType).checksum(FileUtil.getChecksum(filePath))
                                              .path(constructPath(clientRepo, filePath))
                                              .clientRepoId(clientRepo.getId())
                                              .rootNodeId(rootNodeId)
                                              .build();
        return addModel(schemaFileAudit);
    }

    private String constructPath(ClientRepo clientRepo, String filePath) {
        return clientRepo.getRepositorySource().equals(RepoSource.GITHUB) ? getGithubPath(clientRepo.getFullName(), clientRepo.getDefaultBranch(), filePath) : filePath;
    }

    public SchemaFileAudit createSchemaFileAuditForNewFile(ClientRepo clientRepo, Table table, String rootFilePath) {
        var fileType = "";
        if (clientRepo.getRepoType().equals(RepoType.AVRO)) fileType = "avsc";
        if (clientRepo.getRepoType().equals(RepoType.PROTOBUF)) fileType = "proto";
        if (clientRepo.getRepoType().equals(RepoType.JSON)) {
            fileType = "json";
            table = addJsonSchemaId(table);
        }
        var filePath = StringUtil.constructStringEmptySeparator(table.getNameSpace().replace(".", "/"), "/", table.getName(), ".", fileType);
        filePath = Objects.isNull(rootFilePath) ? filePath : rootFilePath.endsWith("/") ? StringUtil.constructStringEmptySeparator(rootFilePath, filePath)
                                                                                        : StringUtil.constructStringEmptySeparator(rootFilePath, "/", filePath);
        var githubPath = githubFilePath.replace("{repoFullName}", clientRepo.getFullName())
                                           .replace("{branch}", clientRepo.getDefaultBranch())
                                           .replace("{filePath}", filePath);
        var schemaFileAudit = createSchemaFileAudit(githubPath, filePath, table.getName(), fileType, null, clientRepo, null);
        schemaFileAudit.setRootNodeId(table.getId());
        return addModel(schemaFileAudit);
    }

    // this method is to generate JsonSchemaId for newly added table.
    private Table addJsonSchemaId(Table table) {
        // var folderPath = table.getNameSpace().replace(".", "/");
        // var fileName = table.getName().concat(".json");
        // table.setJsonSchemaId(StringUtil.constructStringEmptySeparator("http://example.com/", folderPath, "/", fileName));
        var id = StringUtil.constructStringEmptySeparator("/", table.getNameSpace(), "/", table.getName()).replace(".", "/");
        table.setJsonSchemaId(id);
        return tableService.addTable(table);
    }

    private String getGithubPath(String repoFullName, String branchName, String filePath) {
        StringBuilder githubPath = new StringBuilder(StringUtil.constructStringEmptySeparator("https://github.com/",repoFullName,"/tree/",branchName));
        String[] folders = filePath.split("/");
        // To get the root folder path of repo. so that we need starting index of downloaded repo.
        int folderPathLength = homePath.split("/").length + 2;
        for (int i=folderPathLength; i < folders.length; i++) {
          githubPath.append('/').append(folders[i]);
        }
        return githubPath.toString();
    }

    public List<Table> getTablesFromFileContent(byte[] fieldContent, Boolean toSave, String fileType) throws IOException {
        switch (fileType) {
            case "avsc": return avroSchema.getTables(fieldContent, toSave);
            case "json": return jsonSchema.getTables(fieldContent, toSave);
            default: return null;
        }
    }

    @Transactional
    public int updateSchemaFileAuditSetPrIdToNull(Long prId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaUpdate<SchemaFileAudit> update = criteriaBuilder.createCriteriaUpdate(SchemaFileAudit.class);
        Root<SchemaFileAudit> root = update.from(SchemaFileAudit.class);

        update.set(PULL_REQUEST_ID, null).where(criteriaBuilder.equal(root.get(PULL_REQUEST_ID), prId));

        return entityManager.createQuery(update).executeUpdate();
    }

    @Transactional
    public int deleteSchemaFileAuditByPrId(Long prId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<SchemaFileAudit> delete = criteriaBuilder.createCriteriaDelete(SchemaFileAudit.class);
        Root<SchemaFileAudit> root = delete.from(SchemaFileAudit.class);

        delete.where(criteriaBuilder.equal(root.get(PULL_REQUEST_ID), prId));

        return entityManager.createQuery(delete).executeUpdate();
    }

    public List<Table> saveDeltaForProtoSchema(String[] filePaths, ClientRepo clientRepo, PullRequest pullRequest) {
        Map<String, List<Table>> fileTables = protoSchema.getTablesOfFilePaths(filePaths, clientRepo, pullRequest.getSourceBranch());
        return saveDeltaOfTables(fileTables, clientRepo, pullRequest.getId());
    }

    public List<Table> saveDelta(Map<String, byte[]> fileContentMap, ClientRepo clientRepo, Long prId) throws IOException {
        Map<String, List<Table>> fileTables = new HashMap<>();
        fileContentMap.entrySet().forEach(fileContent -> {
            var filePath = fileContent.getKey();
            var fileType = filePath.substring(filePath.lastIndexOf(".") + 1);
            List<Table> tables = new ArrayList<>();
            try {
                tables = getTablesFromFileContent(fileContent.getValue(), Boolean.FALSE, fileType);
            } catch (IOException e) {
                log.info(e.getMessage());
                throw new SchemaParserException(e.getMessage());
            }
            fileTables.put(filePath, tables);
        });
        return saveDeltaOfTables(fileTables, clientRepo, prId);
    }

    private List<Table> saveDeltaOfTables(Map<String, List<Table>> fileTables, ClientRepo clientRepo, Long prId) {
        Map<String, List<Field>> tableWithNewFields = new HashMap<>();
        Map<String, Long> tableIds = new HashMap<>();
        var domain = domainService.getDefaultDomain(clientRepo.getFullName());
        fileTables.entrySet().forEach(fileTable -> {
            var tables = fileTable.getValue();
            var filePath = fileTable.getKey();
            log.info("saving delta for - "+filePath);
            var githubPath = githubFilePath.replace("{repoFullName}", clientRepo.getFullName())
                                           .replace("{branch}", clientRepo.getDefaultBranch())
                                           .replace("{filePath}", filePath);
            var fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
            var fileType = filePath.substring(filePath.lastIndexOf(".") + 1);
            var schemaFileAudit = getSchemaFileAudit(githubPath, null);  //send prId is null then only it will fetch file details of main branch
            if (schemaFileAudit == null) {
                // if schemaFileAudit is not present then the incoming file is new file.
                schemaFileAudit = createSchemaFileAudit(githubPath, filePath, fileName, fileType, null, clientRepo, prId);
                // schemaFileAudit = createSchemaFileAudit(githubPath, filePath, fileName, fileType, FileUtil.getChecksum(fileContent.getValue()), clientRepo, prId);
            }
            var models = modelService.findBySchemaFileAudit(schemaFileAudit.getId());
            Map<String, Table> tableMap = new HashMap<>();
            tables.stream().forEach(table -> {
                tableMap.put(StringUtil.constructStringEmptySeparator(table.getNameSpace(),".",table.getName()), table);
            });
            log.info("tableMap length = "+tableMap.size());
            models.forEach(model -> {
                var fullName = StringUtil.constructStringEmptySeparator(model.getNameSpace(),".",model.getName());
                if (ObjectUtils.isEmpty(tableWithNewFields.get(fullName))) {
                    var tableNew = tableMap.get(fullName);
                    tableWithNewFields.put(fullName, tableNew.getFields());
                    // call tableService and save delta in neo4j;
                    var table = tableService.findDeltaForTable(tableNew, model.getNodeId(), prId);
                    tableIds.put(fullName, table.getId());
                }
                tableMap.remove(fullName);
            });
            for (Map.Entry<String, Table> entry : tableMap.entrySet()) {
                // if size is greater than 0, that's tell that new schema is added in this file.
                // CALL TABLE SERVICE TO ADD THIS TABLE.
                // ADD IN tableDeltas list
                var fullName = StringUtil.constructStringEmptySeparator(entry.getValue().getNameSpace(),".",entry.getValue().getName());
                var table = entry.getValue();
                if (ObjectUtils.isEmpty(tableWithNewFields.get(fullName))) {
                    tableWithNewFields.put(fullName, entry.getValue().getFields());
                    table = tableService.saveNewTableWithDelta(table, prId);
                    tableIds.put(fullName, table.getId());
                }
                table.setId(tableIds.get(fullName));
                var model = modelService.createModel(table, schemaFileAudit, domain, prId);
                modelService.addModel(model);
            }
            if (ObjectUtils.isEmpty(schemaFileAudit.getRootNodeId())) {
                var table = tables.get(tables.size()-1);
                var fullName = StringUtil.constructStringEmptySeparator(table.getNameSpace(),".",table.getName());
                schemaFileAudit.setRootNodeId(tableIds.get(fullName));
                addModel(schemaFileAudit);
            }
        });
        var tableDeltas = tableService.findDeltaForFields(tableWithNewFields, prId, clientRepo.getRepoType());
        return tableDeltas; 
    }

    public SchemaFileAudit createSchemaFileAudit(String path, String filePath, String fileName, String fileType, String checkSum, ClientRepo clientRepo, Long prId) {
        var schemaFileAudit = SchemaFileAudit.builder().name(fileName).fileType(fileType).checksum(checkSum)
                                             .path(path).clientRepoId(clientRepo.getId()).clientId(clientRepo.getClientId()).pullRequestId(prId)
                                             .build();
        return addModel(schemaFileAudit);
    }

    public SchemaFileAudit getModel(Long id) {
        return schemaFileAuditRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.SCHEMA_FILE_AUDIT)));
    }

    public List<SchemaFileAudit> getByModelNodeId(Long nodeId) {
        return schemaFileAuditRepository.findByModelNodeId(nodeId);
    }

    public List<SchemaFileAudit> getByModelNodeIds(List<Long> nodeIds) {
        return schemaFileAuditRepository.findByModelNodeIds(nodeIds);
    }

    public SchemaFileAudit getSchemaFileAudit(String path) {
        return schemaFileAuditRepository.findOne(scmFileAuditpecifications.findByPath(path)).orElse(null);
    }

    public SchemaFileAudit getSchemaFileAudit(String path, Long prId) {
        var spec = scmFileAuditpecifications.findByPath(path).and(scmFileAuditpecifications.findByPullRequest(prId));
        return schemaFileAuditRepository.findOne(spec).orElse(null);
    }

    public List<SchemaFileAudit> getAll(Long clientRepoId) {
        Specification<SchemaFileAudit> specification = Specification.where(null);
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(clientRepoId))) {
            specification = specification.and(scmFileAuditpecifications.findByClientRepoId(clientRepoId));
        }
        return schemaFileAuditRepository.findAll(specification);
    }

    /*
     * Generate file content map where the given table is present.
     */
    public Map<SchemaFileAudit, String> generateFileContentOfSchema(Long tableId, RepoType repoType) {
        if (repoType.equals(RepoType.PROTOBUF)) return protoSchema.generateSchema(tableId);
        var schemaFileAudits = getByModelNodeId(tableId);
        var schemaFileAuditMap = schemaFileAudits.stream().collect(Collectors.toMap(SchemaFileAudit::getRootNodeId, Function.identity()));
        var tables = tableService.findAllById(new ArrayList<>(schemaFileAuditMap.keySet()));
        return generateFileContentOfSchema(schemaFileAuditMap, tables);
    }

    public Map<SchemaFileAudit, String> generateFileContentOfSchema(List<Long> tableIds) {
        var schemaFileAudits = getByModelNodeIds(tableIds);
        var schemaFileAuditMap = schemaFileAudits.stream().collect(Collectors.toMap(SchemaFileAudit::getRootNodeId, Function.identity()));
        var tables = tableService.findAllById(new ArrayList<>(schemaFileAuditMap.keySet()));
        return generateFileContentOfSchema(schemaFileAuditMap, tables);
    } 

    private Map<SchemaFileAudit, String> generateFileContentOfSchema(Map<Long, SchemaFileAudit> schemaFileAuditMap, List<Table> tables) {
        Map<SchemaFileAudit, String> map = new HashMap<>();
        tables.forEach(table -> {
            // need to filter unwanted fields.
            var table2 = filterFields(table);
            if (schemaFileAuditMap.get(table.getId()).getFileType().equalsIgnoreCase("avsc")) {
                map.put(schemaFileAuditMap.get(table.getId()), avroSchema.getFileContent(table2));
            }
            if (schemaFileAuditMap.get(table.getId()).getFileType().equalsIgnoreCase("json")) {
                map.put(schemaFileAuditMap.get(table.getId()), jsonSchema.getFileContent(table2));
            }
        });
        return map;
    }

    // this method will filter the fields in the table which is in other PR's 
    public Table filterFields(Table table) {
        List<Field> fields = table.getFields();
        Map<Long, Field> fieldMap = table.getFields().stream().collect(Collectors.toMap(Field::getId, Function.identity()));
        fields.forEach(field -> {
            if (field.getIsUserChanged().equals(Boolean.TRUE)) {
                if (ObjectUtils.isEmpty(field.getPrId()) && field.getIsDeleted().equals(Boolean.TRUE)) {
                    fieldMap.remove(field.getId());
                    return;
                }
                if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getPrId()))) {
                    fieldMap.remove(field.getId());
                    return;
                }
            }
            checkNestedTable(field);
        });
        // check the table is modified now then transfer values from new table to old table for raising PR.
        if ((table.getIsUserChanged().equals(Boolean.TRUE) && table.getIsDeleted().equals(Boolean.TRUE)) && Objects.isNull(table.getPrId())) {
            tableService.transferTableProps(table, table.getModifiedTable());
        }
        Field[] orderedFields = new Field[fieldMap.size()];
        // order the field according to row number.
        for (Field field : fieldMap.values())
            orderedFields[field.getRowNumber()-1] = field;
        table.setFields(Arrays.asList(orderedFields));
        return table;
    }

    private void checkNestedTable(Field field) {
        if (field.getIsPrimitiveType().equals(Boolean.FALSE)) {
            if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getContain()))) {
                var table = filterFields(field.getContain());
                field.setContain(table);
            }
            switch(field.getDataType()) {
                case "array" -> checkNestedTableArrayField(field);
                case "map" -> checkNestedTableMapField(field);
                case "union" -> checkNestedTableUnionField(field);
            }
        }
    }

    private void checkNestedTableArrayField(Field field) {
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getContain()))) {
            var table = filterFields(field.getContain());
            field.setContain(table);
        }
        switch(field.getItems()) {
            case "array" -> checkNestedTableArrayField(field.getArrayField());
            case "map" -> checkNestedTableMapField(field.getMapField());
            case "union" -> checkNestedTableUnionField(field);
        }
    }

    private void checkNestedTableMapField(Field field) {
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getContain()))) {
            var table = filterFields(field.getContain());
            field.setContain(table);
        }
        switch(field.getValues()) {
            case "array" -> checkNestedTableArrayField(field.getArrayField());
            case "map" -> checkNestedTableMapField(field.getMapField());
            case "union" -> checkNestedTableUnionField(field);
        }
    }

    private void checkNestedTableUnionField(Field field) {
        field.getUnionTypes().stream().forEach(unionField -> checkNestedTable(unionField));
    }
    
    public void deleteAllByClientRepoId(Long clientRepoId) {
        schemaFileAuditRepository.deleteAllByClientRepoId(clientRepoId);
    }
}
