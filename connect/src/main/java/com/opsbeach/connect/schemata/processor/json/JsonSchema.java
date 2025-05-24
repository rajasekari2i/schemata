package com.opsbeach.connect.schemata.processor.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.service.DomainService;
import com.opsbeach.connect.github.service.ModelService;
import com.opsbeach.connect.github.service.SchemaFileAuditService;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.processor.SchemaFileProcessor;
import com.opsbeach.connect.schemata.service.DomainNodeService;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.FileNotFoundException;
import com.opsbeach.sharedlib.exception.SchemaParserException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.utils.FileUtil;
import com.opsbeach.sharedlib.utils.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonSchema implements SchemaFileProcessor {
    
    static final List<String> PRIMITIVES = new ArrayList<>();

    static {
        PRIMITIVES.add("string");
        PRIMITIVES.add("number");
        PRIMITIVES.add("integer");
        PRIMITIVES.add("boolean");
        PRIMITIVES.add("null");
    }

    private final TableService tableService;
    private final DomainNodeService domainNodeService;
    private final DomainService domainService;
    private final ModelService modelService;
    private final ResponseMessage responseMessage;
    private final SchemaFileAuditService schemaFileAuditService;

    @Value("${server.home-path}")
    private String homePath;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Table> getTables(byte[] content, Boolean initialPull) throws IOException {
        var jsonSchema = mapper.readTree(content);
        return parseSchema(jsonSchema, new HashMap<>(), initialPull);
    }

    @Override
    public String getFileContent(Table table) {
        return new JsonSchemaGenerator(PRIMITIVES).generateTableSchema(table).toPrettyString();
    }

    @Override
    public List<Table> getTables(String path, Boolean initialPull) throws IOException {
        var inputStream = new FileInputStream(new File(path));
        var jsonSchema = mapper.readTree(inputStream);   // get schema structure as JsonNode from inputStream of file.
        return parseSchema(jsonSchema, new HashMap<>(), initialPull);
    }

    private List<Table> parseSchema(JsonNode jsonSchema, Map<Field, String> fieldTableMap, Boolean initialPull) {
        Map<String, Table> tableMap = new HashMap<>(); // this map is to store nested tables.
        var jsonSchemaParser = new JsonSchemaParser(tableService, PRIMITIVES, mapper);
        var table = jsonSchemaParser.parseTable(jsonSchema, tableMap, fieldTableMap, initialPull);
        // add to Neo4j DB
        if (initialPull) {
            table = tableService.addTable(table);
        }
        List<Table> tables = new LinkedList<>(tableMap.values());
        tables.add(table);
        return tables;
    }

    @Override
    public void parseFolder(String folderPath, ClientRepo clientRepo) {
        var filePaths = FileUtil.deepSearchFiles(folderPath, ".json");
        List<Model> models = new ArrayList<>();
        log.info("creating domain in neo4j");
        var domainNode = domainNodeService.addDomainNode(clientRepo.getFullName(), clientRepo.getClientId(), clientRepo.getId());
        var domain = domainService.addDomain(clientRepo, domainNode.getId());
        domainNode.setTables(new ArrayList<>());
        Map<Field, String> fieldTableMap = new HashMap<>();
        filePaths.forEach(filePath -> {
            log.info("Working on fetching Models from file : "+filePath);
            var tables = readSchemaAndAddInGraphDB(filePath, fieldTableMap);
            if (tables != null) {
                var schemaFileAudit = schemaFileAuditService.createSchemaFileAuditWhileInitialLoading(filePath, clientRepo, tables.get(tables.size()-1).getId());
                models.addAll(modelService.createModels(tables, schemaFileAudit, domain));
                domainNode.getTables().addAll(tables);
            }
        });
        mergeFieldAndTable(fieldTableMap);
        log.info("Fetching Models from Files is completed");
        domainNodeService.update(domainNode);
        modelService.addAll(models);
    }

    private void mergeFieldAndTable(Map<Field, String> fieldTableMap) {
        for (Map.Entry<Field, String> entry : fieldTableMap.entrySet()) {
            var fullname = entry.getValue();
            var nameSpace = fullname.substring(0, fullname.lastIndexOf("."));
            var name = fullname.substring(fullname.lastIndexOf(".")+1);
            var table = tableService.findByNameAndNameSpace(name, nameSpace);
            var field = entry.getKey();
            field.setContain(table);
            tableService.addField(field);
        }
    }

    public List<Table> readSchemaAndAddInGraphDB(String filePath, Map<Field, String> fieldTableMap) {
        var fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
        var fileType = filePath.substring(filePath.lastIndexOf(".") + 1);
        // create tables on neo4j 
        log.info(StringUtil.constructStringEmptySeparator("Started parsing file ",fileName,".",fileType));
        try {
            var inputStream = new FileInputStream(new File(filePath));
            var jsonSchema = mapper.readTree(inputStream);   // get schema structure as JsonNode from inputStream of file.
            return parseSchema(jsonSchema, fieldTableMap, Boolean.TRUE);
        } catch (IOException e) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.FILE_NOT_FOUND, e.getMessage()));
        } catch (SchemaParserException e) {
            throw new SchemaParserException("filePath", e.fillInStackTrace());
        }
    }
}
