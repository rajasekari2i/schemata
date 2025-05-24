package com.opsbeach.connect.schemata.processor.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.apache.avro.SchemaParseException;
import org.hibernate.tool.schema.extract.spi.SchemaExtractionException;
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
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.response.ResponseMessage;

public class JsonSchemaTest {
    
    @InjectMocks
    private JsonSchema jsonSchema;

    @Mock
    private TableService tableService;

    @Mock
    private DomainNodeService domainNodeService;

    @Mock
    private DomainService domainService;

    @Mock
    private ModelService modelService;

    @Mock
    private ResponseMessage responseMessage;

    @Mock
    private SchemaFileAuditService schemaFileAuditService;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    } 

    private Table getTable() {
        final var JSON_SCHEMA_ID = "https://example.com/product/schema.json";
        var primitiveField = Field.builder().id(1L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).isPii(null).isClassified(null).deprecated(null).build();
        var arrayField = Field.builder().id(2L).name("arrayfield").dataType("array").items("array").arrayField(Field.builder().id(3L).name("arrayField").dataType("array").items("string").build()).isPrimitiveType(Boolean.FALSE).build();
        var unionField = Field.builder().id(6L).name("unionfield").dataType("union").unionTypes(List.of(Field.builder().id(7L).dataType("null").isPrimitiveType(Boolean.TRUE).build(), Field.builder().id(8L).dataType("int").isPrimitiveType(Boolean.TRUE).build())).isPrimitiveType(Boolean.FALSE).build();
        String[] requiredFields = {"Marks"};
        var tableField = Field.builder().id(9L).name("tablefiled").dataType("Marks").contain(Table.builder().id(10L).name("Marks").schemaType(null).jsonSchemaId(JSON_SCHEMA_ID).fields(List.of(Field.builder().id(11L).name("Marks").dataType("string").isPrimitiveType(Boolean.TRUE).build())).requiredFields(requiredFields).build()).isPrimitiveType(Boolean.FALSE).build();
        String[] enums = {"active","deactive"};
        var enumField = Field.builder().id(12L).name("enumField").dataType("enum").symbols(enums).build();
        var arrayOfUnion = Field.builder().id(13L).name("ArrayOfUnion").dataType("array").items("union").unionTypes(unionField.getUnionTypes()).build();
        var arrayOfEnum = Field.builder().id(14L).name("ArrayOfEnum").dataType("array").items("enum").symbols(new String[0]).build();
        var arrayOfObject = Field.builder().id(15L).name("ArrayOfObject").dataType("array").items("table").contain(tableField.getContain()).build();
        var enumField2 = Field.builder().id(17L).name("enumField2").dataType("enum").defaultValue("active").symbols(enums).build();
        var intField = Field.builder().id(25L).name("intField").dataType("int").defaultValue("0").isPrimitiveType(Boolean.TRUE).build();
        var floatField = Field.builder().id(25L).name("floatField").dataType("float").defaultValue("0.0").isPrimitiveType(Boolean.TRUE).build();
        var longField = Field.builder().id(25L).name("longField").dataType("long").defaultValue("0").isPrimitiveType(Boolean.TRUE).build();
        var doubleField = Field.builder().id(25L).name("doubleField").dataType("double").defaultValue("0.0").isPrimitiveType(Boolean.TRUE).build();
        var unionOfUnion = Field.builder().id(26L).name("unionOfUnion").dataType("union").isPrimitiveType(Boolean.FALSE).unionTypes(List.of(primitiveField)).build();

        var fields = List.of(primitiveField, arrayField, unionField, tableField, enumField, arrayOfUnion, arrayOfEnum, arrayOfObject,
                             enumField2, intField, floatField, longField, doubleField, unionOfUnion);
        return Table.builder().id(12L).name("student").schemaType(SchemaType.ENTITY).fields(fields).complianceOwner("compliance owner")
                    .owner("owner").domain("domain").channel("channel").email("email").status("active")
                    .qualityRuleBase("base rule").subscribers(enums).jsonSchemaId(JSON_SCHEMA_ID).build();
    }

    final String FILE_PATH1 = "src/test/resources/schema_1/json/productListFiltered.json";
    final String FILE_PATH2 = "src/test/resources/schema_1/json/product.json";

    @Test
    public void buildSchemaTest() throws IOException {
        when(tableService.addTable(any(Table.class))).thenReturn(Table.builder().id(1L).name("table").build());
        when(tableService.addField(any(Field.class))).thenReturn(Field.builder().id(1L).name("product").build());
        var response = jsonSchema.getTables(FILE_PATH1, Boolean.FALSE);
        assertEquals(3, response.size());
        assertNull(response.get(0).getId());
        response = jsonSchema.getTables(FILE_PATH2, Boolean.FALSE);
        assertEquals("brand", response.get(0).getName());
        assertEquals(3, response.size());
        assertNull(response.get(0).getId());
    }

    @Test
    public void getFileContentTest() throws IOException {
        var table = getTable();
        var response = jsonSchema.getFileContent(table);
        System.out.println(response);

        var filterTable = Table.builder().id(2L).name("filter").nameSpace("schemas.browsing.product_list_filtered")
                            .fields(List.of(Field.builder().id(1L).name("id").dataType("interger").build())).build();
        var field = Field.builder().id(1L).name("filter").dataType("schemas.browsing.product_list_filtered.filter")
                    .jsonSchemaRefId("#/definitions/filter").contain(filterTable).build();
        var field2 = Field.builder().id(2L).name("brand").dataType("schemas.product").jsonSchemaRefId("/schemas/product").contain(filterTable).build();
        var arrayField = Field.builder().id(1L).name("filters").dataType("array").items("schemas.browsing.product_list_filtered.filter")
                        .contain(filterTable).jsonSchemaRefId("#/definitions/filter").build();
        var arrayField2 = Field.builder().id(2L).name("brands").dataType("array").items("schemas.product").jsonSchemaRefId("/schemas/product").contain(filterTable).build();
        table.setFields(List.of(field, field2, arrayField, arrayField2));
        response = jsonSchema.getFileContent(table);
        System.out.println(response);
    }

    @Test
    public void getTablesByContentTest() throws IOException {
        var content = jsonSchema.getFileContent(getTable()).getBytes();
        var response = jsonSchema.getTables(content, Boolean.FALSE);
        assertEquals(2, response.size());
        assertNull(response.get(response.size() - 1).getId());

        assertThrows(SchemaExtractionException.class, () -> jsonSchema.getTables("{ \"type\": \"object\" }".getBytes(), Boolean.FALSE));
        assertThrows(SchemaExtractionException.class, () -> jsonSchema.getTables("{ \"$schema\": \"http://json-schema.org/draft-07/schema#\" }".getBytes(), Boolean.FALSE));
        assertThrows(SchemaExtractionException.class, () -> jsonSchema.getTables("{ \"$schema\": \"http://json-schema.org/draft/schema#\" }".getBytes(), Boolean.FALSE));
        assertThrows(InvalidDataException.class, () -> jsonSchema.getTables("{ \"$schema\": \"http://json-schema.org/draft-07/schema#\", \"$id\": \"invalid id\" }".getBytes(), Boolean.FALSE));
    }

    @Test
    public void parseFolderTest() {
        var folderPath = "src/test/resources/schema_1";
        var clientRepo = ClientRepo.builder().id(2L).clientId(1L).defaultBranch("branch").fullName("fullName").build();
            when(domainNodeService.addDomainNode(anyString(), anyLong(), anyLong())).thenReturn(DomainNode.builder().id(2L).build());
            when(domainService.addModel(any(Domain.class))).thenReturn(Domain.builder().id(1L).build());
            when(tableService.addTable(any(Table.class))).thenReturn(Table.builder().id(1L).name("table").build());
            when(tableService.addField(any(Field.class))).thenReturn(Field.builder().id(1L).name("product").build());
        jsonSchema.parseFolder(folderPath, clientRepo);

            when(tableService.addTable(any(Table.class))).thenThrow(SchemaParseException.class);
        assertThrows(SchemaParseException.class, () -> jsonSchema.parseFolder(folderPath, clientRepo));
    }
}
