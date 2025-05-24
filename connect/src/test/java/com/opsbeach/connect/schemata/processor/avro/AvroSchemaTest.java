package com.opsbeach.connect.schemata.processor.avro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Domain;
import com.opsbeach.connect.github.service.DomainService;
import com.opsbeach.connect.github.service.ModelService;
import com.opsbeach.connect.github.service.SchemaFileAuditService;
import com.opsbeach.connect.schemata.entity.DomainNode;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.SchemaType;
import com.opsbeach.connect.schemata.service.DomainNodeService;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.exception.FileNotFoundException;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

public class AvroSchemaTest {
    
    @InjectMocks
    private AvroSchema avroSchema;

    @Mock
    private TableService tableService;

    @Mock
    private ResponseMessage responseMessage;

    @Mock
    private DomainNodeService domainNodeService;

    @Mock
    private DomainService domainService;

    @Mock
    private ModelService modelService;

    @Mock
    private SchemaFileAuditService schemaFileAuditService;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private final String FILE_PATH = "src/test/resources/schema_1/avro/avro_testing.avsc";

    private Table getTable() {
        var primitiveField = Field.builder().id(1L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).isPii(null).isClassified(null).deprecated(null).build();
        var arrayField = Field.builder().id(2L).name("arrayfield").dataType("array").items("array").arrayField(Field.builder().id(3L).name("arrayField").dataType("array").items("string").build()).isPrimitiveType(Boolean.FALSE).build();
        var mapField = Field.builder().id(4L).name("mapFiled").dataType("map").values("map").mapField(Field.builder().id(5L).name("mapField").dataType("map").values("string").build()).isPrimitiveType(Boolean.FALSE).build();
        var unionField = Field.builder().id(6L).name("unionfield").dataType("union").unionTypes(List.of(Field.builder().id(7L).dataType("null").isPrimitiveType(Boolean.TRUE).build(), Field.builder().id(8L).dataType("int").isPrimitiveType(Boolean.TRUE).build())).isPrimitiveType(Boolean.FALSE).build();
        var tableField = Field.builder().id(9L).name("tablefiled").dataType("Marks").contain(Table.builder().id(10L).name("Marks").schemaType(null).fields(List.of(Field.builder().id(11L).name("Marks").dataType("string").isPrimitiveType(Boolean.TRUE).build())).build()).isPrimitiveType(Boolean.FALSE).build();
        String[] enums = {"active","deactive"};
        var enumField = Field.builder().id(12L).name("enumField").dataType("enum").symbols(enums).build();
        var arrayOfUnion = Field.builder().id(13L).name("ArrayOfUnion").dataType("array").items("union").unionTypes(unionField.getUnionTypes()).build();
        var arrayOfEnum = Field.builder().id(14L).name("ArrayOfEnum").dataType("array").items("enum").symbols(enums).build();
        var arrayOfObject = Field.builder().id(15L).name("ArrayOfObject").dataType("array").items("table").contain(tableField.getContain()).build();
        var fixedField = Field.builder().id(16L).name("fixedField").dataType("fixed").size(10).build();
        var enumField2 = Field.builder().id(17L).name("enumField2").dataType("enum").defaultValue("active").symbols(enums).build();
        var arrayOfMap = Field.builder().id(18L).name("arrayOfMap").dataType("array").items("map").mapField(Field.builder().id(19L).dataType("map").values("string").build()).build();
        var arrayOfFixed = Field.builder().id(19L).name("arrayOfFixed").dataType("array").items("fixed").size(10).build();
        var mapOfArray = Field.builder().id(20L).name("mapOfArray").dataType("map").values("array").arrayField(arrayField).build();
        var mapOfUnion = Field.builder().id(21L).name("mapOfUnion").dataType("map").values("union").unionTypes(unionField.getUnionTypes()).build();
        var mapOfEnum = Field.builder().id(22L).name("mapOfEnum").dataType("map").values("enum").symbols(enums).build();
        var mapOfObject = Field.builder().id(23L).name("mapOfObject").dataType("map").values("table").contain(tableField.getContain()).build();
        var mapOfFixed = Field.builder().id(24L).name("mapOfFixed").dataType("map").values("fixed").size(10).build();
        var intField = Field.builder().id(25L).name("intField").dataType("int").defaultValue("0").isPrimitiveType(Boolean.TRUE).build();
        var floatField = Field.builder().id(25L).name("floatField").dataType("float").defaultValue("0.0").isPrimitiveType(Boolean.TRUE).build();
        var longField = Field.builder().id(25L).name("longField").dataType("long").defaultValue("0").isPrimitiveType(Boolean.TRUE).build();
        var doubleField = Field.builder().id(25L).name("doubleField").dataType("double").defaultValue("0.0").isPrimitiveType(Boolean.TRUE).build();
        var unionOfUnion = Field.builder().id(26L).name("unionOfUnion").dataType("union").isPrimitiveType(Boolean.FALSE).unionTypes(List.of(fixedField)).build();

        var fields = List.of(primitiveField, arrayField, unionField, mapField, tableField, enumField, arrayOfUnion, arrayOfEnum, arrayOfObject, fixedField, enumField2,
                             arrayOfMap, arrayOfFixed, mapOfArray, mapOfUnion, mapOfEnum, mapOfObject, mapOfFixed, intField, floatField, longField, doubleField, unionOfUnion);
        return Table.builder().id(12L).name("student").schemaType(SchemaType.ENTITY).fields(fields).complianceOwner("compliance owner")
                    .owner("owner").domain("domain").channel("channel").email("email").status("active")
                    .qualityRuleBase("base rule").subscribers(enums).build();
    }

