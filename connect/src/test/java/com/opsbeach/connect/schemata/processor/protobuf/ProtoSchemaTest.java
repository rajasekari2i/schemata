package com.opsbeach.connect.schemata.processor.protobuf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Domain;
import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.service.ClientRepoService;
import com.opsbeach.connect.github.service.DomainService;
import com.opsbeach.connect.github.service.ModelService;
import com.opsbeach.connect.github.service.SchemaFileAuditService;
import com.opsbeach.connect.schemata.entity.DomainNode;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.SchemaType;
import com.opsbeach.connect.schemata.service.DomainNodeService;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.dto.ClientDto;
import com.opsbeach.sharedlib.exception.SchemaParserException;
import com.opsbeach.sharedlib.security.ApplicationConfig;
import com.opsbeach.sharedlib.service.GoogleCloudService;
import com.opsbeach.sharedlib.utils.StringUtil;

public class ProtoSchemaTest {
    
    @InjectMocks
    private ProtoSchema protoSchema;
    @Mock
    private DomainNodeService domainNodeService;
    @Mock
    private DomainService domainService;
    @Mock
    private TableService tableService;
    @Mock
    private SchemaFileAuditService schemaFileAuditService;
    @Mock
    private ModelService modelService;
    @Mock
    private ClientRepoService clientRepoService;
    @Mock
    private ApplicationConfig applicationConfig;
    @Mock
    private GoogleCloudService googleCloudService;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private Map<String, String> gcloud;
    private Object homePath;

    @BeforeEach
    public void init() throws StreamReadException, DatabindException, IOException {
        InputStream inputStream = new FileInputStream(new File("src/test/resources/application-test.yaml"));
        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> data = yaml.load(inputStream);
        gcloud = data.get("gcloud");
        homePath = data.get("home-path");
    }

