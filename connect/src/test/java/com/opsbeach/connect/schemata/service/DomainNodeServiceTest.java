package com.opsbeach.connect.schemata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.schemata.dto.SchemaVisualizerDto;
import com.opsbeach.connect.schemata.entity.DomainNode;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Organization;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.SchemaType;
import com.opsbeach.connect.schemata.repository.DomainNodeRepository;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

public class DomainNodeServiceTest {

    @InjectMocks
    private DomainNodeService domainNodeService;

    @Mock
    private DomainNodeRepository domainNodeRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private TableService tableService;

    @Mock
    private ResponseMessage responseMessage;
    
    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private Table getTable() {
        var primitiveField = Field.builder().id(1L).name("score").dataType("int").isPrimitiveType(Boolean.TRUE).build();
        var arrayField = Field.builder().id(2L).name("array field").dataType("array").items("array").arrayField(Field.builder().id(3L).name("array field").dataType("array").items("string").build()).isPrimitiveType(Boolean.FALSE).build();
        var mapField = Field.builder().id(4L).name("map filed").dataType("map").values("map").mapField(Field.builder().id(5L).name("map field").dataType("map").values("string").build()).isPrimitiveType(Boolean.FALSE).build();
        var unionField = Field.builder().id(6L).name("union field").dataType("union").unionTypes(List.of(Field.builder().id(7L).dataType(null).build(), Field.builder().id(8L).dataType("int").build())).isPrimitiveType(Boolean.FALSE).build();
        var tableField = Field.builder().id(9L).name("table filed").dataType("Marks").contain(Table.builder().id(10L).name("Marks").schemaType(SchemaType.ENTITY).fields(List.of(Field.builder().id(11L).dataType("string").build())).build()).isPrimitiveType(Boolean.FALSE).build();
        var fields = List.of(primitiveField, arrayField, unionField, mapField, tableField);
        return Table.builder().id(12L).name("student").schemaType(SchemaType.ENTITY).fields(fields).build();
    }

    private DomainNode getDomain() {
        return DomainNode.builder().id(1L).name("analytics").tables(List.of(getTable())).build();
    }

    @Test
    public void addTest() {
        var domain = getDomain();
        var organization = Organization.builder().id(2L).name("OpsBeach").domains(List.of()).build();
            when(organizationService.get(anyLong())).thenReturn(organization);
            when(organizationService.update(any(Organization.class))).thenReturn(organization);
        var response = domainNodeService.add(domain, organization.getId());
        assertEquals(response.getName(), domain.getName());
        organization.setDomains(List.of(domain));
            when(organizationService.get(anyLong())).thenReturn(organization);
        response = domainNodeService.add(domain, organization.getId());
        assertEquals(response.getName(), domain.getName());
    }

    @Test
    public void getTestFail() {
        var domain = getDomain();
            when(domainNodeRepository.findById(1L)).thenReturn(Optional.of(domain));
        var response = domainNodeService.get(1L);
        assertEquals(response.getId(), domain.getId());
        assertThrows(RecordNotFoundException.class, () -> { domainNodeService.get(2L); });
    }

    @Test
    public void updateTest() {
        var domain = getDomain();
            when(domainNodeRepository.findById(anyLong())).thenReturn(Optional.of(domain));
            when(domainNodeRepository.save(domain)).thenReturn(domain);
        var response = domainNodeService.update(domain);
        assertEquals(response.getName(), domain.getName());
            
    }

    @Test
    public void getAllTest() {
        var organization = Organization.builder().domains(List.of()).build();
            when(organizationService.get(anyLong())).thenReturn(organization);
        var response = domainNodeService.getAll(1L);
        assertEquals(0, response.size());
    }

    @Test
    public void getTablesByDominTest() {
        var domain = getDomain();
            when(domainNodeRepository.findById(anyLong())).thenReturn(Optional.of(domain));
        var response = domainNodeService.getTablesByDominId(1L);
        assertEquals(domain.getTables().get(0).getName(), response.get(0).getName());
        domain.setTables(List.of());
            when(domainNodeRepository.findById(anyLong())).thenReturn(Optional.of(domain));
        response = domainNodeService.getTablesByDominId(1L);
        assertEquals(0, response.size());
    }

    @Test
    public void getSchemaVisualizerByDomainTest() {
        var domainNode = getDomain();
            when(domainNodeRepository.findById(anyLong())).thenReturn(Optional.of(domainNode));
            when(tableService.buildSchemaVisualizerDto(anyList())).thenReturn(SchemaVisualizerDto.builder().build());
        var response = domainNodeService.getSchemaVisualizerByDomain(1L);
        assertNull(response.getTables());
    }

    @Test
    public void getSchemaScoreTest() {
        var table = getTable();
        var domain = getDomain();
        domain.setTables(List.of(table, table.getFields().get(4).getContain()));
            when(domainNodeRepository.findById(anyLong())).thenReturn(Optional.of(domain));
        var response = domainNodeService.getSchemaScore(1L, table.getName());
        assertEquals(1.0, response);
    }

    @Test
    public void addDomainNodeTest() {
        var organization = Organization.builder().id(2L).name("OpsBeach").domains(List.of()).build();
            when(organizationService.getByClientId(anyLong())).thenReturn(organization);
            when(organizationService.get(anyLong())).thenReturn(organization);
            when(organizationService.update(any(Organization.class))).thenReturn(organization);
        
        var response = domainNodeService.addDomainNode("domain", 1L, 2L);
        assertEquals("domain", response.getName());
    }

    @Test
    public void deleteByClientRepoIdTest() {
        domainNodeService.deleteByClientRepoId(1L);
    }
}
