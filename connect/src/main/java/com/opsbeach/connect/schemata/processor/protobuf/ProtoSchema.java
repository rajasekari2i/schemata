package com.opsbeach.connect.schemata.processor.protobuf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.service.ClientRepoService;
import com.opsbeach.connect.github.service.DomainService;
import com.opsbeach.connect.github.service.ModelService;
import com.opsbeach.connect.github.service.SchemaFileAuditService;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.service.DomainNodeService;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.FileNotFoundException;
import com.opsbeach.sharedlib.exception.SchemaParserException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.ApplicationConfig;
import com.opsbeach.sharedlib.service.GoogleCloudService;
import com.opsbeach.sharedlib.utils.FileUtil;
import com.opsbeach.sharedlib.utils.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProtoSchema {

    private final DomainNodeService domainNodeService;
    private final DomainService domainService;
    private final TableService tableService;
    private final ModelService modelService;
    private final SchemaFileAuditService schemaFileAuditService;
    private final ResponseMessage responseMessage;
    private final GoogleCloudService googleCloudService;
    private final ClientRepoService clientRepoService;
    private final ApplicationConfig applicationConfig;

    @Value("${server.home-path}")
    private String homePath;

    // public List<Table> getTables(String path, Boolean toSave) throws IOException, DescriptorValidationException {
    //     var loader = new ProtoFileDescriptorSetLoader(new FileInputStream(new File(path)));
    //     var descriptors = loader.loadDescriptors();
    //     return new ProtoProcessor().parse(descriptors, new HashMap<>());
    // }

    public Map<SchemaFileAudit, String> generateSchema(Long tableId) {
        var fileGenerator = new ProtobufFileGenerator(schemaFileAuditService, modelService, tableService);
        return fileGenerator.getSchema(tableId);
    }

    public void parseFolder(String folderPath, ClientRepo clientRepo) {
        var filePaths = FileUtil.deepSearchFiles(folderPath, ".proto");
        log.info("creating domain in neo4j");
        var domainNode = domainNodeService.addDomainNode(clientRepo.getFullName(), clientRepo.getClientId(), clientRepo.getId());
        var domain = domainService.addDomain(clientRepo, domainNode.getId());
        domainNode.setTables(new ArrayList<>());
        Map<Field, String> fieldTableMap = new HashMap<>();
        filePaths.forEach(filePath -> {
            if (filePath.contains("/src/org/schemata/protobuf/")) return;
            log.info("Working on fetching Models from file : "+filePath);
            var tables = getTableFromProtoFileByPath(filePath, folderPath, fieldTableMap);
            if (tables != null) {
                tables = tableService.addTables(tables);
                var schemaFileAudit = schemaFileAuditService.createSchemaFileAuditWhileInitialLoading(filePath, clientRepo, tables.get(tables.size()-1).getId());
                modelService.createModels(tables, schemaFileAudit, domain);
                domainNode.getTables().addAll(tables);
            }            
        });
        mergeFieldAndTable(fieldTableMap);
        log.info("Fetching Models from Files is completed");
        domainNodeService.update(domainNode);
    }

    private List<Table> getTableFromProtoFileByPath(String filePath, String repoFolderPath, Map<Field, String> fieldTableMap) {
        try {
            var schemataProtoFilesPath = StringUtil.constructStringEmptySeparator(repoFolderPath, "/", new File(repoFolderPath).list()[0], "/src/org");
            var rootFolderPath = StringUtil.constructStringEmptySeparator(repoFolderPath, "/", new File(repoFolderPath).list()[0], "/src/main/schema");
            var descriptorFilePath = generateDescriptorFileForProtoFile(filePath, repoFolderPath, schemataProtoFilesPath, rootFolderPath);
            var loader = new ProtoFileDescriptorSetLoader(new FileInputStream(new File(descriptorFilePath)));
            var fileDescriptors = loader.loadFileDescriptors();
            var fileName = filePath.replace(rootFolderPath+"/", "");
            // FileUtil.deleteFile(descriptorFilePath);
            var descriptors = fileDescriptors.get(fileName).getMessageTypes().stream().toList();
            descriptors = loader.collectAllNestedMessageDescriptors(descriptors);
            return new ProtoProcessor().parse(descriptors, fieldTableMap);
        } catch (IOException e) {
            throw new FileNotFoundException(ErrorCode.FILE_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.FILE_NOT_FOUND, e.getMessage()));
        } catch (Exception e) {
            log.info("Error occured while parsing file: "+filePath);
            throw new SchemaParserException(e.getMessage());
        }
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

    // private String generateDescriptorFileForFolder(String folderPath) {
    //     var filePaths = FileUtil.deepSearchFiles(folderPath, ".proto");
    //     Set<String> protoPaths = new HashSet<>();
    //     filePaths.forEach(path -> {
    //         protoPaths.add(path.substring(0, path.lastIndexOf("/")));
    //     });
    //     StringBuilder protoComand = new StringBuilder("protoc");
    //     protoPaths.forEach(pp -> protoComand.append(" -I=".concat(pp)));
    //     protoComand.append(" --descriptor_set_out=model.desc --include_imports --include_source_info ");
    //     // protoComand.append(folderPath.concat("/**/*.proto "));
    //     protoPaths.forEach(fp -> protoComand.append(fp+"/*.proto "));
    //     protoComand.append("--experimental_allow_proto3_optional");
    //     System.out.println(protoComand.toString());
    //     executeProtocCmd(protoComand.toString(), folderPath);
    //     return folderPath+"/model.desc";
    // }

    private String generateDescriptorFileForProtoFile(String filePath, String repoFolderPath,
                            String schemataProtoFilesPath, String rootFolderPath) throws IOException, InterruptedException {
        var folderPath = filePath.substring(0, filePath.lastIndexOf("/"));
        var descriptorFileName = filePath.substring(filePath.lastIndexOf("/")+1, filePath.lastIndexOf("."));
        StringBuilder protoComand = new StringBuilder("protoc -I=");
        protoComand.append(schemataProtoFilesPath);
        protoComand.append(" -I=".concat(rootFolderPath));
        protoComand.append(" --descriptor_set_out=").append(descriptorFileName).append(".desc --include_imports --include_source_info ");
        protoComand.append(filePath).append(" --experimental_allow_proto3_optional");
        System.out.println(protoComand.toString());
        executeProtocCmd(protoComand.toString(), folderPath);
        return StringUtil.constructStringEmptySeparator(folderPath, "/", descriptorFileName, ".desc");
    }

    private void executeProtocCmd(String cmd, String cmdExecutionPath) throws IOException, InterruptedException {
        System.out.println(System.getProperty("os.name"));
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", cmd);
        builder.directory(new File(cmdExecutionPath));
        Process process = builder.start();

        // Read the output of the command if needed
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        // Wait for the process to finish
        int exitCode = process.waitFor();
        System.out.println("Command completed with exit code "+exitCode);
    }

    public Map<String, List<Table>> getTablesOfFilePaths(String[] filePaths, ClientRepo clientRepo, String branchName) {
        var clientDto = clientRepoService.getClient();
        String objectName = StringUtil.constructStringEmptySeparator(clientDto.getName(), "/", clientRepo.getFullName(), "/", branchName);
        var repoFolderPath = StringUtil.constructStringEmptySeparator(homePath, clientRepo.getFullName(), "-delta");
        new File(repoFolderPath).mkdirs();
        pullFilesFromBucket(objectName, repoFolderPath);
        Map<String, String> filteredFilePaths = filterFilePaths(FileUtil.deepSearchFiles(repoFolderPath, ".proto"), filePaths);
        Map<String, List<Table>> fileTables = new HashMap<>();
        for (Map.Entry<String, String> entry : filteredFilePaths.entrySet()) {
            var filePath = entry.getValue();
            if (filePath.contains("/src/org/schemata/protobuf/")) continue;
            log.info("Working on fetching Models from file : "+filePath);
            var tables = getTableFromProtoFileByPath(filePath, repoFolderPath, new HashMap<>());
            fileTables.put(entry.getKey(), tables);
        }
        FileUtil.deleteDirectory(repoFolderPath);
        return fileTables;    
    }

    private Map<String, String> filterFilePaths(List<String> allFilePaths, String[] changesFilePaths) {
        Map<String, String> filePaths = new HashMap<>();
        AtomicInteger flag = new AtomicInteger(0);
        for (String filePath : allFilePaths) {
            if (filePath.endsWith(changesFilePaths[flag.get()])) {
                /*
                 * Here Key value contains absolute path of file where as value contains exactPath of folder from root folder.
                 */
                filePaths.put(changesFilePaths[flag.getAndIncrement()], filePath);
            }
            if (flag.get() > (changesFilePaths.length - 1)) break;
        }
        return filePaths;
    }

    private boolean pullFilesFromBucket(String objectName, String repoFolderPath) {
        String destFilePath = repoFolderPath + "/repo.tar.gz";
        log.info("pulling file from bucket");
        googleCloudService.downloadFile(applicationConfig.getGcloud().get("repo-bucket"), objectName, destFilePath);
        log.info("UnZip the downloaded tar.gz file");
        FileUtil.uncompressTarGZ(repoFolderPath, destFilePath);
        FileUtil.deleteFile(destFilePath);
        return true;
    }
}
