package com.opsbeach.connect.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Domain;
import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.entity.PullRequest;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.entity.ClientRepo.RepoSource;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.repository.SchemaFileAuditRepository;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.SchemaType;
import com.opsbeach.connect.schemata.processor.avro.AvroSchema;
import com.opsbeach.connect.schemata.processor.json.JsonSchema;
import com.opsbeach.connect.schemata.processor.protobuf.ProtoSchema;
import com.opsbeach.connect.schemata.service.DomainNodeService;
import com.opsbeach.connect.schemata.service.OrganizationService;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.exception.SchemaParserException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.service.GoogleCloudService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class SchemaFileAuditServiceTest {
    
    @InjectMocks
    private SchemaFileAuditService schemaFileAuditService;
    @Mock
    private SchemaFileAuditRepository schemaFileAuditRepository;
    @Mock
    private EventAuditService eventAuditService;
    @Mock
    private DomainNodeService domainNodeService;
    @Mock
    private DomainService domainService;
    @Mock
    private ModelService modelService;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private GoogleCloudService googleCloudService;
    @Spy
    private IdSpecifications<SchemaFileAudit> scmFileAuditpecifications;
    @Mock
    private TableService tableService;
    @Mock
    private JsonSchema jsonSchema;
    @Mock
    private AvroSchema avroSchema;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaUpdate<SchemaFileAudit> criteriaUpdate;
    @Mock
    private CriteriaDelete<SchemaFileAudit> criteriaDelete;
    @Mock
    private Root<SchemaFileAudit> root;
    @Mock
    private TypedQuery<SchemaFileAudit> typedQuery;
    @Mock
    private EntityManager entityManager;
    @Mock
    private ResponseMessage responseMessage;
    @Mock
    private ProtoSchema protoSchema;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private Object homePath;
    private String githubFilePath;

    @BeforeEach
    public void init() throws StreamReadException, DatabindException, IOException {
        InputStream inputStream = new FileInputStream(new File("src/test/resources/application-test.yaml"));
        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> data = yaml.load(inputStream);
        homePath = data.get("home-path");
        githubFilePath = data.get("github").get("construct-file-path");
    }

    private SchemaFileAudit getSchemaFileAudit() {
        return SchemaFileAudit.builder().id(1L).name("avro_testing").fileType("avsc").path("path").build();
    }

    @Test
    public void createSchemaFileAuditWhileInitialLoadingTest() {
        var schemaFileAudit = getSchemaFileAudit();
        var filePath = "src/test/resources/schema_1/avro/avro_testing.avsc";
            when(schemaFileAuditRepository.save(any(SchemaFileAudit.class))).thenReturn(schemaFileAudit);
            ReflectionTestUtils.setField(schemaFileAuditService, "homePath", homePath);
        var clientRepo = ClientRepo.builder().id(1L)
                                   .fullName("fullName")
                                   .defaultBranch("main")
                                   .repositorySource(RepoSource.GITHUB)
                                   .build();
        var response = schemaFileAuditService.createSchemaFileAuditWhileInitialLoading(filePath, clientRepo, 1L);
        assertEquals(response.getName(), schemaFileAudit.getName());
    }

    @Test
    public void createSchemaFileAuditForNewFileTest() {
        var schemaFileAudit = getSchemaFileAudit();
        var clientRepo = ClientRepo.builder().id(1L).repoType(RepoType.AVRO).fullName("fullName").defaultBranch("main").build();
        var table = Table.builder().id(1L).name("product").nameSpace("com.acme").build();
        var filePath = "src/test/resources/schema_1/avro";
            when(schemaFileAuditRepository.save(any(SchemaFileAudit.class))).thenReturn(schemaFileAudit);
            ReflectionTestUtils.setField(schemaFileAuditService, "githubFilePath", githubFilePath);
        var response = schemaFileAuditService.createSchemaFileAuditForNewFile(clientRepo, table, filePath);
        assertEquals(response.getName(), schemaFileAudit.getName());

        clientRepo = ClientRepo.builder().id(1L).repoType(RepoType.PROTOBUF).fullName("fullName").defaultBranch("main").build();
        response = schemaFileAuditService.createSchemaFileAuditForNewFile(clientRepo, table, filePath.concat("/"));
        assertEquals(response.getName(), schemaFileAudit.getName());
        
        clientRepo = ClientRepo.builder().id(1L).repoType(RepoType.JSON).fullName("fullName").defaultBranch("main").build();
            when(tableService.addTable(any(Table.class))).thenReturn(table);
        response = schemaFileAuditService.createSchemaFileAuditForNewFile(clientRepo, table, null);
        assertEquals(response.getName(), schemaFileAudit.getName());
    }

    @Test
    public void getTablesFromFileContentTest() throws IOException {
        ReflectionTestUtils.setField(schemaFileAuditService, "avroSchema", avroSchema);
        var response = schemaFileAuditService.getTablesFromFileContent(null, null, "avsc");
        assertEquals(0, response.size());

        ReflectionTestUtils.setField(schemaFileAuditService, "jsonSchema", jsonSchema);
        response = schemaFileAuditService.getTablesFromFileContent(null, null, "json");
        assertEquals(0, response.size());

        assertNull(schemaFileAuditService.getTablesFromFileContent(null, null, "proto"));
    }

    private static final String PULL_REQUEST_ID = "pullRequestId";

    @Test
    public void updateSchemaFileAuditSetPrIdToNullTest() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createCriteriaUpdate(SchemaFileAudit.class)).thenReturn(criteriaUpdate);
        when(criteriaUpdate.from(SchemaFileAudit.class)).thenReturn(root);

        when(criteriaBuilder.equal(root.get(PULL_REQUEST_ID), 1L)).thenReturn(mock(Predicate.class));
        when(criteriaUpdate.where(any(Predicate.class))).thenReturn(criteriaUpdate);
        when(criteriaUpdate.set(PULL_REQUEST_ID, null)).thenReturn(criteriaUpdate);
        when(entityManager.createQuery(criteriaUpdate)).thenReturn(typedQuery);
        when(typedQuery.executeUpdate()).thenReturn(0);

        var response = schemaFileAuditService.updateSchemaFileAuditSetPrIdToNull(1L);
        assertEquals(response, 0);
    }

    @Test
    public void deleteSchemaFileAuditByPrIdTest() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createCriteriaDelete(SchemaFileAudit.class)).thenReturn(criteriaDelete);
        when(criteriaDelete.from(SchemaFileAudit.class)).thenReturn(root);

        when(criteriaBuilder.equal(root.get(PULL_REQUEST_ID), 1L)).thenReturn(mock(Predicate.class));
        when(criteriaDelete.where(any(Predicate.class))).thenReturn(criteriaDelete);
        when(entityManager.createQuery(criteriaDelete)).thenReturn(typedQuery);
        when(typedQuery.executeUpdate()).thenReturn(0);

        var response = schemaFileAuditService.deleteSchemaFileAuditByPrId(1L);
        assertEquals(response, 0);
    }

    @Test
    public void getModelTest() {
        var schemaFileAudit = getSchemaFileAudit();
            when(schemaFileAuditRepository.findById(1L)).thenReturn(Optional.of(schemaFileAudit));
        var response = schemaFileAuditService.getModel(1L);
        assertEquals(response.getName(), schemaFileAudit.getName());
        
        assertThrows(RecordNotFoundException.class, () -> schemaFileAuditService.getModel(2L));
    }

    @Test
    public void getByModelNodeIdTest() {
        var schemaFileAudit = getSchemaFileAudit();
            when(schemaFileAuditRepository.findByModelNodeId(1L)).thenReturn(List.of(schemaFileAudit));
        var response = schemaFileAuditService.getByModelNodeId(1L);
        assertEquals(response.size(), 1);
        assertEquals(response.get(0).getName(), schemaFileAudit.getName());
    }

    @Test
    public void getByModelNodeIdsTest() {
        var schemaFileAudit = getSchemaFileAudit();
            when(schemaFileAuditRepository.findByModelNodeIds(List.of(1L))).thenReturn(List.of(schemaFileAudit));
        var response = schemaFileAuditService.getByModelNodeIds(List.of(1L));
        assertEquals(response.size(), 1);
        assertEquals(response.get(0).getName(), schemaFileAudit.getName());
    }

    @Test
    public void getSchemaFileAuditTest() {
        assertNull(schemaFileAuditService.getSchemaFileAudit("path1"));

        var schemaFileAudit = getSchemaFileAudit();
            when(schemaFileAuditRepository.findOne(ArgumentMatchers.<Specification<SchemaFileAudit>>any())).thenReturn(Optional.of(schemaFileAudit));
        var response = schemaFileAuditService.getSchemaFileAudit("path1");
        assertEquals(response.getName(), schemaFileAudit.getName());
    }

    @Test
    public void getSchemaFileAuditTwoTest() {
        assertNull(schemaFileAuditService.getSchemaFileAudit("path1", 1L));
        
        var schemaFileAudit = getSchemaFileAudit();
            when(schemaFileAuditRepository.findOne(ArgumentMatchers.<Specification<SchemaFileAudit>>any())).thenReturn(Optional.of(schemaFileAudit));
        var response = schemaFileAuditService.getSchemaFileAudit("path1", 1L);
        assertEquals(response.getName(), schemaFileAudit.getName());
    }

    @Test
    public void getAllTest() {
        var schemaFileAudit = getSchemaFileAudit();
            when(schemaFileAuditRepository.findAll(ArgumentMatchers.<Specification<SchemaFileAudit>>any())).thenReturn(List.of(schemaFileAudit));
        var response = schemaFileAuditService.getAll(null);
        assertEquals(1, response.size());
        assertEquals(schemaFileAudit.getName(), response.get(0).getName());

            when(schemaFileAuditRepository.findAll(ArgumentMatchers.<Specification<SchemaFileAudit>>any())).thenReturn(List.of());
        response = schemaFileAuditService.getAll(1L);
        assertEquals(0, response.size());
    }

    @Test
    public void saveDeltaForProtoSchemaTest() {
        var schemaFileAudit = getSchemaFileAudit();
        var clientRepo = ClientRepo.builder().id(1L).repoType(RepoType.AVRO).fullName("fullName").defaultBranch("main").build();
        var pullRequest = PullRequest.builder().id(1L).sourceBranch("develop").build();
        String[] filePaths = {"src/test/resources/schema_1/avro/avro_testing.avsc", "src/test/resources/schema_1/json/product.json"};
        var table = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(Field.builder().id(1L).build())).build();
        var fileTables = Map.of(filePaths[0], List.of(table), filePaths[1], List.of(table));
            ReflectionTestUtils.setField(schemaFileAuditService, "protoSchema", protoSchema);
            when(protoSchema.getTablesOfFilePaths(filePaths, clientRepo, pullRequest.getSourceBranch())).thenReturn(fileTables);
        var domain = Domain.builder().id(1L).name(clientRepo.getFullName()).build();
            when(domainService.getDefaultDomain(anyString())).thenReturn(domain);
            ReflectionTestUtils.setField(schemaFileAuditService, "githubFilePath", githubFilePath);
        var model = Model.builder().id(1L).nodeId(table.getId()).name(table.getName()).nameSpace(table.getNameSpace()).build();
            when(tableService.findDeltaForFields(anyMap(), anyLong(), any(RepoType.class))).thenReturn(List.of(table, table));

        schemaFileAudit = SchemaFileAudit.builder().id(1L).name("avro_testing").fileType("avsc").path("path").rootNodeId(1L).build();
            when(schemaFileAuditRepository.findOne(ArgumentMatchers.<Specification<SchemaFileAudit>>any())).thenReturn(Optional.of(schemaFileAudit));
            when(modelService.findBySchemaFileAudit(anyLong())).thenReturn(List.of(model));
            when(tableService.findDeltaForTable(any(Table.class), anyLong(), anyLong())).thenReturn(table);
        var response = schemaFileAuditService.saveDeltaForProtoSchema(filePaths, clientRepo, pullRequest);
        assertEquals(response.size(), 2);
    }

    @Test
    public void saveDeltaTest() throws IOException {
        var schemaFileAudit = getSchemaFileAudit();
        var clientRepo = ClientRepo.builder().id(1L).repoType(RepoType.AVRO).fullName("fullName").defaultBranch("main").build();
        var pullRequest = PullRequest.builder().id(1L).sourceBranch("develop").build();
        String[] filePaths = {"src/test/resources/schema_1/avro/avro_testing.avsc", "src/test/resources/schema_1/json/product.avsc"};
        var table = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(Field.builder().id(1L).build())).build();
        var fileTables = Map.of(filePaths[0], table.toString().getBytes(), filePaths[1], table.toString().getBytes());
            ReflectionTestUtils.setField(schemaFileAuditService, "avroSchema", avroSchema);
            when(avroSchema.getTables(table.toString().getBytes(), Boolean.FALSE)).thenReturn(List.of(table));
        var domain = Domain.builder().id(1L).name(clientRepo.getFullName()).build();
            when(domainService.getDefaultDomain(anyString())).thenReturn(domain);
            when(schemaFileAuditRepository.save(any(SchemaFileAudit.class))).thenReturn(schemaFileAudit);
            ReflectionTestUtils.setField(schemaFileAuditService, "githubFilePath", githubFilePath);
            when(tableService.saveNewTableWithDelta(any(Table.class), anyLong())).thenReturn(table);
        var model = Model.builder().id(1L).nodeId(table.getId()).name(table.getName()).nameSpace(table.getNameSpace()).build();
            when(modelService.createModel(any(Table.class), any(SchemaFileAudit.class), any(Domain.class), anyLong())).thenReturn(model);
            when(modelService.addModel(any(Model.class))).thenReturn(model);
            when(tableService.findDeltaForFields(anyMap(), anyLong(), any(RepoType.class))).thenReturn(List.of(table, table));
        var response = schemaFileAuditService.saveDelta(fileTables, clientRepo, pullRequest.getId());
        assertEquals(response.size(), 2);
    }

    @Test
    public void saveDeltaTestFail() {
        var clientRepo = ClientRepo.builder().id(1L).repoType(RepoType.AVRO).fullName("fullName").defaultBranch("main").build();
        String[] filePaths = {"src/test/resources/schema_1/avro/avro_testing.avsc", "src/test/resources/schema_1/json/product.avsc"};
        var table = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(Field.builder().id(1L).build())).build();
        var fileTables = Map.of(filePaths[0], table.toString().getBytes(), filePaths[1], table.toString().getBytes());
            ReflectionTestUtils.setField(schemaFileAuditService, "avroSchema", avroSchema);
            try {
                when(avroSchema.getTables(table.toString().getBytes(), Boolean.FALSE)).thenThrow(IOException.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        assertThrows(SchemaParserException.class, () -> schemaFileAuditService.saveDelta(fileTables, clientRepo, 1L));
    }

    private Table getTable() {
        var nameField = Field.builder().id(1L).name("name").dataType("string").isPrimitiveType(Boolean.TRUE).rowNumber(1).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).build();
        var scoreFieldDelete = Field.builder().id(2L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).rowNumber(2).prId(1L).isUserChanged(Boolean.TRUE).isDeleted(Boolean.TRUE).build();
        var scoreFieldUpdated = Field.builder().id(3L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).rowNumber(2).prId(1L).isUserChanged(Boolean.TRUE).isDeleted(Boolean.FALSE).build();

        List<Field> fields = new ArrayList<>();
        fields.add(nameField); fields.add(scoreFieldDelete); fields.add(scoreFieldUpdated);
        return Table.builder().id(1L).name("student").modifiedTable(Table.builder().id(2L).name("student").owner("core").build())
                    .isUserChanged(Boolean.TRUE).isDeleted(Boolean.TRUE).fields(fields).build();
    }

    @Test
    public void generateFileContentOfSchemaTest() {
        ReflectionTestUtils.setField(schemaFileAuditService, "protoSchema", protoSchema);
        assertEquals(0, schemaFileAuditService.generateFileContentOfSchema(10L, RepoType.PROTOBUF).size());

        var table = getTable();
        var schemaFileAudit = SchemaFileAudit.builder().id(1L).name("avro_testing").fileType("avsc").path("path")
                                .rootNodeId(table.getId()).build();
            ReflectionTestUtils.setField(schemaFileAuditService, "avroSchema", avroSchema);
            when(schemaFileAuditRepository.findByModelNodeId(anyLong())).thenReturn(List.of(schemaFileAudit));
            when(tableService.findAllById(anyList())).thenReturn(List.of(table));
        var response = schemaFileAuditService.generateFileContentOfSchema(table.getId(), RepoType.AVRO);
        assertEquals(response.size(), 1);
    }

    @Test
    public void generateFileContentOfSchemaTest2() {
        var table = getTable();
        var schemaFileAudit = SchemaFileAudit.builder().id(1L).name("Json_testing").fileType("json").path("path")
                                .rootNodeId(table.getId()).build();
            ReflectionTestUtils.setField(schemaFileAuditService, "jsonSchema", jsonSchema);
            when(schemaFileAuditRepository.findByModelNodeIds(anyList())).thenReturn(List.of(schemaFileAudit));
            when(tableService.findAllById(anyList())).thenReturn(List.of(table));
        var response = schemaFileAuditService.generateFileContentOfSchema(List.of(table.getId()));
        assertEquals(response.size(), 1);
    }

    @Test
    public void filterFieldsTest() {
        var scoreFieldDelete = Field.builder().id(1L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).rowNumber(1).isUserChanged(Boolean.TRUE).isDeleted(Boolean.TRUE).build();
        var scoreFieldUpdated = Field.builder().id(2L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).rowNumber(1).isUserChanged(Boolean.TRUE).isDeleted(Boolean.FALSE).build();

        var arrayField = Field.builder().id(3L).rowNumber(2).name("arrayField").dataType("array").isPrimitiveType(Boolean.FALSE).items("string").build(); 
        var mapField = Field.builder().id(4L).name("mapFiled").dataType("map").rowNumber(3).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).values("string").isPrimitiveType(Boolean.TRUE).build();
        var unionField = Field.builder().id(5L).name("unionfield").dataType("union").rowNumber(4).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).unionTypes(List.of(Field.builder().id(8L).rowNumber(4).dataType("null").isPrimitiveType(Boolean.TRUE).build(), Field.builder().id(9L).rowNumber(4).dataType("int").isPrimitiveType(Boolean.TRUE).build())).isPrimitiveType(Boolean.FALSE).build();
        var tableField = Field.builder().id(6L).name("tablefiled").dataType("Marks").rowNumber(5).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).contain(Table.builder().id(10L).name("Marks").schemaType(SchemaType.ENTITY).fields(List.of(Field.builder().id(11L).rowNumber(1).name("Marks").dataType("string").isPrimitiveType(Boolean.TRUE).build())).build()).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.TRUE).isDeleted(Boolean.TRUE).prId(1L).build();

        var arrayOfArray = Field.builder().id(7L).name("arrayOfArray").dataType("array").rowNumber(2).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).items("array").arrayField(arrayField).build();
        var arrayOfUnion = Field.builder().id(8L).name("ArrayOfUnion").dataType("array").rowNumber(3).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).items("union").unionTypes(unionField.getUnionTypes()).build();
        var arrayOfObject = Field.builder().id(9L).name("ArrayOfObject").dataType("array").rowNumber(4).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).items("table").contain(tableField.getContain()).build();
        var arrayOfMap = Field.builder().id(10L).name("ArrayOfMap").dataType("array").rowNumber(5).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).items("map").mapField(mapField).build();

        var mapOfArray = Field.builder().id(11L).name("mapOfArray").dataType("map").rowNumber(6).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).values("array").arrayField(arrayField).build();
        var mapOfUnion = Field.builder().id(12L).name("MapOfUnion").dataType("map").rowNumber(7).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).values("union").unionTypes(unionField.getUnionTypes()).build();
        var mapOfObject = Field.builder().id(13L).name("MapOfObject").dataType("map").rowNumber(8).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).values("table").contain(tableField.getContain()).build();
        var mapOfMap = Field.builder().id(14L).name("MapOfMap").dataType("map").rowNumber(9).isPrimitiveType(Boolean.FALSE).isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).values("map").mapField(mapField).build();

        List<Field> fields = new ArrayList<>();
        fields.add(scoreFieldDelete); fields.add(scoreFieldUpdated); fields.add(unionField);
        fields.add(arrayOfUnion); fields.add(arrayOfObject); fields.add(arrayOfMap); fields.add(arrayOfArray);
        fields.add(mapOfUnion); fields.add(mapOfObject); fields.add(mapOfMap); fields.add(mapOfArray);
        
        var table = Table.builder().id(12L).name("student").schemaType(SchemaType.ENTITY)
                         .isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).fields(fields).build();

        var response = schemaFileAuditService.filterFields(table);
        assertEquals(response.getFields().size(), fields.size()-1);
    }

    @Test
    public void deleteAllByClientRepoIdTest() {
        schemaFileAuditService.deleteAllByClientRepoId(1L);
    }
}
