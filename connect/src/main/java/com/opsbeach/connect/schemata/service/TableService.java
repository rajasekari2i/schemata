package com.opsbeach.connect.schemata.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.neo4j.driver.internal.InternalNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.service.ClientRepoService;
import com.opsbeach.connect.github.service.GitHubService;
import com.opsbeach.connect.github.service.ModelService;
import com.opsbeach.connect.github.service.SchemaFileAuditService;
import com.opsbeach.connect.schemata.dto.FieldDto;
import com.opsbeach.connect.schemata.dto.SchemaValidationDto;
import com.opsbeach.connect.schemata.dto.TableDto;
import com.opsbeach.connect.schemata.dto.TableFilterOptionsDto;
import com.opsbeach.connect.schemata.dto.SchemaVisualizerDto;
import com.opsbeach.connect.schemata.dto.TableCsvDto;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.graph.SchemaGraph;
import com.opsbeach.connect.schemata.repository.FieldRepostory;
import com.opsbeach.connect.schemata.repository.TableRepository;
import com.opsbeach.connect.schemata.validate.SchemaValidator;
import com.opsbeach.connect.schemata.validate.Status;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.utils.FileUtil;
import com.opsbeach.sharedlib.utils.JsonUtil;
import com.opsbeach.sharedlib.utils.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final FieldRepostory fieldRepostory;
    private final ResponseMessage responseMessage;
    private final ModelService modelService;
    private final SchemaValidator schemaValidator;
    private final Neo4jClient neo4jClient;
    private final ClientRepoService clientRepoService;

    @Lazy
    @Autowired
    private GitHubService gitHubService;

    @Lazy @Autowired
    private SchemaFileAuditService schemaFileAuditService;
    @Value("${github.construct-file-path}")
    private String githubFilePath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Table addTable(Table table) {
        return tableRepository.save(table);
    }

    @Transactional
    public List<Table> addTables(List<Table> tables) {
        return tableRepository.saveAll(tables);
    }

    public List<Table> getAll() {
        var ids = modelService.getNodeIds();
        return findAllById(ids);
    }

    public List<Table> findAllById(List<Long> ids) {
        return tableRepository.findAllById(ids);
    }

    public void deleteByIds(List<Long> ids) {
        var tables = tableRepository.findAllById(ids);
        List<Long> fieldIds = new ArrayList<>();
        tables.forEach(table -> {
            table.getFields().forEach(field -> {
                fieldIds.add(field.getId());
                if (Objects.nonNull(field.getArrayField())) fieldIds.add(field.getArrayField().getId());
                else if (Objects.nonNull(field.getMapField())) fieldIds.add(field.getMapField().getId());
                else if (Objects.nonNull(field.getUnionTypes())) fieldIds.addAll(field.getUnionTypes().stream().map(Field::getId).toList());
            });
        });
        fieldRepostory.deleteAllById(fieldIds);
        tableRepository.deleteAllById(ids);
    }

    public Table findByNameAndNameSpace(String name, String nameSpace) {
        return tableRepository.findByNameAndNameSpaceAndClientId(name, nameSpace, SecurityUtil.getClientId());
    }

    public TableFilterOptionsDto getTableFilterOptions() {
        var clientId = SecurityUtil.getClientId();
        var owners = tableRepository.getAllOwner(clientId);
        var domains = tableRepository.getAllDomain(clientId);
        var subscriberArrs = tableRepository.getAllSubscribers(clientId);
        Set<String> subscribers = new HashSet<>();
        subscriberArrs.stream().forEach(subscriber -> {
            var subs = JsonUtil.jsonArrayToObjectList(String.valueOf(subscriber), String.class);
            subs.forEach(sub -> subscribers.add(sub));
        });
        return new TableFilterOptionsDto(owners, domains, subscribers);
    }

    private StringBuilder generateQueryForGetTables(List<String> owners, List<String> domains, List<String> subscribers, Map<String, Object> params) {
        boolean isOwnerEmpty = ObjectUtils.isEmpty(owners);
        boolean isDomainEmpty = ObjectUtils.isEmpty(domains);
        boolean isSubscribersEmpty = ObjectUtils.isEmpty(subscribers);
        var query = new StringBuilder("""
            MATCH (a:Table) 
            OPTIONAL MATCH (a)-[]->(b:Table)
            WITH a, b, CASE WHEN b IS NULL THEN a ELSE b END AS selectedNode
            WHERE a.clientId = $clientId
        """);
        params.put("clientId", SecurityUtil.getClientId());
        if (!isOwnerEmpty) {
            query.append(" AND selectedNode.owner IN $owners");
            params.put("owners", owners);
            if (owners.contains("default"))
                query.append(" OR selectedNode.owner IS NULL");
        }
        if (!isDomainEmpty) {
            query.append(" AND selectedNode.domain IN $domains");
            params.put("domains", domains);
            if (domains.contains("default"))
                query.append(" OR selectedNode.domain IS NULL");
        }
        if (!isSubscribersEmpty) {
            query.append(" AND any(subscribers IN selectedNode.subscribers WHERE subscribers IN $subscribers)");
            params.put("subscribers", subscribers);
            if (subscribers.contains("default"))
                query.append(" OR selectedNode.subscribers IS NULL");
        }
        return query;
    }
    public JsonNode getAll(List<String> owners, List<String> domains, List<String> subscribers, Pageable pageable) {
        Map<Long, Table> tables = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        var query = generateQueryForGetTables(owners, domains, subscribers, params);
        var getTablesQuery = query.toString();
        var offset = pageable.getPageNumber()*pageable.getPageSize();
        var limit = pageable.getPageSize();
        getTablesQuery = StringUtil.constructStringEmptySeparator(getTablesQuery, " RETURN a,b SKIP "+offset+" LIMIT "+limit);
        var countQuery = query.append(" RETURN count(a) as count").toString();
        neo4jClient.query(getTablesQuery).bindAll(params).fetch().all().stream().forEach(map -> {
            var value = (InternalNode) map.get("a");
            var table = objectMapper.convertValue(value.asMap(), Table.class);
            // table.setId(Long.parseLong(value.elementId()));
            // Newly installed neo4j stores id in different format (Need to test in prod)
            table.setId(Long.parseLong(value.elementId().split(":")[2]));
            value = (InternalNode) map.get("b");
            if (value != null) {
                var modifiedTable = objectMapper.convertValue(value.asMap(), Table.class);
                modifiedTable.setId(Long.parseLong(value.elementId().split(":")[2]));
                table.setModifiedTable(modifiedTable);
            }
            tables.put(table.getId(), table);
        });
        AtomicLong total = new AtomicLong();
        neo4jClient.query(countQuery).bindAll(params).fetch().all().forEach(map -> total.set((long) map.get("count")));
        
        Map<Long, Boolean> isFieldChangedMap = checkIsFieldChanged(tables.keySet().toArray(new Long[0]));
        List<TableDto> tableDtos = new ArrayList<>(pageable.getPageSize());
        tables.values().forEach(table -> {
            table.setFields(null);
            if (Objects.nonNull(table.getModifiedTable())) {
                transferTableProps(table, table.getModifiedTable());
                table.setModifiedTable(null);
            }
            var tableDto = table.toDto(table);
            if (isFieldChangedMap.containsKey(table.getId())) tableDto.setIsFieldChanged(isFieldChangedMap.get(table.getId()));
            tableDtos.add(tableDto);
        });
        var payload = JsonNodeFactory.instance.objectNode();
        payload.set("tables", objectMapper.convertValue(tableDtos, JsonNode.class));
        payload.put("total", total.get());
        return payload;
    }

    public Map<Long, Boolean> checkIsFieldChanged(Long[] tableIds) {
        Map<Long, Boolean> isFieldChangedMap = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("tableIds", tableIds);
        var isFieldChangedQuery = "MATCH (a:Table)-[r:PROPERTIES]->(b:Field) WHERE ID(a) IN $tableIds AND b.prId IS NOT NULL RETURN distinct(ID(a)) AS tableId, true AS isFieldChanged";
        neo4jClient.query(isFieldChangedQuery).bindAll(params).fetch().all().stream().forEach(map -> {
            isFieldChangedMap.put((long) map.get("tableId"), (boolean) map.get("isFieldChanged"));
        });
        return isFieldChangedMap;
    }

    public TableDto get(Long id) {
        var table = getModel(id);
        if (!ObjectUtils.isEmpty(table.getModifiedTable())) {
            // if the table content is changed, then transfer the latest content.
            transferTableProps(table, table.getModifiedTable());
            table.setModifiedTable(null);
        }
        var fields = new ArrayList<Field>();
        var updatedFieldNames = table.getFields().stream().filter(f -> (f.getIsDeleted().equals(Boolean.TRUE))).map(Field::getName).toList();
        table.getFields().stream().filter(f -> (f.getIsDeleted().equals(Boolean.FALSE)))
              .forEach(field -> {
            if (field.getIsUserChanged().equals(Boolean.TRUE) && updatedFieldNames.contains(field.getName())) {
                // if the field is updated field then the above condition will true.
                field.setIsDeleted(Boolean.TRUE); // to show that the difference between updated and new field in UI.
            }
            fields.add(field);
        });
        table.setFields(fields);
        var tableDto = table.toDto(table);
        var clientRepo = modelService.getByNodeId(tableDto.getId());
        tableDto.setDataTypes(fieldDataTypes(clientRepo.getRepoType()));
        tableDto.setClientRepoId(clientRepo.getId());
        return tableDto;
    }

    public Table getModel(Long id) {
        return tableRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.TABLE)));
    }

    public Field getFieldModel(Long id) {
        return fieldRepostory.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.FIELD)));
    }

    public SchemaVisualizerDto getSchemaVisualizerForAll() {
        return buildSchemaVisualizerDto(getAll());
    }

    public SchemaVisualizerDto getSchemaVisualizer(Long tableId) {
        Set<Long> tableIds = tableRepository.getTableIdsConnectedToTable(tableId);
        tableIds.add(tableId);
        var tables = tableRepository.findAllById(tableIds);
        return buildSchemaVisualizerDto(tables);
    }

    public SchemaVisualizerDto buildSchemaVisualizerDto(List<Table> tables) {
        List<TableDto> tableDtos = new ArrayList<>();
        List<FieldDto> fieldDtos = new ArrayList<>();
        List<Map<String, Long>> links = new ArrayList<>();
        List<Long> addedTables = new ArrayList<>();
        tables.stream().forEach(table -> {
            if (Boolean.FALSE.equals(addedTables.contains(table.getId()))) {
                var schemaVisualizerDto = buildSchemaVisualizerDto(table, addedTables);
                tableDtos.addAll(schemaVisualizerDto.getTables());
                fieldDtos.addAll(schemaVisualizerDto.getFields());
                links.addAll(schemaVisualizerDto.getLinks());
                addedTables.add(table.getId());
            }
        });
        return SchemaVisualizerDto.builder().tables(tableDtos).fields(fieldDtos).links(links).build();
    }

    private SchemaVisualizerDto buildSchemaVisualizerDto(Table table, List<Long> addedTables) {
        var fields = table.getFields();
        table.setFields(null);
        List<TableDto> tableDtos = new LinkedList<>();
        List<FieldDto> fieldDtos = new LinkedList<>();
        if (table.getIsUserChanged().equals(Boolean.TRUE) && table.getIsDeleted().equals(Boolean.TRUE)){
            transferTableProps(table, table.getModifiedTable());
            table.setModifiedTable(null);
        }
        tableDtos.add(table.toDto(table));
        List<Map<String, Long>> links = new LinkedList<>();
        var updatedFieldNames = fields.stream().filter(f -> (f.getIsDeleted().equals(Boolean.TRUE))).map(Field::getName).toList();
        fields.stream().filter(f -> (f.getIsDeleted().equals(Boolean.FALSE)))
              .forEach(field -> {
            if (field.getIsUserChanged().equals(Boolean.TRUE) && updatedFieldNames.contains(field.getName())) {
                // if the field is updated field then the above condition will true.
                field.setIsDeleted(Boolean.TRUE); // to show that the difference between updated and new field in UI.
            }
            createLink(links, table.getId(), field.getId());
            buildSchemaVisualizerDto(field, fieldDtos, tableDtos, links, addedTables);
        });
        return SchemaVisualizerDto.builder().tables(tableDtos).fields(fieldDtos).links(links).build();
    }

    private void createLink(List<Map<String, Long>> links, Long sourceId, long targetId) {
        links.add(Map.of("source", sourceId, "target", targetId));
    }

    private void buildSchemaVisualizerDto(Field field, List<FieldDto> fieldDtos, List<TableDto> tableDtos, 
                               List<Map<String, Long>> links, List<Long> addedTables) {
        
        if (!ObjectUtils.isEmpty(field.getContain())) {
            var table = field.getContain(); field.setContain(null);
            createLink(links, field.getId(), table.getId());
            if (Boolean.FALSE.equals(addedTables.contains(table.getId()))) {
                var schemaVisualizerDto = buildSchemaVisualizerDto(table, addedTables);
                tableDtos.addAll(schemaVisualizerDto.getTables());
                fieldDtos.addAll(schemaVisualizerDto.getFields());
                links.addAll(schemaVisualizerDto.getLinks());
                addedTables.add(table.getId());
            }
        }
        else if (!ObjectUtils.isEmpty(field.getArrayField())) {
            var arrayField = field.getArrayField(); field.setArrayField(null);
            createLink(links, field.getId(), arrayField.getId());
            buildSchemaVisualizerDto(arrayField, fieldDtos, tableDtos, links, addedTables);
        }
        else if (!ObjectUtils.isEmpty(field.getMapField())) {
            var mapField = field.getMapField(); field.setMapField(null);
            createLink(links, field.getId(), mapField.getId());
            buildSchemaVisualizerDto(mapField, fieldDtos, tableDtos, links, addedTables);
        }
        else if (!ObjectUtils.isEmpty(field.getUnionTypes())) {
            var unionFields = field.getUnionTypes();   field.setUnionTypes(null);
            unionFields.stream().forEach(unionField -> {
                createLink(links, field.getId(), unionField.getId());
                buildSchemaVisualizerDto(unionField, fieldDtos, tableDtos, links, addedTables);
            });
        }
        else if (!ObjectUtils.isEmpty(field.getReferenceField())) {
            // createLink(links, field.getId(), field.getReferenceField().getId());
            var tableId = fieldRepostory.getTableIdOfField(field.getReferenceField().getId());
            createLink(links, field.getId(), tableId);
            var table = getModel(tableId);
            if (Boolean.FALSE.equals(addedTables.contains(table.getId()))) {
                var schemaVisualizerDto = buildSchemaVisualizerDto(table, addedTables);
                tableDtos.addAll(schemaVisualizerDto.getTables());
                fieldDtos.addAll(schemaVisualizerDto.getFields());
                links.addAll(schemaVisualizerDto.getLinks());
                addedTables.add(table.getId());
            }
        }
        fieldDtos.add(field.toDto(field));
    }

    // public List<Table> parseSchemaVisualizerDto(Long rootNodeId, SchemaVisualizerDto schemaVisualizerDto, SchemaVisualizerDto.Purpose purpose) {
    //     var tableDtoMap = schemaVisualizerDto.getTables().stream().collect(Collectors.toMap(TableDto::getId, Function.identity()));
    //     var FieldDtoMap = schemaVisualizerDto.getFields().stream().collect(Collectors.toMap(FieldDto::getId, Function.identity()));
    //     var linkMap = createLinksMap(schemaVisualizerDto.getLinks());
    //     List<Table> tables = new ArrayList<>();
    //     tables.add(getTableFromSchemaVisualizerDto(rootNodeId, tableDtoMap, FieldDtoMap, linkMap, tables, purpose));
    //     return tables;
    // }

    // private Multimap<Long, Long> createLinksMap(List<Map<String, Long>> links) {
    //     Multimap<Long, Long> linkMap = LinkedHashMultimap.create();
    //     links.stream().forEach(link -> {
    //         linkMap.put(link.get("source"), link.get("target"));
    //     });
    //     return linkMap;
    // }

    // private Table getTableFromSchemaVisualizerDto(Long modelId, Map<Long, TableDto> tableDtoMap, Map<Long, FieldDto> fieldDtoMap,
    //                                     Multimap<Long, Long> linkMap, List<Table> tables, SchemaVisualizerDto.Purpose purpose) {
    //     var tableDto = tableDtoMap.get(modelId);
    //     var fieldIds = linkMap.get(tableDto.getId());
    //     List<Field> fields = new ArrayList<>();
    //     fieldIds.stream().forEach(fieldId -> {
    //         fields.add(getFieldFromSchemaVisualizerDto(fieldId, tableDtoMap, fieldDtoMap, linkMap, tables, purpose));
    //     });
    //     var table = tableDto.toDomain(tableDto);
    //     table.setFields(fields);
    //     return table;
    // }

    // private Field getFieldFromSchemaVisualizerDto(Long fieldId, Map<Long, TableDto> tableDtoMap, Map<Long, FieldDto> fieldDtoMap,
    //                                     Multimap<Long, Long> linkMap, List<Table> tables, SchemaVisualizerDto.Purpose purpose) {
    //     var fieldDto = fieldDtoMap.get(fieldId);
    //     var fieldParent = fieldDto.toDomin(fieldDto);
    //     var dataType = fieldParent.getDataType();
    //     var fieldIds = linkMap.get(fieldDto.getId()).toArray(new Long[0]);
    //     List<Field> unionTypes = new ArrayList<>();
    //     // If the field is complex type then the condition will true
    //     if (fieldIds.length > 0) {
    //         if (dataType.equalsIgnoreCase("array")) {
    //             parseNestedField(fieldParent, fieldParent.getItems(), fieldIds, tableDtoMap, fieldDtoMap, linkMap, tables, purpose);
    //         }
    //         else if (dataType.equalsIgnoreCase("map")) {
    //             parseNestedField(fieldParent, fieldParent.getValues(), fieldIds, tableDtoMap, fieldDtoMap, linkMap, tables, purpose);
    //         }
    //         else if (dataType.equalsIgnoreCase("union")) {
    //             for (Long id : fieldIds) {
    //                 unionTypes.add(getFieldFromSchemaVisualizerDto(id, tableDtoMap, fieldDtoMap, linkMap, tables, purpose));
    //                 fieldParent.setUnionTypes(unionTypes);
    //             }
    //         }
    //         else {
    //             var table = getTableFromSchemaVisualizerDto(fieldIds[0], tableDtoMap, fieldDtoMap, linkMap, tables, purpose);
    //             if (purpose.equals(SchemaVisualizerDto.Purpose.VALIDATE)) {
    //                 tables.add(table);
    //             } else {
    //                 fieldParent.setContain(table);
    //             }
    //         }
    //     }
    //     return fieldParent;
    // }

    // private void parseNestedField(Field fieldParent, String nestedType, Long[] fieldIds, Map<Long, TableDto> tableDtoMap, 
    //                             Map<Long, FieldDto> fieldDtoMap, Multimap<Long, Long> linkMap, List<Table> tables, SchemaVisualizerDto.Purpose purpose) {
    //     List<Field> unionTypes = new ArrayList<>();
    //     if (nestedType.equalsIgnoreCase("array")) {
    //         fieldParent.setArrayField(getFieldFromSchemaVisualizerDto(fieldIds[0], tableDtoMap, fieldDtoMap, linkMap, tables, purpose));
    //     }
    //     else if (nestedType.equalsIgnoreCase("map")) {
    //         fieldParent.setMapField(getFieldFromSchemaVisualizerDto(fieldIds[0], tableDtoMap, fieldDtoMap, linkMap, tables, purpose));
    //     }
    //     else if (nestedType.equalsIgnoreCase("union")) {
    //         for (Long id : fieldIds) {
    //             unionTypes.add(getFieldFromSchemaVisualizerDto(id, tableDtoMap, fieldDtoMap, linkMap, tables, purpose));
    //             fieldParent.setUnionTypes(unionTypes);
    //         }
    //     }
    //     else {
    //         var table = getTableFromSchemaVisualizerDto(fieldIds[0], tableDtoMap, fieldDtoMap, linkMap, tables, purpose);
    //         if (purpose.equals(SchemaVisualizerDto.Purpose.VALIDATE)) {
    //             tables.add(table);
    //         } else {
    //             fieldParent.setContain(table);
    //         }
    //     }
    // }

    // public Map<String, Double> computeScores(SchemaVisualizerDto schemaVisualizerDto, Long rootNodeId) {
    //     var tables = parseSchemaVisualizerDto(rootNodeId, schemaVisualizerDto, SchemaVisualizerDto.Purpose.VALIDATE);
    //     return computeScores(tables);
    // }

    public Map<String, Double> computeScores(List<Table> tables) {
        var graph = new SchemaGraph(getAll());
        Map<String, Double> tableScore = new HashMap<>();
        for (Table table : tables) {
            tableScore.put(table.getName(), graph.getSchemataScore(StringUtil.constructStringEmptySeparator(table.getNameSpace(),".",table.getName())));
        }
        return tableScore;
    }

    // public List<Result> validateSchema(SchemaVisualizerDto schemaVisualizerDto, Long rootNodeId) {
    //     var tables = parseSchemaVisualizerDto(rootNodeId, schemaVisualizerDto, SchemaVisualizerDto.Purpose.VALIDATE);
    //     List<Result> results = new ArrayList<>();
    //     for (Table table : tables) {
    //         results.add(schemaValidator.apply(table));
    //         for (Field field: table.getFields()) {
    //             results.add(fieldValidator.apply(field));
    //         }
    //     }
    //     return results;
    // }

    public List<String> getFieldDataTypes(Long tableId) {
        var clientRepo = modelService.getByNodeId(tableId);
        return fieldDataTypes(clientRepo.getRepoType());
    }
    
    public List<String> fieldDataTypes(RepoType type) {
        return switch (type) {
            case AVRO -> List.of("string", "int", "float", "double", "long", "bytes", "boolean", "null");
            case PROTOBUF -> List.of("double", "float", "int32", "int64", "uint32", "uint64", "sint32", "sint64", "fixed32", "fixed64", "sfixed32", "sfixed64", "bool", "string", "bytes");
            case JSON -> List.of("string", "number" ,"integer" ,"boolean", "null");
            case YAML -> List.of();
        };
    }

    // this method is to save the modified table with the old table.
    public Table findDeltaForTable(Table tableNew, Long tableId, Long prId) {
        var table = getModel(tableId);
        if (Boolean.TRUE.equals(compareTable(tableNew, table))) {
            // ther is change with new Table.
            tableNew.setFields(null);
            tableNew.setIsUserChanged(Boolean.TRUE);
            tableNew.setClientId(null); // because when we fetch the table we should get the main table only.
            table.setIsUserChanged(Boolean.TRUE);
            table.setIsDeleted(Boolean.TRUE);
            table.setPrId(prId);
            table.setModifiedTable(tableNew);
            tableRepository.save(table);
        }
        return table;
    }

    // this method is to save newly added table in the file.
    // if the new schema is already present then return that or else create new with delta flag and pr_id.
    public Table saveNewTableWithDelta(Table tableNew, Long prId) {
        var table = findByNameAndNameSpace(tableNew.getName(), tableNew.getNameSpace());
        if (Boolean.TRUE.equals(ObjectUtils.isEmpty(table))) {
            tableNew.setIsUserChanged(Boolean.TRUE);
            tableNew.setPrId(prId);
            tableNew.setFields(null);
            tableNew.setClientId(SecurityUtil.getClientId());   // Add clientId for new table.
            return tableRepository.save(tableNew);
        }
        return table;
    }
    
    public List<Table> findDeltaForFields(Map<String, List<Field>> tableWithNewFields, Long prId, RepoType repoType) {
        List<Table> newTables = new ArrayList<>();
        tableWithNewFields.entrySet().forEach(entry -> {
            var fullName = entry.getKey();
            var nameSpace = fullName.substring(0, fullName.lastIndexOf("."));
            var name = fullName.substring(fullName.lastIndexOf(".")+1, fullName.length());
            // ASSUMING THAT THE CHAGED TABLE WILL NOT CONTAIN ANY PREVIOUS CHANGES. (i.e. IT IS NEWLY CHANGED TABLE).
            var table = findByNameAndNameSpace(name, nameSpace);
            List<Field> fields = new ArrayList<>();  // this list is to store modified fields.
            // get key value pair to identify the current field by new field
            var fieldMap = table.getFields().stream().collect(Collectors.toMap(Field::getName, Function.identity()));
            AtomicInteger rowNumber = new AtomicInteger(fieldMap.size()+1);
            entry.getValue().forEach(fieldNew -> {
                // find the field is exist already in schema
                var field = fieldMap.get(fieldNew.getName());
                if (ObjectUtils.isEmpty(field)) {
                    // if field is not present then the incoming field is new field
                    fieldNew.setIsUserChanged(Boolean.TRUE);
                    fieldNew.setPrId(prId);
                    fieldNew.setRowNumber(rowNumber.getAndIncrement());
                    checkNestedTable(fieldNew);  // if any nested table is present then join it from neo4j by searching.
                    fields.add(fieldNew);
                    return;
                }
                if (Boolean.TRUE.equals(compareField(fieldNew, field, repoType))) {
                    // if field is present but there is change with new field.
                    fieldNew.setIsUserChanged(Boolean.TRUE);
                    fieldNew.setPrId(prId);
                    fieldNew.setRowNumber(field.getRowNumber());
                    field.setPrId(prId);
                    field.setIsDeleted(Boolean.TRUE);
                    field.setIsUserChanged(Boolean.TRUE);
                    fieldNew.setReferenceField(field.getReferenceField());
                    checkNestedTable(fieldNew);
                    fields.add(fieldNew);
                } 
                fields.add(field);
                fieldMap.remove(field.getName());  // remove the processed field.
            });
    
            //if the field is present in fieldMap then that fields are deleted field OR field of other existing PR.
            // for (Map.Entry<String, Field> fld : fieldMap.entrySet()) {
            fieldMap.entrySet().forEach(fld -> {
                var field = fld.getValue();
                // this condition is to check that fields of other PR. 
                // Because the new incoming field will not contain fields of other PR.
                if (field.getIsDeleted().equals(Boolean.FALSE) && field.getIsUserChanged().equals(Boolean.FALSE)) {
                    field.setPrId(prId);
                    field.setIsDeleted(Boolean.TRUE);
                    field.setIsUserChanged(Boolean.TRUE);
                }
                fields.add(field);
            });
            table.setFields(fields);
            newTables.add(tableRepository.save(table));
        });
        return newTables;
    }

    private void checkNestedTable(Field field) {
        if (field.getIsPrimitiveType().equals(Boolean.FALSE)) {
            if (Boolean.FALSE.equals(ObjectUtils.isEmpty(field.getContain()))) {
                // field.getContain() gives table without ID. so we need to get the table from neo4j and join in it.
                var table = findByNameAndNameSpace(field.getContain().getName(), field.getContain().getNameSpace());
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
            var table = findByNameAndNameSpace(field.getContain().getName(), field.getContain().getNameSpace());
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
            var table = findByNameAndNameSpace(field.getContain().getName(), field.getContain().getNameSpace());
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

    public boolean compareField(Field fieldNew, Field field, RepoType repoType) {
        if (compareString(fieldNew.getDataType(), field.getDataType()) && repoType.equals(RepoType.PROTOBUF)) return true;
        if (compareString(fieldNew.getDescription(), field.getDescription())) return true;
        if (compareBoolean(fieldNew.getIsPii(), field.getIsPii())) return true;
        if (compareBoolean(fieldNew.getIsClassified(), field.getIsClassified())) return true;
        if (compareBoolean(fieldNew.getDeprecated(), field.getDeprecated())) return true;
        return false;
    }

    public boolean compareBoolean(Boolean newVal, Boolean oldVal) {
        if (ObjectUtils.isEmpty(oldVal)) {
            if (ObjectUtils.isEmpty(newVal)) {
                return false;
            }
            return true;
        }
        if (Boolean.FALSE.equals(oldVal.equals(newVal))) return true;
        return false;
    }

    public boolean compareString(String newDes, String oldDes) {
        if (ObjectUtils.isEmpty(oldDes)) {
            if (ObjectUtils.isEmpty(newDes)) {
                return false;
            }
            return true;
        }
        if (Boolean.FALSE.equals(oldDes.equals(newDes))) return true;
        return false;
    }

    public boolean compareTable(Table tableNew, Table tableOld) {
        if (compareString(tableNew.getDescription(), tableOld.getDescription())) return true;
        if (compareString(tableNew.getOwner(), tableOld.getOwner())) return true;
        if (compareString(tableNew.getDomain(), tableOld.getDomain())) return true;
        if (compareString(tableNew.getComplianceOwner(), tableOld.getComplianceOwner())) return true;
        if (compareString(tableNew.getChannel(), tableOld.getChannel())) return true;
        if (compareString(tableNew.getEmail(), tableOld.getEmail())) return true;
        if (compareString(tableNew.getStatus(), tableOld.getStatus())) return true;
        // else if (compareString(tableNew.getSchemaType().name(), tableOld.getSchemaType().name())) return true;
        if (!Arrays.deepEquals(tableNew.getSubscribers(), tableOld.getSubscribers())) return true;
        if (compareString(tableNew.getQualityRuleBase(), tableOld.getQualityRuleBase())) return true;
        if (compareString(tableNew.getQualityRuleSql(), tableOld.getQualityRuleSql())) return true;
        if (compareString(tableNew.getQualityRuleCel(), tableOld.getQualityRuleCel())) return true;
        return false;
    }

    public boolean revertChanges(Long prId, Long clientRepoId) {
        doTableChanges(prId, false, clientRepoId);
        doFieldChanges(prId, false);
        modelService.deleteModelByPrId(prId);
        schemaFileAuditService.deleteSchemaFileAuditByPrId(prId);
        return true;
    }

    private void doFieldChanges(Long prId, boolean acceptChanges) {
        var fields = fieldRepostory.findByPrId(prId);
        if (fields.isEmpty()) return;
        List<Field> fieldsToChange = new ArrayList<>();
        List<Field> fieldsToDelete = new ArrayList<>();
        fields.forEach(field -> {
            if (field.getIsDeleted().equals(Boolean.TRUE) && field.getIsUserChanged().equals(Boolean.TRUE)) {
                if (acceptChanges) {
                    fieldsToDelete.add(field);
                } else {
                    field.setIsDeleted(Boolean.FALSE);
                    field.setIsUserChanged(Boolean.FALSE);
                    field.setPrId(null);
                    fieldsToChange.add(field);
                }                
            }
            if (field.getIsDeleted().equals(Boolean.FALSE) && field.getIsUserChanged().equals(Boolean.TRUE)) {
                if (acceptChanges) {
                    field.setIsDeleted(Boolean.FALSE);
                    field.setIsUserChanged(Boolean.FALSE);
                    field.setPrId(null);
                    fieldsToChange.add(field);
                } else {
                    fieldsToDelete.add(field);
                }
            }
        });
        fieldRepostory.saveAll(fieldsToChange);
        fieldRepostory.deleteAll(fieldsToDelete);
    }

    public void transferTableProps(Table table, Table newTable) {
        table.setDescription(newTable.getDescription());
        table.setOwner(newTable.getOwner());
        table.setDomain(newTable.getDomain());
        table.setChannel(newTable.getChannel());
        table.setSchemaType(newTable.getSchemaType());
        table.setEmail(newTable.getEmail());
        table.setStatus(newTable.getStatus());
        table.setComplianceOwner(newTable.getComplianceOwner());
        table.setSubscribers(newTable.getSubscribers());
        table.setQualityRuleBase(newTable.getQualityRuleBase());
        table.setQualityRuleSql(newTable.getQualityRuleSql());
        table.setQualityRuleCel(newTable.getQualityRuleCel());
    }

    private void doTableChanges(Long prId, boolean acceptChanges, Long clientRepoId) {
        var tables = tableRepository.findByPrId(prId);
        if (tables.isEmpty()) return;
        List<Table> tablesToChange = new ArrayList<>();
        List<Table> tablesToDelete = new ArrayList<>();
        tables.forEach(table -> {
            if (table.getIsDeleted().equals(Boolean.TRUE) && table.getIsUserChanged().equals(Boolean.TRUE)) {
                if (acceptChanges) {
                    transferTableProps(table, table.getModifiedTable());  // save values from new table to old table
                }
                tablesToDelete.add(table.getModifiedTable()); // delete the delta node of table.
                table.setIsDeleted(Boolean.FALSE);
                table.setIsUserChanged(Boolean.FALSE);
                table.setModifiedTable(null);
                table.setPrId(null);
                tablesToChange.add(table);
            }
            if (table.getIsDeleted().equals(Boolean.FALSE) && table.getIsUserChanged().equals(Boolean.TRUE)) {
                if (acceptChanges) {
                    table.setIsUserChanged(Boolean.FALSE);
                    table.setPrId(null);
                    tableRepository.createTableDomainRelationShip(clientRepoId, List.of(table.getId()));
                    tablesToChange.add(table);
                } else {
                    tablesToDelete.add(table);
                }
            }
        });
        tableRepository.saveAll(tablesToChange);
        tableRepository.deleteAll(tablesToDelete);
    }
 
    public boolean acceptChanges(Long prId, Long clientRepoId) {
        doTableChanges(prId, true, clientRepoId);
        doFieldChanges(prId, true);       
        modelService.updateModelSetPrIdToNull(prId);
        schemaFileAuditService.updateSchemaFileAuditSetPrIdToNull(prId);
        return true;
    }

    public List<Field> addFields(List<Field> fields) {
        return fieldRepostory.saveAll(fields);
    }

    public Field addField(Field field) {
        return fieldRepostory.save(field);
    }

    public SchemaValidationDto schemaCompare(Map<String, Table> pathTableMap, ClientRepo clientRepo, Long prId) {
        return schemaValidator.schemaCompare(pathTableMap, clientRepo, prId);
    }

    @Transactional
    public Object uploadCsvToGit(List<MultipartFile> multipartFiles) throws IOException {
        var tableIds = getTablesFromCsvFile(multipartFiles).stream().map(Table::getId).toList();
        if (Boolean.FALSE.equals(tableIds.isEmpty())) {
            log.info("Generating File Contents");
            var fileContentMap = schemaFileAuditService.generateFileContentOfSchema(tableIds);
            gitHubService.commitAndPushInMainBranch(fileContentMap, "Initial commit");   
        }
        return Status.SUCCESS;
    }

    public List<Table> getTablesFromCsvFile(List<MultipartFile> multipartFiles) throws IOException {
        Map<String, Table> tables = new HashMap<>();
        var clientRepo = clientRepoService.getSchemataRepo().get();
        multipartFiles.stream().forEach(file -> {
            validateCsvFile(file);
            log.info("Reading csv file {}", file.getOriginalFilename());
            var arrayNode = FileUtil.readCsvFile(file);
            tables.putAll(getTablesFromArryaNode(arrayNode));
        });
        var fullnames = new ArrayList<>(tables.keySet());
        var models = modelService.findByFullNames(fullnames).stream().map(model -> StringUtil.constructStringEmptySeparator(model.getNameSpace(),".",model.getName())).toList();
        List<Table> newTables = new ArrayList<>();
        log.info("Filtering Already exists tables");
        for (Table table : tables.values()) {
            var fullName = StringUtil.constructStringEmptySeparator(table.getNameSpace(),".",table.getName());
            if (Boolean.FALSE.equals(models.contains(fullName))) {
                newTables.add(table);
            }
        }
        log.info("Saving new tables from csv files");
        newTables = tableRepository.saveAll(newTables);
        newTables.forEach(table -> {
            var schemaFileAudit = schemaFileAuditService.createSchemaFileAuditForNewFile(clientRepo, table, Constants.CSV_UPLOAD_ROOT_FILE_PATH);
            modelService.createModel(schemaFileAudit, clientRepo.getId(), table);
        });
        log.info("Creating Domain Tables RelationShip");
        tableRepository.createTableDomainRelationShip(clientRepo.getId(), newTables.stream().map(Table::getId).toList());
        return newTables;
    }

    private void validateCsvFile(MultipartFile multipartFile) {
        log.info("Validating file {}", multipartFile.getOriginalFilename());
        if (Boolean.FALSE.equals(FileUtil.isCSVFormat(multipartFile))) 
            throw new InvalidDataException(ErrorCode.INVALID_FILE, responseMessage.getErrorMessage(ErrorCode.INVALID_FILE, multipartFile.getName()));
        var headers = FileUtil.getCsvFileHeaders(multipartFile);
        for (String column : TableCsvDto.getCsvHeaders()) {
            if (Boolean.FALSE.equals(headers.contains(column)))
                throw new InvalidDataException(ErrorCode.UNRECOGNIZED_COLUMN_IN_CSV_FILE_NAME, responseMessage.getErrorMessage(ErrorCode.UNRECOGNIZED_COLUMN_IN_CSV_FILE_NAME, column, multipartFile.getOriginalFilename()));
        }
    }

    public Map<String, Table> getTablesFromArryaNode(ArrayNode arrayNode) {
        var iterator = arrayNode.iterator();
        Map<String, Table> tables = new HashMap<>();
        while (iterator.hasNext()) {
            var csvDto = objectMapper.convertValue(iterator.next(), TableCsvDto.class);
            var fullName = StringUtil.constructStringEmptySeparator(csvDto.getTableNamespace(), ".", csvDto.getTableName());
            var table = tables.get(fullName);
            if (Objects.isNull(table)){
                table = Table.builder().name(csvDto.getTableName()).nameSpace(csvDto.getTableNamespace()).type("object")
                             .clientId(SecurityUtil.getClientId()).description(csvDto.getTableDescription()).build();
            }
            var fields = new ArrayList<>(table.getFields());
            var field = Field.builder().name(csvDto.getColumnName()).schema(fullName).dataType(csvDto.getDataType())
                             .rowNumber(fields.size() + 1).isPrimitiveType(Field.isPrimitiveType(csvDto.getDataType())).isPii(csvDto.getIsPii())
                             .description(csvDto.getColumnDescription()).isClassified(csvDto.getIsClassified()).build();
            fields.add(field);
            table.setFields(fields);
            tables.put(fullName, table);
        }
        return tables;
    }
}
