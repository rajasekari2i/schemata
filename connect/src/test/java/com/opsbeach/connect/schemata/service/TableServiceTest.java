package com.opsbeach.connect.schemata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.InternalNode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jClient.RecordFetchSpec;
import org.springframework.data.neo4j.core.Neo4jClient.RunnableSpec;
import org.springframework.data.neo4j.core.Neo4jClient.UnboundRunnableSpec;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.service.ClientRepoService;
import com.opsbeach.connect.github.service.GitHubService;
import com.opsbeach.connect.github.service.ModelService;
import com.opsbeach.connect.github.service.SchemaFileAuditService;
import com.opsbeach.connect.schemata.dto.SchemaValidationDto;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.SchemaType;
import com.opsbeach.connect.schemata.repository.FieldRepostory;
import com.opsbeach.connect.schemata.repository.TableRepository;
import com.opsbeach.connect.schemata.validate.SchemaValidator;
import com.opsbeach.connect.schemata.validate.Status;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.utils.StringUtil;

public class TableServiceTest {
    
    @InjectMocks
    private TableService tableService;
    @Mock
    private TableRepository tableRepository;
    @Mock
    private FieldRepostory fieldRepostory;
    @Mock
    private DomainNodeService domainNodeService;
    @Mock
    private ResponseMessage responseMessage;
    @Mock
    private ModelService modelService;
    @Mock
    private SchemaValidator schemaValidator;
    @Mock
    private SchemaFileAuditService schemaFileAuditService;
    @Mock
    private Neo4jClient neo4jClient;
    @Mock
    private UnboundRunnableSpec unboundRunnableSpec;
    @Mock
    private RunnableSpec runnableSpec;

    @Mock
    private RecordFetchSpec<Map<String,Object>> recordFetchSpec;
    @Mock
    private ClientRepoService clientRepoService;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void mockApplicationUser() {
        UserDto userDto = mock(UserDto.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDto);
    }

    private Table getTable() {
        var primitiveField = Field.builder().id(1L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).build();
        var arrayField = Field.builder().id(2L).name("array field").dataType("array").items("array").arrayField(Field.builder().id(3L).name("array field").dataType("array").items("string").build()).isPrimitiveType(Boolean.FALSE).build();
        var mapField = Field.builder().id(4L).name("map filed").dataType("map").values("map").mapField(Field.builder().id(5L).name("map field").dataType("map").values("string").build()).isPrimitiveType(Boolean.FALSE).build();
        var unionField = Field.builder().id(6L).name("union field").dataType("union").unionTypes(List.of(Field.builder().id(7L).dataType(null).isPrimitiveType(Boolean.TRUE).build(), Field.builder().id(8L).dataType("int").isPrimitiveType(Boolean.TRUE).build())).isPrimitiveType(Boolean.FALSE).build();
        var tableField = Field.builder().id(9L).name("table filed").dataType("com.acme.Marks").contain(Table.builder().id(10L).name("Marks").nameSpace("com.acme").isUserChanged(Boolean.FALSE).isDeleted(Boolean.FALSE).schemaType(SchemaType.ENTITY).fields(List.of(Field.builder().id(11L).dataType("string").build())).build()).isPrimitiveType(Boolean.FALSE).build();
        var fields = List.of(primitiveField, arrayField, unionField, mapField, tableField);
        return Table.builder().id(12L).name("student").nameSpace("org").schemaType(SchemaType.ENTITY).fields(fields).build();
    }

