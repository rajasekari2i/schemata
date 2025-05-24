package com.opsbeach.connect.schemata.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.protobuf.Descriptors;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.processor.protobuf.ProtoFileDescriptorSetLoader;
import com.opsbeach.connect.schemata.processor.protobuf.ProtoProcessor;
import com.opsbeach.sharedlib.exception.SchemaNotFoundException;

public class SchemaGraphTest {
   
    static SchemaGraph graph;

    @BeforeAll
    static void setUp()
        throws IOException, Descriptors.DescriptorValidationException {
        var stream = new FileInputStream(new File("src/test/resources/schema_1/descriptors/entities.desc"));
        var protoFileDescriptorLoader = new ProtoFileDescriptorSetLoader(stream);
        var parser = new ProtoProcessor();
        var schemaList = parser.parse(protoFileDescriptorLoader.loadDescriptors(), new HashMap<>());
        graph = new SchemaGraph(schemaList);
    }

    @Test
    public void testWithInvalidSchema() {
        Assertions.assertThrows(SchemaNotFoundException.class, () -> graph.getSchema("User"),
            "Schema not found was expected");
    }

    @Test
    public void testWithValidSchema() {
        var response = graph.getSchema("org.entities.College");
        assertEquals("College", response.getName());
    }

    @Test
    public void getSchemataScoreTest() {
        var response = graph.getSchemataScore("org.entities.Person");
        System.out.println(response);
        assertEquals(0, response);
        response = graph.getSchemataScore("org.entities.Department");
        System.out.println(response);
        assertEquals(0.4, response);
        response = graph.getSchemataScore("org.entities.College");
        System.out.println(response);
        assertEquals(0, response);
        response = graph.getSchemataScore("org.entities.City");
        System.out.println(response);
        assertEquals(0, response);
        response = graph.getSchemataScore("org.entities.Country");
        System.out.println(response);
        assertEquals(0, response);
    }

    @Test
    public void getVertexPageRankScoreTest() {
        assertEquals(0.3114570188661495, graph.getVertexPageRankScore("org.entities.Country"));
    }

    @Test
    public void getAllEntityVertexTest() {
        var response = graph.getAllEntityVertex();
        assertEquals("Department", response.iterator().next().getName());
    }

    @Test
    public void incomingVertexOfTest() {
        var incomingVertex = graph.incomingVertexOf("org.entities.Department");
        var expectedVertex = Set.of("Person");
        var actualVertex = incomingVertex.stream().map(Table::getName).collect(Collectors.toSet());
        assertEquals(1, incomingVertex.size());
        assertTrue(SetUtils.isEqualSet(actualVertex, expectedVertex));
    }

    @Test
    public void outgoingVertexOfTest() {
        var outgoingVertex = graph.outgoingEntityVertexOf("org.entities.Person");
        var expectedVertex = Set.of("Department");
        var actualVertex = outgoingVertex.stream().map(Table::getName).collect(Collectors.toSet());
        assertEquals(1, outgoingVertex.size());
        assertTrue(SetUtils.isEqualSet(actualVertex, expectedVertex));
    }

    @Test
    public void buildEdgeFail() {
        var fields = List.of(
            Field.builder().id(1L).name("name").dataType("string").isPrimitiveType(Boolean.TRUE).isDeleted(Boolean.FALSE).build(),
            Field.builder().id(2L).name("Filter").dataType("com.acme.filter").isPrimitiveType(Boolean.FALSE).isDeleted(Boolean.TRUE).build(),
            Field.builder().id(2L).name("Brand").dataType("com.acme.brand").isPrimitiveType(Boolean.FALSE).isDeleted(Boolean.FALSE).build()
        );
        var table = Table.builder().name("product").nameSpace("com.acme").fields(fields).build();
        assertThrows(SchemaNotFoundException.class, () -> new SchemaGraph(List.of(table)));
    }
}
