package com.opsbeach.connect.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsbeach.connect.github.dto.WorkflowDto;
import com.opsbeach.connect.github.dto.WorkflowDto.FieldDto;
import com.opsbeach.connect.github.dto.WorkflowDto.TableDto;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.entity.PullRequest;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.entity.Workflow;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.repository.WorkflowRepository;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.repository.FieldRepostory;
import com.opsbeach.connect.schemata.repository.TableRepository;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.connect.schemata.validate.FieldValidator;
import com.opsbeach.connect.schemata.validate.Result;
import com.opsbeach.connect.schemata.validate.SchemaValidator;
import com.opsbeach.connect.schemata.validate.Status;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.exception.AlreadyExistException;
import com.opsbeach.sharedlib.exception.BadRequestException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

public class WorkflowServiceTest {
    
    @InjectMocks
    private WorkflowService workflowService;
    @Mock
    private DomainService domainService;
    @Mock
    private WorkflowRepository workflowRepository;
    @Mock
    private ResponseMessage responseMessage;
    @Mock
    private TableRepository tableRepository;
    @Mock
    private ClientRepoService clientRepoService;
    @Mock
    private SchemaFileAuditService schemaFileAuditService;
    @Mock
    private GitHubService gitHubService;
    @Mock
    private SchemaValidator schemaValidator;
    @Mock
    private FieldValidator fieldValidator;
    @Mock
    private FieldRepostory fieldRepostory;
    @Mock
    private ModelService modelService;

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

    private WorkflowDto getWorkflowDto() {
        return WorkflowDto.builder().id(1L).domainId(2L).status(Workflow.Status.PR_CLOSED).rank(0).build();
    }

    private Workflow getWorkflow() {
        return Workflow.builder().id(1L).domainId(2L).status(Workflow.Status.PR_CLOSED).rank(0).build();
    }

    @Test
    public void addTest() {
        var workflowDto = getWorkflowDto();
        var workflow = workflowDto.toDomain(workflowDto);
            when(workflowRepository.save(any())).thenReturn(workflow);
        var response = workflowService.add(workflowDto);
        assertEquals(workflowDto.getDomainId(), response.getDomainId());
    }

    @Test
    public void getTest() {
        var workflowDto = getWorkflowDto();
        var workflow = workflowDto.toDomain(workflowDto);
            when(workflowRepository.findById(workflowDto.getId())).thenReturn(Optional.of(workflow));
        var response = workflowService.get(workflowDto.getId());
        assertEquals(workflowDto.getDomainId(), response.getDomainId());
        
        assertThrows(RecordNotFoundException.class, () -> { workflowService.get(2L); });
    }

    @Test
    public void getAllTest() {
        var workflowDto = getWorkflowDto();
        var workflow = workflowDto.toDomain(workflowDto);
            when(workflowRepository.findAll()).thenReturn(List.of(workflow));
        var response = workflowService.getAll();
        assertEquals(1, response.size());
        assertEquals(workflowDto.getDomainId(), response.get(0).getDomainId());

            when(workflowRepository.findAll()).thenReturn(List.of());
        response = workflowService.getAll();
        assertEquals(0, response.size());
    }

    @Test
    public void updateStatusTest() {
        var workflow = getWorkflow();
            when(workflowRepository.findById(workflow.getId())).thenReturn(Optional.of(workflow));
            when(workflowRepository.save(any(Workflow.class))).thenReturn(workflow);
        var response = workflowService.updateStatus(1L, Workflow.Status.PR_CLOSED);
        assertEquals(response.getStatus(), Workflow.Status.PR_CLOSED);
    }

    private class TableAndFieldChanges {
        
        public static Table tableOld() {
        var fieldId = Field.builder().id(1L).name("id").dataType("int").description("primary key").isPrimitiveType(Boolean.TRUE).build();
        var fieldName = Field.builder().id(2L).name("name").dataType("string").description("Name of the product").isPrimitiveType(Boolean.TRUE).build();
        // var fieldPrice = Field.builder().id(3L).name("price").dataType("long").description("Price of the Product").build();
        List<Field> fields = new ArrayList<>(3);
        fields.add(fieldId); fields.add(fieldName); //fields.add(fieldPrice);
        return Table.builder().id(1L).name("product").nameSpace("com.acme").description("Product schema")
                    .owner("core").domain("E-commerce").fields(fields).build();
        }