    private Table getModifiedTable() {
        var table = getTable();
        table.setModifiedTable(Table.builder().description("desc").owner("owner").domain("domain").isUserChanged(Boolean.TRUE).isDeleted(Boolean.FALSE).build());
        table.setIsUserChanged(Boolean.TRUE); table.setIsDeleted(Boolean.TRUE);
        var name = Field.builder().id(12L).name("name").dataType("string").isUserChanged(Boolean.TRUE).isDeleted(Boolean.TRUE).isPrimitiveType(Boolean.TRUE).build();
        var nameModified = Field.builder().id(12L).name("name").dataType("string").isUserChanged(Boolean.TRUE).isDeleted(Boolean.FALSE).isPii(Boolean.TRUE).isPrimitiveType(Boolean.TRUE).build();
        var referedField = Field.builder().id(13L).name("college_id").dataType("int").isUserChanged(Boolean.FALSE).isUserChanged(Boolean.FALSE).referenceField(Field.builder().id(14L).name("id").dataType("int").build()).build();
        List<Field> fields = new ArrayList<>(table.getFields());
        fields.add(name); fields.add(referedField);
        fields.add(nameModified);
        table.setFields(fields);
        return table;
    }
    
    @Test
    public void addTableTest() {
        var table = getTable();
        when(tableRepository.save(table)).thenReturn(table);
        var response = tableService.addTable(table);
        assertEquals(table.getName(), response.getName());
        assertEquals(table.getFields().get(0).getDataType(), response.getFields().get(0).getDataType());
    }
    
    @Test
    public void addTablesTest() {
        var table = getTable();
        var tables = List.of(table);
        when(tableRepository.saveAll(tables)).thenReturn(tables);
        var response = tableService.addTables(tables);
        assertEquals(table.getName(), response.get(0).getName());
        assertEquals(table.getFields().get(0).getDataType(), response.get(0).getFields().get(0).getDataType());
    }

