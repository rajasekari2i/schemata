package com.opsbeach.connect.schemata.processor.avro;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.service.DomainService;
import com.opsbeach.connect.github.service.ModelService;
import com.opsbeach.connect.github.service.SchemaFileAuditService;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.processor.SchemaFileProcessor;
import com.opsbeach.connect.schemata.service.DomainNodeService;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.FileNotFoundException;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.utils.FileUtil;
import com.opsbeach.sharedlib.utils.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvroSchema implements SchemaFileProcessor {

    private final TableService tableService;
    private final DomainService domainService;
    private final DomainNodeService domainNodeService;
    private final SchemaFileAuditService schemaFileAuditService;
    private final ModelService modelService;
    private final ResponseMessage responseMessage;

    @Value("${server.home-path}")
    private String homePath;

    static final Map<String, Schema.Type> PRIMITIVES = new HashMap<>();

    static {
        PRIMITIVES.put("string", Schema.Type.STRING);
        PRIMITIVES.put("bytes", Schema.Type.BYTES);
        PRIMITIVES.put("int", Schema.Type.INT);
        PRIMITIVES.put("long", Schema.Type.LONG);
        PRIMITIVES.put("float", Schema.Type.FLOAT);
        PRIMITIVES.put("double", Schema.Type.DOUBLE);
        PRIMITIVES.put("boolean", Schema.Type.BOOLEAN);
        PRIMITIVES.put("null", Schema.Type.NULL);
    }

    @Override
    public List<Table> getTables(byte[] content, Boolean toSave) throws IOException {
      var avroSchema = new Schema.Parser().parse(new ByteArrayInputStream(content));
      Map<String, Table> tableMap = new HashMap<>();
      var table = parseSchema(avroSchema, tableMap, toSave);
      List<Table> tables = new LinkedList<>(tableMap.values());
      tables.add(table);
      return tables;
    }

    @Override
    public List<Table> getTables(String path, Boolean toSave) throws IOException {
      if (Boolean.FALSE.equals(path.endsWith("avsc"))) {
        throw new InvalidDataException(ErrorCode.INVALID_FILE, responseMessage.getErrorMessage(ErrorCode.INVALID_FILE, path));
      }
      var avroSchema = new Schema.Parser().parse(new File(path));
      Map<String, Table> tableMap = new HashMap<>();  // this map is to track schemas present in current file.
      var table = parseSchema(avroSchema, tableMap, toSave);
      if (toSave) {
        table = tableService.addTable(table);
      }
      List<Table> tables = new LinkedList<>(tableMap.values());
      tables.add(table);
      return tables;
    }

    private Table parseSchema(Schema schema, Map<String, Table> tableMap, Boolean bool) {
      var avroSchemaParser = new AvroSchemaParser(tableService, PRIMITIVES);
      return avroSchemaParser.parseSchema(schema, tableMap, bool);
    }

    @Override
    public String getFileContent(Table table) {
      var avroSchemaGenerator = new AvroSchemaGenerator(PRIMITIVES);
      return avroSchemaGenerator.generateTableSchema(table).toString(true);
    }

    @Override
    public void parseFolder(String folderPath, ClientRepo clientRepo) {
      var filePaths = FileUtil.deepSearchFiles(folderPath, ".avsc");
      List<Model> models = new ArrayList<>();
      log.info("creating domain in neo4j");
      var domainNode = domainNodeService.addDomainNode(clientRepo.getFullName(), clientRepo.getClientId(), clientRepo.getId());
      var domain = domainService.addDomain(clientRepo, domainNode.getId());
      domainNode.setTables(new ArrayList<>());
      filePaths.forEach(filePath -> {
          log.info("Working on fetching Models from file : "+filePath);
          var tables = addModelsInGraphDB(filePath);
          if (tables != null) {
              var schemaFileAudit = schemaFileAuditService.createSchemaFileAuditWhileInitialLoading(filePath, clientRepo, tables.get(tables.size()-1).getId());
              models.addAll(modelService.createModels(tables, schemaFileAudit, domain));
              domainNode.getTables().addAll(tables);
          }
      });
      log.info("Fetching Models from Files is completed");
      domainNodeService.update(domainNode);
      modelService.addAll(models);
    }

    private List<Table> addModelsInGraphDB(String filePath) {
        var fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
        var fileType = filePath.substring(filePath.lastIndexOf(".") + 1);
        // create tables on neo4j 
        log.info(StringUtil.constructStringEmptySeparator("Started parsing file ",fileName,".",fileType));
        try {
          return getTables(filePath, Boolean.TRUE);
        } catch (Exception e) {
          throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.FILE_NOT_FOUND, e.getMessage()));
        }
    }
}