        public static Table tableNew() {
            var fieldIdOld = Field.builder().id(1L).name("id").dataType("int").description("primary key").isPrimitiveType(Boolean.TRUE).build();
            var fieldIdNew = Field.builder().id(1L).name("id").dataType("int").description("Unique Identifier of the Product").isUserChanged(Boolean.TRUE).isPrimitiveType(Boolean.TRUE).build();
            var fieldName = Field.builder().id(2L).name("name").dataType("string").description("Name of the product").isUserChanged(Boolean.FALSE).isPrimitiveType(Boolean.TRUE).build();
            var fieldPrice = Field.builder().id(3L).name("price").dataType("long").description("Price of the Product").isUserChanged(Boolean.TRUE).build();
            List<Field> fields = new ArrayList<>(3);
            fields.add(fieldIdOld); fields.add(fieldIdNew); fields.add(fieldName); fields.add(fieldPrice);
            return Table.builder().id(1L).name("product").nameSpace("com.acme").description("Product schema")
                        .isUserChanged(Boolean.TRUE).owner("core").domain("E-commerce").fields(fields).build();
        }

        public static TableDto workflowTable() {
            var fieldId = new FieldDto(1L, "id", "int", "Unique Identifier of the Product", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
            var fieldName = new FieldDto(2L, "name", "string", "Name of the product", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
            var fieldPrice = new FieldDto(3L, "price", "string", "Price of the Product", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
            List<FieldDto> fieldDtos = new ArrayList<>(3);
            fieldDtos.add(fieldId); fieldDtos.add(fieldName); fieldDtos.add(fieldPrice);
            return new TableDto(1L, "com.acme", "product", null, "Product schema", "core", "core", null, null, null, null, "Active", fieldDtos, null, null, null);
        }
        
    }

    private class FieldMapping {

        public static Table tableOld() {
            var fieldId = Field.builder().id(1L).name("id").dataType("int").description("primary key").isPrimitiveType(Boolean.TRUE).build();
            var fieldName = Field.builder().id(2L).name("name").dataType("string").description("Name of the product").isPrimitiveType(Boolean.TRUE).build();
            var fieldFilterId = Field.builder().id(3L).name("filter_id").dataType("long").description("Primary Key of Filter Table").isPrimitiveType(Boolean.TRUE).build();
            List<Field> fields = new ArrayList<>();
            fields.add(fieldId); fields.add(fieldName); fields.add(fieldFilterId);
            return Table.builder().id(1L).name("product").nameSpace("com.acme").description("Product schema")
                        .owner("core").domain("E-commerce").jsonSchemaId("jsonSchemaId").fields(fields).build();
        }

        public static Table tableBrand() {
            var fieldId = Field.builder().id(1L).name("id").dataType("int").description("Unique Identifier of the Brand").isPrimitiveType(Boolean.TRUE).build();
            var fieldName = Field.builder().id(2L).name("name").dataType("string").description("Name of the Brand").isUserChanged(Boolean.FALSE).isPrimitiveType(Boolean.TRUE).build();
            List<Field> fields = new ArrayList<>();
            fields.add(fieldId); fields.add(fieldName);
            return Table.builder().id(1L).name("brand").nameSpace("com.acme").description("Brand schema")
                        .owner("core").domain("E-commerce").jsonSchemaId("jsonSchemaId").fields(fields).build();
        }

        public static Table tableNew() {
            var fieldId = Field.builder().id(1L).name("id").dataType("int").description("Unique Identifier of the Product").isPrimitiveType(Boolean.TRUE).build();
            var fieldName = Field.builder().id(2L).name("name").dataType("string").description("Name of the product").isUserChanged(Boolean.FALSE).isPrimitiveType(Boolean.TRUE).build();
            var fieldFilterId = Field.builder().id(3L).name("filter_id").dataType("long").description("Primary Key of Filter Table").isPrimitiveType(Boolean.TRUE)
                                     .referenceField(Field.builder().id(1L).name("id").dataType("long").build()).build();
            var fieldUserId = Field.builder().id(4L).name("user_id").dataType("long").description("Primary Key of User Table").isPrimitiveType(Boolean.TRUE)
                                    .referenceField(Field.builder().id(4L).name("id").dataType("long").build()).build();
            var fieldBrand = Field.builder().id(5L).name("brand").dataType("com.acme.brand").description("Brand Of the Product").isUserChanged(Boolean.TRUE)
                                  .isPrimitiveType(Boolean.FALSE).contain(tableBrand()).build();
            List<Field> fields = new ArrayList<>();
            fields.add(fieldId); fields.add(fieldName); fields.add(fieldFilterId); fields.add(fieldUserId); fields.add(fieldBrand);
            return Table.builder().id(1L).name("product").nameSpace("com.acme").description("Product schema")
                        .owner("core").domain("E-commerce").fields(fields).build();
        }

        public static TableDto workflowTable() {
            var fieldId = new FieldDto(1L, "id", "int", "Unique Identifier of the Product", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
            var fieldName = new FieldDto(2L, "name", "string", "Name of the product", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
            var fieldFilter = new FieldDto(3L, "filter_id", "long", "Primary Key of Filter Table", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 1L);
            var fieldPrice = new FieldDto(null, "user_id", "long", "Primary Key of User Table", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 4L);
            var fieldBrand = new FieldDto(null, "brand", "com.acme.brand", "Brand of the Product", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
            List<FieldDto> fieldDtos = new ArrayList<>();
            fieldDtos.add(fieldId); fieldDtos.add(fieldName); fieldDtos.add(fieldFilter); fieldDtos.add(fieldPrice); fieldDtos.add(fieldBrand);
            return new TableDto(1L, "com.acme", "product", null, "Product schema", "core", "E-commerce", null, null, null, null, "Active", fieldDtos, null, null, null);
        }
    }

    @Test
    public void saveAndRaisePrTest() {
        var table = TableAndFieldChanges.tableOld();
        var workflowDto = WorkflowDto.builder().table(TableAndFieldChanges.workflowTable()).build();
        var clientRepo = ClientRepo.builder().id(1L).name("name").fullName("fullName").connectId(1L).build();
            when(clientRepoService.getModel(anyLong())).thenReturn(clientRepo);
        var workflow = Workflow.builder().id(1L).status(Workflow.Status.NEW).title("changes-in-table-"+workflowDto.getTable().name())
                               .purpose("modifying schema "+workflowDto.getTable().name()).build();
            when(workflowRepository.save(any(Workflow.class))).thenReturn(workflow);
            TableService tableService = new TableService(tableRepository, fieldRepostory, responseMessage, null, null, null, null);
            ReflectionTestUtils.setField(workflowService, "tableService", tableService);
            when(tableRepository.findById(workflowDto.getTable().id())).thenReturn(Optional.of(table));
            when(schemaValidator.apply(any(Table.class))).thenReturn(new Result(null, null,  Status.SUCCESS, null));
            when(fieldValidator.apply(any(Field.class))).thenReturn(new Result(null, null,  Status.SUCCESS, null));
        var tableNew = TableAndFieldChanges.tableNew();
            when(tableRepository.save(any(Table.class))).thenReturn(tableNew);
        var pullRequest = PullRequest.builder().id(1L).workflowId(workflow.getId()).build();
            when(gitHubService.commitAndRaisePr(anyMap(), any(Workflow.class))).thenReturn(pullRequest);
        var response = new ObjectMapper().convertValue(workflowService.saveAndRaisePr(workflowDto, clientRepo.getId()), WorkflowDto.class);
        assertEquals(response.getTitle(), workflow.getTitle());

        mockApplicationUser();
        table = FieldMapping.tableOld();
        workflowDto = WorkflowDto.builder().table(FieldMapping.workflowTable()).build();
            when(tableRepository.findById(workflowDto.getTable().id())).thenReturn(Optional.of(table));
            when(fieldRepostory.findById(1L)).thenReturn(Optional.of(Field.builder().id(1L).name("id").dataType("long").build()));
            when(fieldRepostory.findById(4L)).thenReturn(Optional.of(Field.builder().id(4L).name("id").dataType("long").build()));
            when(tableRepository.findByNameAndNameSpaceAndClientId(anyString(), anyString(), anyLong())).thenReturn(FieldMapping.tableBrand());
        tableNew = FieldMapping.tableNew();
            when(tableRepository.save(any(Table.class))).thenReturn(tableNew);
        response = new ObjectMapper().convertValue(workflowService.saveAndRaisePr(workflowDto, clientRepo.getId()), WorkflowDto.class);
        assertEquals(response.getTitle(), workflow.getTitle());
    }

    private class NewTableCreation {
    
        public static Table tableNew() {
            var fieldIdOld = Field.builder().id(1L).name("id").dataType("int").description("primary key").isPrimitiveType(Boolean.TRUE).build();
            var fieldName = Field.builder().id(2L).name("name").dataType("string").description("Name of the product").isUserChanged(Boolean.FALSE).isPrimitiveType(Boolean.TRUE).build();
            var fieldPrice = Field.builder().id(3L).name("price").dataType("long").description("Price of the Product").isUserChanged(Boolean.TRUE).build();
            List<Field> fields = new ArrayList<>(3);
            fields.add(fieldIdOld); fields.add(fieldName); fields.add(fieldPrice);
            return Table.builder().id(1L).name("product").nameSpace("com.acme").description("Product schema")
                        .isUserChanged(Boolean.TRUE).owner("core").domain("E-commerce").fields(fields).build();
        }

        public static TableDto workflowTable() {
            var fieldId = new FieldDto(null, "id", "int", "Unique Identifier of the Product", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
            var fieldName = new FieldDto(null, "name", "string", "Name of the product", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
            var fieldPrice = new FieldDto(null, "price", "string", "Price of the Product", Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
            List<FieldDto> fieldDtos = new ArrayList<>(3);
            fieldDtos.add(fieldId); fieldDtos.add(fieldName); fieldDtos.add(fieldPrice);
            return new TableDto(null, "com.acme", "product", null, "Product schema", "core", "E-commerce", null, null, null, null, "Active", fieldDtos, null, null, null);
        }
    }

    @Test
    public void saveAndRaisePrNewTableTest() {
        var table = NewTableCreation.tableNew();
        var workflowDto = WorkflowDto.builder().table(NewTableCreation.workflowTable()).build();
        var clientRepo = ClientRepo.builder().id(1L).name("name").fullName("fullName").repoType(RepoType.PROTOBUF).connectId(1L).build();
            when(clientRepoService.getModel(anyLong())).thenReturn(clientRepo);
        var workflow = Workflow.builder().id(1L).status(Workflow.Status.NEW).title("changes-in-table-"+workflowDto.getTable().name())
                               .purpose("modifying schema "+workflowDto.getTable().name()).build();
            when(workflowRepository.save(any(Workflow.class))).thenReturn(workflow);
            when(schemaValidator.apply(any(Table.class))).thenReturn(new Result(null, null,  Status.SUCCESS, null));
            when(fieldValidator.apply(any(Field.class))).thenReturn(new Result(null, null,  Status.SUCCESS, null));
            TableService tableService = new TableService(tableRepository, fieldRepostory, responseMessage, null, null, null, null);
            ReflectionTestUtils.setField(workflowService, "tableService", tableService);
            when(tableRepository.save(any(Table.class))).thenReturn(table);
        var schemaFileAudit = SchemaFileAudit.builder().id(1L).clientRepoId(clientRepo.getId()).build();
            when(schemaFileAuditService.createSchemaFileAuditForNewFile(any(ClientRepo.class), any(Table.class), anyString())).thenReturn(schemaFileAudit);
        var model = Model.builder().id(1L).schemaFileAuditId(schemaFileAudit.getId()).name(table.getName()).nameSpace(table.getNameSpace()).nodeId(table.getId()).build();
            when(modelService.createModel(any(SchemaFileAudit.class), anyLong(), any(Table.class))).thenReturn(model);
        var pullRequest = PullRequest.builder().id(1L).workflowId(workflow.getId()).build();
            when(gitHubService.commitAndRaisePr(anyMap(), any(Workflow.class))).thenReturn(pullRequest);
        var response = new ObjectMapper().convertValue(workflowService.saveAndRaisePr(workflowDto, clientRepo.getId()), WorkflowDto.class);
        assertEquals(response.getTitle(), workflow.getTitle());
        
        clientRepo = ClientRepo.builder().id(1L).name("name").fullName("fullName").repoType(RepoType.JSON).connectId(1L).build();
            when(clientRepoService.getModel(anyLong())).thenReturn(clientRepo);
        response = new ObjectMapper().convertValue(workflowService.saveAndRaisePr(workflowDto, clientRepo.getId()), WorkflowDto.class);
        assertEquals(response.getTitle(), workflow.getTitle());
    }

    private void fieldNameChangedTest() {
        var fieldIdOld = Field.builder().id(1L).name("id").dataType("int").build();
        var tableOld = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(fieldIdOld)).build();
            when(tableRepository.findById(anyLong())).thenReturn(Optional.of(tableOld));
        var fieldDto = new FieldDto(1L, "ID", "int", null, null, null, null, null);
        var tableDto = new TableDto(1L, "com.acme", "product", null, null, null, null, null, null, null, null, null, List.of(fieldDto), null, null, null);
        var workflowDto1 = WorkflowDto.builder().table(tableDto).build();
        assertThrows(BadRequestException.class, () -> workflowService.saveAndRaisePr(workflowDto1, 1L));
    }

    private void fieldDatatypeChangedTest() {
        var fieldIdOld = Field.builder().id(1L).name("id").dataType("int").build();
        var tableOld = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(fieldIdOld)).build();
            when(tableRepository.findById(anyLong())).thenReturn(Optional.of(tableOld));
        var fieldDto = new FieldDto(1L, "id", "long", null, null, null, null, null);
        var tableDto = new TableDto(1L, "com.acme", "product", null, null, null, null, null, null, null, null, null, List.of(fieldDto), null, null, null);
        var workflowDto = WorkflowDto.builder().table(tableDto).build();
        assertThrows(BadRequestException.class, () -> workflowService.saveAndRaisePr(workflowDto, 1L));
    }

    private void newFieldWithNameAlreayExistsTest() {
        var fieldIdOld = Field.builder().id(1L).name("id").dataType("int").build();
        var tableOld = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(fieldIdOld)).build();
            when(tableRepository.findById(anyLong())).thenReturn(Optional.of(tableOld));
        var fieldDto = new FieldDto(1L, "id", "int", null, null, null, null, null);
        var fieldDto2 = new FieldDto(null, "id", "long", null, null, null, null, null);
        var tableDto = new TableDto(1L, "com.acme", "product", null, null, null, null, null, null, null, null, null, List.of(fieldDto, fieldDto2), null, null, null);
        var workflowDto = WorkflowDto.builder().table(tableDto).build();
            when(fieldValidator.apply(any(Field.class))).thenReturn(new Result(null, null,  Status.SUCCESS, null));
        assertThrows(AlreadyExistException.class, () -> workflowService.saveAndRaisePr(workflowDto, 1L));
    }

    private void fieldMetadataEmptyTest() {
        var fieldIdOld = Field.builder().id(1L).name("id").dataType("int").build();
        var tableOld = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(fieldIdOld)).build();
            when(tableRepository.findById(anyLong())).thenReturn(Optional.of(tableOld));
        var fieldDto = new FieldDto(1L, "id", "int", null, null, null, null, null);
        var tableDto = new TableDto(1L, "com.acme", "product", null, null, null, null, null, null, null, null, null, List.of(fieldDto), null, null, null);
        var workflowDto = WorkflowDto.builder().table(tableDto).build();
            when(fieldValidator.apply(any(Field.class))).thenReturn(new Result(null, null,  Status.ERROR, List.of("Empty desc")));
        assertThrows(BadRequestException.class, () -> workflowService.saveAndRaisePr(workflowDto, 1L));
    }

    private void tableNameSpaceChangedTest() {
        var fieldIdOld = Field.builder().id(1L).name("id").dataType("int").build();
        var tableOld = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(fieldIdOld)).build();
            when(tableRepository.findById(anyLong())).thenReturn(Optional.of(tableOld));
        var fieldDto = new FieldDto(1L, "id", "int", null, null, null, null, null);
        var tableDto = new TableDto(1L, "com.xcme", "product", null, null, null, null, null, null, null, null, null, List.of(fieldDto), null, null, null);
        var workflowDto = WorkflowDto.builder().table(tableDto).build();
        assertThrows(BadRequestException.class, () -> workflowService.saveAndRaisePr(workflowDto, 1L));
    }

    private void tableNameChangedTest() {
        var fieldIdOld = Field.builder().id(1L).name("id").dataType("int").build();
        var tableOld = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(fieldIdOld)).build();
            when(tableRepository.findById(anyLong())).thenReturn(Optional.of(tableOld));
        var fieldDto = new FieldDto(1L, "id", "int", null, null, null, null, null);
        var tableDto = new TableDto(1L, "com.acme", "products", null, null, null, null, null, null, null, null, null, List.of(fieldDto), null, null, null);
        var workflowDto = WorkflowDto.builder().table(tableDto).build();
        assertThrows(BadRequestException.class, () -> workflowService.saveAndRaisePr(workflowDto, 1L));
    }

    private void tableAlreadyChangedTest() {
        var fieldIdOld = Field.builder().id(1L).name("id").dataType("int").build();
        var tableOld = Table.builder().id(1L).name("product").nameSpace("com.acme").isUserChanged(Boolean.TRUE).fields(List.of(fieldIdOld)).build();
            when(tableRepository.findById(anyLong())).thenReturn(Optional.of(tableOld));
        var fieldDto = new FieldDto(1L, "id", "int", null, null, null, null, null);
        var tableDto = new TableDto(1L, "com.acme", "product", null, null, null, null, null, null, null, null, null, List.of(fieldDto), null, null, null);
        var workflowDto = WorkflowDto.builder().table(tableDto).build();
        assertThrows(BadRequestException.class, () -> workflowService.saveAndRaisePr(workflowDto, 1L));
    }

    private void fieldAlreadyChangedTest() {
        var fieldIdOld = Field.builder().id(1L).name("id").dataType("int").isUserChanged(Boolean.TRUE).build();
        var tableOld = Table.builder().id(1L).name("product").nameSpace("com.acme").fields(List.of(fieldIdOld)).build();
            when(tableRepository.findById(anyLong())).thenReturn(Optional.of(tableOld));
        var fieldDto = new FieldDto(1L, "id", "int", null, null, null, null, null);
        var tableDto = new TableDto(1L, "com.acme", "product", null, null, null, null, null, null, null, null, null, List.of(fieldDto), null, null, null);
        var workflowDto = WorkflowDto.builder().table(tableDto).build();
        assertThrows(BadRequestException.class, () -> workflowService.saveAndRaisePr(workflowDto, 1L));
    }

    private void newTableAlreadyExistsTest() {
            when(modelService.findModelByNameAndNameSpace("com.acme", "product")).thenReturn(List.of(Model.builder().build()));
        var fieldDto = new FieldDto(null, "id", "int", null, null, null, null, null);
        var tableDto = new TableDto(null, "com.acme", "product", null, null, null, null, null, null, null, null, null, List.of(fieldDto), null, null, null);
        var workflowDto = WorkflowDto.builder().table(tableDto).build();
        assertThrows(AlreadyExistException.class, () -> workflowService.saveAndRaisePr(workflowDto, 1L));
    }

    private void tableMetadataEmptyTest() {
            when(schemaValidator.apply(any(Table.class))).thenReturn(new Result(null, null,  Status.ERROR, List.of("Empty desc")));
        var fieldDto = new FieldDto(null, "id", "int", null, null, null, null, null);
        var tableDto = new TableDto(null, "com.acme", "product", null, null, null, null, null, null, null, null, null, List.of(fieldDto), null, null, null);
        var workflowDto = WorkflowDto.builder().table(tableDto).build();
        assertThrows(BadRequestException.class, () -> workflowService.saveAndRaisePr(workflowDto, 1L));
    }

    @Test
    public void saveAndRaisePrFailTest() {
        var clientRepo = ClientRepo.builder().id(1L).name("name").fullName("fullName").repoType(RepoType.PROTOBUF).connectId(1L).build();
            when(clientRepoService.getModel(anyLong())).thenReturn(clientRepo);            
            when(workflowRepository.save(any(Workflow.class))).thenReturn(Workflow.builder().id(1L).build());
        TableService tableService = new TableService(tableRepository, fieldRepostory, responseMessage, null, null, null, null);
            ReflectionTestUtils.setField(workflowService, "tableService", tableService);
            when(schemaValidator.apply(any(Table.class))).thenReturn(new Result(null, null,  Status.SUCCESS, null));
        
        fieldNameChangedTest();
        fieldDatatypeChangedTest();
        newFieldWithNameAlreayExistsTest();
        fieldMetadataEmptyTest();
        tableNameChangedTest();
        tableNameSpaceChangedTest();
        tableAlreadyChangedTest();
        fieldAlreadyChangedTest();
        tableMetadataEmptyTest();
        newTableAlreadyExistsTest();
    }
}