    @Test
    public void getTableTest() {
        var table = getTable();
            when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));
            when(modelService.getByNodeId(anyLong())).thenReturn(ClientRepo.builder().id(1L).repoType(RepoType.AVRO).build());
        var response = tableService.get(table.getId());
        assertEquals(table.getName(), response.getName());

        table = getModifiedTable();
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));
        response = tableService.get(table.getId());
        assertEquals(table.getName(), response.getName());
        
        assertThrows(RecordNotFoundException.class, () -> { tableService.get(3L); });
    }

    @Test
    public void getSchemaVisualizerDtoAndParsingTest() {
        var table = getModifiedTable();
            when(tableRepository.getTableIdsConnectedToTable(anyLong())).thenReturn(new HashSet<>());
            when(fieldRepostory.getTableIdOfField(anyLong())).thenReturn(1L);
            when(tableRepository.findAllById(anyCollection())).thenReturn(List.of(table));
            when(tableRepository.findById(1L)).thenReturn(Optional.of(getTable()));
        var responseSchemaVisualizerDto = tableService.getSchemaVisualizer(1L);
        assertEquals(table.getName(), responseSchemaVisualizerDto.getTables().get(0).getName());
        assertEquals(table.getId(), responseSchemaVisualizerDto.getLinks().get(0).get("source"));

        // var tableResponse = tableService.parseSchemaVisualizerDto(table.getId(), responseSchemaVisualizerDto, SchemaVisualizerDto.Purpose.SUBMIT);
        // assertEquals(table.getId(), tableResponse.get(0).getId());
        // assertEquals(table.getFields().size(), tableResponse.get(0).getFields().size());
    }

    // @Test
    // public void computeScoresTest() {
    //     var fields = List.of(Field.builder().id(1L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).build());
    //     var table = Table.builder().id(12L).name("student").nameSpace("org").schemaType(SchemaType.ENTITY).fields(fields).build();
    //         when(tableRepository.getTableIdsConnectedToTable(anyLong())).thenReturn(new HashSet<>());
    //         when(tableRepository.findAllById(anyCollection())).thenReturn(List.of(table));
    //     var responseSchemaVisualizerDto = tableService.getSchemaVisualizer(1L);

    //     table.setFields(fields);
    //         when(modelService.getNodeIds()).thenReturn(List.of(table.getId()));
    //         when(tableRepository.findAllById(List.of(table.getId()))).thenReturn(List.of(table));
    //     var score = tableService.computeScores(responseSchemaVisualizerDto, table.getId());
    //     assertEquals(0.0, score.get("student"));
    // }

    @Test
    public void getAllSchemaVisualizerDtoTest() {
        var table = getTable();
            when(modelService.getNodeIds()).thenReturn(List.of(table.getId()));
            when(tableRepository.findAllById(List.of(table.getId()))).thenReturn(List.of(table));
        var response = tableService.getSchemaVisualizerForAll();
        assertEquals(2, response.getTables().size());
    }

    @Test
    public void findByNameAndNameSpaceTest() {
        var table = getTable();
        mockApplicationUser();
            when(tableRepository.findByNameAndNameSpaceAndClientId(anyString(), anyString(), anyLong())).thenReturn(table);
        var response = tableService.findByNameAndNameSpace("table", "nameSpace");
        assertEquals(table.getName(), response.getName());
    }

    @Test
    public void getTableFilterOptionsTest() {
        var owners = List.of("owner");
        var domains = List.of("domain");
            mockApplicationUser();
            when(tableRepository.getAllOwner(anyLong())).thenReturn(owners);
            when(tableRepository.getAllDomain(anyLong())).thenReturn(domains);
            when(tableRepository.getAllSubscribers(anyLong())).thenReturn(List.of("[\"subscriber\"]"));
        var response = tableService.getTableFilterOptions();
        assertEquals(response.owners().get(0), owners.get(0));
        assertEquals(response.domains().get(0), domains.get(0));
        assertTrue(response.subscribers().contains("subscriber"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAllTest() {
        // Neo4j DB elementId is changed from "2" to "1:2:3". that's why
        Object aNode = new InternalNode(1L, "2:2:1", null, Map.of("name", Values.value("table1"), "owner", Values.value("owner1")));
        Object bNode = new InternalNode(2L, "2:2:2", null, Map.of("name", Values.value("table2"), "owner", Values.value("owner2")));
        Collection<Map<String,Object>> value1 = List.of(Map.of("a", aNode, "b", bNode));
        Collection<Map<String,Object>> value2 = List.of();
            Mockito.when(neo4jClient.query(Mockito.anyString())).thenReturn(unboundRunnableSpec);
            Mockito.when(unboundRunnableSpec.bindAll(anyMap())).thenReturn(runnableSpec);
            Mockito.when(runnableSpec.fetch()).thenReturn(recordFetchSpec);
            Mockito.when(recordFetchSpec.all()).thenReturn(value1, value2);
        var response = tableService.getAll(List.of("owner", "default"), List.of("domain", "default"), List.of("subscribers", "default"), PageRequest.of(0, 4));
        assertEquals("table1", response.get("tables").get(0).get("name").asText());
        assertEquals("owner2", response.get("tables").get(0).get("owner").asText());
        assertEquals(0, response.get("total").asInt());

        response = tableService.getAll(List.of(), List.of(), List.of(), PageRequest.of(0, 4));
        assertEquals(0, response.get("tables").size());
            
        Collection<Map<String,Object>> value = List.of(Map.of("a", aNode));
            Mockito.when(recordFetchSpec.all()).thenReturn(value, value2);
        response = tableService.getAll(List.of("owner"), List.of("domain"), List.of("subscribers"), PageRequest.of(0, 4));
        assertEquals("table1", response.get("tables").get(0).get("name").asText());
        assertEquals("owner1", response.get("tables").get(0).get("owner").asText());
        assertEquals(0, response.get("total").asInt());
    }

    @Test
    public void addFieldsTest() {
        var field = Field.builder().id(1L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).build();
        when(fieldRepostory.saveAll(anyList())).thenReturn(List.of(field));
        var response = tableService.addFields(List.of(field));
        assertEquals(response.get(0).getName(), field.getName());
    }

    @Test
    public void addFieldTest() {
        var field = Field.builder().id(1L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).build();
        when(fieldRepostory.save(any(Field.class))).thenReturn(field);
        var response = tableService.addField(field);
        assertEquals(response.getName(), field.getName());
    }

    @Test
    public void getFieldModelTest() {
        var field = Field.builder().id(1L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).build();
        when(fieldRepostory.findById(1L)).thenReturn(Optional.of(field));
        var response = tableService.getFieldModel(1L);
        assertEquals(response.getName(), field.getName());

        assertThrows(RecordNotFoundException.class, () -> { tableService.getFieldModel(3L); });
    }

    @Test
    public void computeScoresTest() {
        var table = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(Field.builder().id(1L).name("id").dataType("int").build())).build();
            when(modelService.getNodeIds()).thenReturn(List.of(table.getId()));
            when(tableRepository.findAllById(List.of(table.getId()))).thenReturn(List.of(table));
        var response = tableService.computeScores(List.of(table));
        assertEquals(response.get(table.getName()), 0.0);
    }

    @Test
    public void getFieldDataTypesTest() {
        when(modelService.getByNodeId(anyLong())).thenReturn(ClientRepo.builder().repoType(RepoType.AVRO).build(), 
                                                             ClientRepo.builder().repoType(RepoType.JSON).build(), 
                                                             ClientRepo.builder().repoType(RepoType.PROTOBUF).build(),
                                                             ClientRepo.builder().repoType(RepoType.YAML).build());
        var response = tableService.getFieldDataTypes(1L);
        assertEquals(response, List.of("string", "int", "float", "double", "long", "bytes", "boolean", "null"));
        response = tableService.getFieldDataTypes(2L);
        assertEquals(response, List.of("string", "number" ,"integer" ,"boolean", "null"));
        response = tableService.getFieldDataTypes(1L);
        assertEquals(response, List.of("double", "float", "int32", "int64", "uint32", "uint64", "sint32", "sint64", "fixed32", "fixed64", "sfixed32", "sfixed64", "bool", "string", "bytes"));
        response = tableService.getFieldDataTypes(1L);
        assertEquals(response, List.of());
    }

    @Test
    public void findDeltaForTableTest() {
        var table = getTable();
            when(tableRepository.findById(anyLong())).thenReturn(Optional.of(table));
        var response = tableService.findDeltaForTable(table, table.getId(), 2L);
        assertNull(response.getModifiedTable());
        
        var tableNew = Table.builder().description("des").build();
        table.setModifiedTable(tableNew);
            when(tableRepository.save(any(Table.class))).thenReturn(table);
        response = tableService.findDeltaForTable(tableNew, table.getId(), 2L);
        assertNotNull(response.getModifiedTable());
    }

    @Test
    public void saveNewTableWithDeltaTest() {
        var table = getTable();
        table.setPrId(1L);
            when(tableRepository.save(any(Table.class))).thenReturn(table);
        var response = tableService.saveNewTableWithDelta(table, 1L);
        assertNotNull(response.getPrId());
            mockApplicationUser();
            when(tableRepository.findByNameAndNameSpaceAndClientId(anyString(), anyString(), anyLong())).thenReturn(getTable());
        response = tableService.saveNewTableWithDelta(table, 1L);
        assertNull(response.getPrId());
    }

    @Test
    public void findDeltaForFieldsTest() {
        var table = getTable();
        List<Field> fields = new ArrayList<>();
        fields.add(Field.builder().name("score").dataType("int").description("des").isPrimitiveType(Boolean.TRUE).isPii(Boolean.TRUE).build());
        fields.add(Field.builder().name("name").dataType("string").isPrimitiveType(Boolean.TRUE).isPii(Boolean.TRUE).build());
        fields.addAll(table.getFields());
        var mapOfTable = Field.builder().name("mapOfTable").dataType("map").values("com.product").isPrimitiveType(Boolean.FALSE).contain(table).build();
        var arrayOfMap = Field.builder().name("arrayOfMap").dataType("array").items("map").isPrimitiveType(Boolean.FALSE).mapField(mapOfTable).build();
        var arrayOfTable = Field.builder().name("arrayOfTable").dataType("array").items("com.product").isPrimitiveType(Boolean.FALSE).contain(table).build();
        var mapOfArray = Field.builder().name("mapOfArray").dataType("map").values("array").isPrimitiveType(Boolean.FALSE).arrayField(arrayOfTable).build();
        var arrayOfUnion = Field.builder().name("arrayOfUnion").dataType("array").items("union").isPrimitiveType(Boolean.FALSE).unionTypes(List.of(arrayOfMap)).build();
        var mapOfUnion = Field.builder().name("arrayOfUnion").dataType("map").values("union").isPrimitiveType(Boolean.FALSE).unionTypes(List.of(mapOfArray)).build();
        fields.add(arrayOfUnion); fields.add(mapOfUnion);
        var tableWithNewFields = Map.of(StringUtil.constructStringEmptySeparator(table.getNameSpace(),".",table.getName()), fields);
        
        table.setFields(List.of(Field.builder().id(2L).name("age").dataType("int").isPrimitiveType(Boolean.TRUE).build(),
                                Field.builder().name("name").dataType("string").isPrimitiveType(Boolean.TRUE).isPii(Boolean.TRUE).build(),
                                Field.builder().id(1L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).build()));
            mockApplicationUser();
            when(tableRepository.findByNameAndNameSpaceAndClientId(anyString(), anyString(), anyLong())).thenReturn(table);
            when(tableRepository.save(any(Table.class))).thenReturn(table);
        var response = tableService.findDeltaForFields(tableWithNewFields, 1L, RepoType.AVRO);
        assertEquals(response.get(0).getName(), table.getName());
    }

    @Test
    public void compareFieldTest() {
        var field1 = Field.builder().dataType("int").description("desc").build();
        var field2 = Field.builder().dataType("long").description("desc").build();
        assertFalse(tableService.compareField(field1, field1, RepoType.AVRO));
        assertTrue(tableService.compareField(field1, field2, RepoType.PROTOBUF));
        assertFalse(tableService.compareField(field1, field2, RepoType.AVRO));
        field1.setDescription("desc1");
        assertTrue(tableService.compareField(field1, field2, RepoType.AVRO));
        field1.setDescription("desc");
        field1.setIsPii(Boolean.FALSE); field2.setIsPii(Boolean.TRUE);
        assertTrue(tableService.compareField(field1, field2, RepoType.AVRO));
        field1.setIsPii(Boolean.FALSE); field2.setIsPii(Boolean.FALSE);
        field1.setIsClassified(Boolean.FALSE); field2.setIsClassified(Boolean.TRUE);
        assertTrue(tableService.compareField(field1, field2, RepoType.AVRO));
        field1.setIsClassified(Boolean.FALSE); field2.setIsClassified(Boolean.FALSE);
        field1.setDeprecated(Boolean.FALSE); field2.setDeprecated(Boolean.TRUE);
        assertTrue(tableService.compareField(field1, field2, RepoType.AVRO));
        field1.setDeprecated(null); field2.setDeprecated(Boolean.TRUE);
        assertTrue(tableService.compareField(field1, field2, RepoType.AVRO));
        field1.setDeprecated(Boolean.FALSE); field2.setDeprecated(null);
        assertTrue(tableService.compareField(field1, field2, RepoType.AVRO));
        field1.setDeprecated(null); field2.setDeprecated(null);
        assertFalse(tableService.compareField(field1, field2, RepoType.AVRO));
    }

    @Test
    public void compareTableTest() {
        var table1 = Table.builder().description("desc").owner("owner").build();
        var table2 = Table.builder().description("desc").owner("owner1").build();
        assumeTrue(tableService.compareTable(table1, table2));
        table2.setOwner(table1.getOwner());
        table1.setComplianceOwner("owner"); table2.setComplianceOwner("owner1");
        assumeTrue(tableService.compareTable(table1, table2));
        table2.setComplianceOwner(table1.getComplianceOwner());
        table1.setChannel("channel"); table2.setChannel("channel1");
        assumeTrue(tableService.compareTable(table1, table2));
        table2.setChannel(table1.getChannel());
        table1.setEmail("email"); table2.setEmail("email2");
        assumeTrue(tableService.compareTable(table1, table2));
        table2.setEmail(table1.getEmail());
        table1.setQualityRuleBase("email"); table2.setQualityRuleBase("email2");
        assumeTrue(tableService.compareTable(table1, table2));
        table2.setEmail(table1.getEmail());
        table1.setQualityRuleBase("email"); table2.setQualityRuleBase("email2");
        assumeTrue(tableService.compareTable(table1, table2));
        table2.setQualityRuleBase(table1.getQualityRuleBase());
        table1.setQualityRuleCel("email"); table2.setQualityRuleCel("email2");
        assumeTrue(tableService.compareTable(table1, table2));
        table2.setQualityRuleCel(table1.getQualityRuleCel());
        table1.setQualityRuleSql("email"); table2.setQualityRuleSql("email2");
        assumeTrue(tableService.compareTable(table1, table2));
        String[] subs1 = {"marketing"};
        String[] subs2 = {"sales"};
        table1.setSubscribers(subs1); table2.setSubscribers(subs2);
        assumeTrue(tableService.compareTable(table1, table2));
    }
    
    @Test
    public void revertChangesTest() {
        var tables = List.of(Table.builder().id(1L).isDeleted(Boolean.TRUE).isUserChanged(Boolean.TRUE).build(),
                             Table.builder().id(1L).isDeleted(Boolean.FALSE).isUserChanged(Boolean.TRUE).build(),
                             Table.builder().id(1L).isDeleted(Boolean.TRUE).isUserChanged(Boolean.FALSE).build());
        var fields = List.of(Field.builder().id(1L).isDeleted(Boolean.TRUE).isUserChanged(Boolean.TRUE).build(),
                             Field.builder().id(1L).isDeleted(Boolean.FALSE).isUserChanged(Boolean.TRUE).build(),
                             Field.builder().id(1L).isDeleted(Boolean.TRUE).isUserChanged(Boolean.FALSE).build());
            when(fieldRepostory.findByPrId(anyLong())).thenReturn(fields);
            when(tableRepository.findByPrId(anyLong())).thenReturn(tables);
            ReflectionTestUtils.setField(tableService, "schemaFileAuditService", schemaFileAuditService);
        assertTrue(tableService.revertChanges(10L, 2L));
    }

    @Test
    public void acceptChangesTest() {
            ReflectionTestUtils.setField(tableService, "schemaFileAuditService", schemaFileAuditService);
        assertTrue(tableService.acceptChanges(10L, 2L));

        var tables = List.of(Table.builder().id(1L).isDeleted(Boolean.TRUE).isUserChanged(Boolean.TRUE).modifiedTable(Table.builder().build()).build(),
                             Table.builder().id(1L).isDeleted(Boolean.FALSE).isUserChanged(Boolean.TRUE).build(),
                             Table.builder().id(1L).isDeleted(Boolean.TRUE).isUserChanged(Boolean.FALSE).build());
        var fields = List.of(Field.builder().id(1L).isDeleted(Boolean.TRUE).isUserChanged(Boolean.TRUE).build(),
                             Field.builder().id(1L).isDeleted(Boolean.FALSE).isUserChanged(Boolean.TRUE).build(),
                             Field.builder().id(1L).isDeleted(Boolean.TRUE).isUserChanged(Boolean.FALSE).build());
            when(fieldRepostory.findByPrId(anyLong())).thenReturn(fields);
            when(tableRepository.findByPrId(anyLong())).thenReturn(tables);
        assertTrue(tableService.acceptChanges(10L, 2L));
    }

    @Test
    public void schemaCompareTest() {
        when(schemaValidator.schemaCompare(anyMap(), any(), anyLong())).thenReturn(SchemaValidationDto.builder().status(true).build());
        var response = tableService.schemaCompare(new HashMap<String, Table>(), ClientRepo.builder().build(), 1L);
        assertTrue(response.getStatus());
    }

    @Test
    public void deleteByIdsTest() {
        var table = getTable();
            when(tableRepository.findAllById(anyList())).thenReturn(List.of(table));
        tableService.deleteByIds(List.of(1L));
    }    
    public class CustomMultipartFile implements MultipartFile {
    
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;
    
        public CustomMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }
    
        @Override
        public String getName() {
            return this.name;
        }
    
        @Override
        public String getOriginalFilename() {
            return this.originalFilename;
        }
    
        @Override
        public String getContentType() {
            return this.contentType;
        }
    
        @Override
        public boolean isEmpty() {
            return this.content.length == 0;
        }
    
        @Override
        public long getSize() {
            return this.content.length;
        }
    
        @Override
        public byte[] getBytes() throws IOException {
            return this.content;
        }
    
        @Override
        public InputStream getInputStream() throws IOException {
            return new java.io.ByteArrayInputStream(this.content);
        }
    
        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                fos.write(this.content);
            }
        }
    }

    @Test
    public void uploadCsvToGitTest() throws IOException {
        var filePath = "src/test/resources/schema_1/csv/sample.csv";
        byte[] content = Files.readAllBytes(new File(filePath).toPath());
        var multipartFile = new CustomMultipartFile("sample.csv", "sample.cav", "text/csv", content);
        var clientRepo = ClientRepo.builder().id(2L).name("schemata").repoType(RepoType.AVRO).fullName("opsbeach/schemata").build();
            when(clientRepoService.getSchemataRepo()).thenReturn(Optional.of(clientRepo));
            when(modelService.findByFullNames(anyList())).thenReturn(List.of(Model.builder().id(1L).name("brand").nameSpace("org.company.ecommerce").build()));
            when(tableRepository.saveAll(anyList())).thenAnswer(invocate -> invocate.getArgument(0));
        ReflectionTestUtils.setField(tableService, "schemaFileAuditService", schemaFileAuditService);
        ReflectionTestUtils.setField(tableService, "gitHubService", Mockito.mock(GitHubService.class));
            when(schemaFileAuditService.createSchemaFileAuditForNewFile(any(ClientRepo.class), any(Table.class), anyString())).thenReturn(SchemaFileAudit.builder().id(1L).build());
        var response = tableService.uploadCsvToGit(List.of(multipartFile));
        assertEquals(Status.SUCCESS.name(), response.toString());

        var models = List.of(
            Model.builder().id(1L).name("brand").nameSpace("org.company.ecommerce").build(),
            Model.builder().id(1L).name("promotion_reviewed").nameSpace("org.amce.ecommerce.promotion").build()
        );
            when(modelService.findByFullNames(anyList())).thenReturn(models);
        response = tableService.uploadCsvToGit(List.of(multipartFile));
        assertEquals(Status.SUCCESS.name(), response.toString());

        var tableContent = new String("""
            table_name_space,table_name,table_description,owner,domain,column_name,data_type,column_description,is_pii,is_classified
            org.company.ecommerce,brand,The table contains all the Brand information and related fields.,#team-brand,sales,id,int,The primary identification of the brand,FALSE,FALSE
                """);
        assertThrows(InvalidDataException.class, () -> tableService.uploadCsvToGit(List.of(new CustomMultipartFile("sample.csv", "sample.cav", "text/csv", tableContent.getBytes()))));
        assertThrows(InvalidDataException.class, () -> tableService.uploadCsvToGit(List.of(new CustomMultipartFile("sample.csv", "sample.cav", "text/pdf", tableContent.getBytes()))));
    }
}   