    @Test
    public void getTablesByPathTest() throws IOException {
        when(tableService.addTable(any(Table.class))).thenReturn(Table.builder().id(1L).build());
        when(tableService.findByNameAndNameSpace("mailing_address", "eu.driver.model.sim.support")).thenReturn(Table.builder().id(2L).build());
        var response = avroSchema.getTables(FILE_PATH, Boolean.TRUE);
        assertEquals(1L, response.get(response.size() - 1).getId());

        response = avroSchema.getTables(FILE_PATH, Boolean.FALSE);
        assertNull(response.get(response.size() - 1).getId());

        assertThrows(InvalidDataException.class, () -> { avroSchema.getTables("/avro/avro_testing.avcc", Boolean.TRUE); });
    }
    
    @Test
    public void getFileContentTest() {
        var response = avroSchema.getFileContent(getTable());
        System.out.println(response);
    }

    @Test
    public void parseFolderTest() {
        var folderPath = "src/test/resources/schema_1";
        var clientRepo = ClientRepo.builder().id(2L).clientId(1L).defaultBranch("branch").fullName("fullName").build();
            when(domainNodeService.addDomainNode(anyString(), anyLong(), anyLong())).thenReturn(DomainNode.builder().id(2L).build());
            when(domainService.addModel(any(Domain.class))).thenReturn(Domain.builder().id(1L).build());
            when(tableService.addTable(any(Table.class))).thenReturn(Table.builder().id(1L).build());
            when(tableService.findByNameAndNameSpace("mailing_address", "eu.driver.model.sim.support")).thenReturn(Table.builder().id(2L).build());
        avroSchema.parseFolder(folderPath, clientRepo);

            when(tableService.findByNameAndNameSpace("mailing_address", "eu.driver.model.sim.support")).thenThrow(RecordNotFoundException.class);
        assertThrows(FileNotFoundException.class, () -> avroSchema.parseFolder(folderPath, clientRepo));
    }

    @Test
    public void gettablesByContentTest() throws IOException {
        var content = avroSchema.getFileContent(getTable()).getBytes();
            when(tableService.addTable(any(Table.class))).thenReturn(Table.builder().id(1L).build());
            when(tableService.findByNameAndNameSpace("mailing_address", "eu.driver.model.sim.support")).thenReturn(Table.builder().id(2L).build());
        var response = avroSchema.getTables(content, Boolean.FALSE);
        assertEquals(2, response.size());
        assertNull(response.get(response.size() - 1).getId());
    }
}