    @Test
    public void parseFolderTest() {
        var folderPath = "src/test/resources/schema_1/protobuf";
        var clientRepo = ClientRepo.builder().id(2L).clientId(1L).defaultBranch("branch").fullName("fullName").build();
            when(domainNodeService.addDomainNode(anyString(), anyLong(), anyLong())).thenReturn(DomainNode.builder().id(2L).build());
            when(domainService.addModel(any(Domain.class))).thenReturn(Domain.builder().id(1L).build());
            when(tableService.addTables(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        protoSchema.parseFolder(folderPath, clientRepo);
        assertThrows(SchemaParserException.class, () -> protoSchema.parseFolder("src/test/resources/schema_1/protobuf/repository", clientRepo));
    }

    private Table getTableUser() {        
        var id = Field.builder().id(2L).name("id").dataType("INT32").description("Primary Key").isPii(Boolean.FALSE).isClassified(Boolean.FALSE).deprecated(Boolean.FALSE).build();
        var timeZone = Field.builder().id(1L).name("timezone").dataType("STRING").description("preferred time zone for the user").isPii(Boolean.FALSE).isClassified(Boolean.FALSE).deprecated(Boolean.FALSE).build();
        String[] symbols = {"EMAIL", "SOCIAL_MEDIA"};
        var origin = Field.builder().id(3L).name("origin").dataType("ENUM").description("origin source of the campaign")
                            .enumFilePath("main/schema/cam.proto").enumName("CampaignOrigin").enumPackage("com.schemata")
                            .isPii(Boolean.FALSE).isClassified(Boolean.FALSE).deprecated(Boolean.FALSE).symbols(symbols).build();
        var is_active = Field.builder().id(4L).name("is_active").dataType("BOOL").description("email of user").isPii(Boolean.FALSE).isClassified(Boolean.FALSE).deprecated(Boolean.FALSE).build();
        symbols[0] = "VIEW,READ_REVIEW"; symbols[1] = "VIEW_DESCRIPTION";
        var activityType = Field.builder().id(5L).name("activity_type").dataType("ENUM").description("Type of the user activity")
                            .enumFilePath("main/schema/schemata/protobuf/schemata.proto").enumName("ActivityType").enumPackage("org.schemata.schema")
                            .isPii(Boolean.FALSE).isClassified(Boolean.FALSE).deprecated(Boolean.FALSE).symbols(symbols).build();
        var fields = List.of(id, timeZone, origin, is_active, activityType);
        String[] subscribers = {"Customer Support Team", "sales team"};
        return Table.builder().id(1L).name("User").nameSpace("com.schemata").schemaType(SchemaType.ENTITY).fields(fields).complianceOwner("compliance owner")
                    .owner("owner").domain("domain").channel("channel").email("email").status("active")
                    .qualityRuleBase("base rule").subscribers(subscribers).build();
    }

    private Table getTableUserActivityAggregate() {
        var user = Field.builder().id(1L).name("user").dataType("com.schemata.User").description("User entity reference").isPii(Boolean.FALSE).isClassified(Boolean.FALSE).deprecated(Boolean.FALSE).build();
        String[] symbols = {"VIEW,READ_REVIEW","VIEW_DESCRIPTION"};
        var activityType = Field.builder().id(2L).name("activity_type").dataType("ENUM").description("Type of the user activity")
                            .enumFilePath("main/schema/user.proto").enumName("UserActivityType").enumPackage("com.schemata")
                            .isPii(Boolean.FALSE).isClassified(Boolean.FALSE).deprecated(Boolean.FALSE).symbols(symbols).build();
        var product = Field.builder().id(3L).name("product").dataType("com.schemata.Product").description("Product entity reference").isPii(Boolean.FALSE).isClassified(Boolean.FALSE).deprecated(Boolean.FALSE).build();
        var timestamp = Field.builder().id(4L).name("timestamp").dataType("google.protobuf.Timestamp").description("timestamp").build();
        symbols[0] = "READ_REVIEW"; symbols[1] = "VIEW_DESCRIPTION";
        var productType = Field.builder().id(2L).name("product_type").dataType("ENUM").description("Type of the Product")
                            .enumFilePath("main/schema/product/product.proto").enumName("ProductType").enumPackage("org.schemata")
                            .isPii(Boolean.FALSE).isClassified(Boolean.FALSE).deprecated(Boolean.FALSE).symbols(symbols).build();
        var fields = List.of(user, product, activityType, timestamp, productType);
        return Table.builder().id(2L).name("UserActivityAggregate").nameSpace("com.schemata").schemaType(null)
                    .fields(fields).owner("owner").domain("domain").status(null).build();
    }

    @Test
    public void generateSchemaTest() {
        var userTable = getTableUser();
        var userActivityAggregateTable = getTableUserActivityAggregate();
        var userFilePath = "https://github.com/opsbeach/schemata_protobuf/tree/main/src/main/schema/user.proto";
        var userSchemaFileAudit = SchemaFileAudit.builder().id(1L).path(userFilePath).build();
            when(schemaFileAuditService.getByModelNodeId(userTable.getId())).thenReturn(List.of(userSchemaFileAudit));
        var userModel = Model.builder().id(1L).nodeId(userTable.getId()).build();
        var userActivityAggregateModel = Model.builder().id(2L).nodeId(userActivityAggregateTable.getId()).build();
            when(modelService.findBySchemaFileAudit(userSchemaFileAudit.getId())).thenReturn(List.of(userModel, userActivityAggregateModel));
            when(tableService.findAllById(anyList())).thenReturn(List.of(userTable, userActivityAggregateTable));
            when(schemaFileAuditService.filterFields(any(Table.class))).thenAnswer(invocation -> invocation.getArgument(0));

            when(tableService.findByNameAndNameSpace("User", "com.schemata")).thenReturn(userTable);
            when(schemaFileAuditService.getByModelNodeId(userTable.getId())).thenReturn(List.of(userSchemaFileAudit));

        var productFilePath = "https://github.com/opsbeach/schemata_protobuf/tree/main/src/main/schema/Product/product.proto";
        var productSchemaFileAudit = SchemaFileAudit.builder().id(1L).path(productFilePath).build();
            when(tableService.findByNameAndNameSpace("Product", "com.schemata")).thenReturn(Table.builder().id(108L).build());
            when(schemaFileAuditService.getByModelNodeId(108L)).thenReturn(List.of(productSchemaFileAudit));
        var response = protoSchema.generateSchema(userTable.getId());
        assertEquals(1, response.values().size());
    }

    @Test
    public void getTablesOfFilePathsTest() throws IOException {
        when(clientRepoService.getClient()).thenReturn(ClientDto.builder().name("opsbeach").build());
        var clientRepo = ClientRepo.builder().id(2L).clientId(1L).defaultBranch("branch").fullName("fullName").build();
        String[] filePaths = {"src/main/schema/Product/product.proto", "/src/org/schemata/protobuf/schemata.proto"};
            createZipFile(clientRepo);
            when(applicationConfig.getGcloud()).thenReturn(gcloud);
            ReflectionTestUtils.setField(protoSchema, "homePath", homePath);
        var response = protoSchema.getTablesOfFilePaths(filePaths, clientRepo, "develop");
        assertEquals(2, response.get(filePaths[0]).size());
    }

    private void createZipFile(ClientRepo clientRepo) throws IOException {
        var repoFolderPath = StringUtil.constructStringEmptySeparator(homePath.toString(), clientRepo.getFullName(), "-delta");
        new File(repoFolderPath).mkdir();
        Files.copy(Paths.get("src/test/resources/sampleRepo/proto-repo.tar.gz"), Paths.get(repoFolderPath.concat("/repo.tar.gz")), StandardCopyOption.REPLACE_EXISTING);
    }
}
