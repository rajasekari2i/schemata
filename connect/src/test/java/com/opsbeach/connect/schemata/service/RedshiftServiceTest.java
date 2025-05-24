package com.opsbeach.connect.schemata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.schemata.dto.RedshiftDto;
import com.opsbeach.connect.schemata.entity.DomainNode;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.repository.SchemaRepository;

public class RedshiftServiceTest {
    
    @InjectMocks
    private RedshiftService redshiftService;

    @Mock
    private SchemaRepository schemaRepository;

    @Mock
    private TableService tableService;

    @Mock
    private DomainNodeService domainService;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private List<RedshiftDto> getSchemas() {
        return List.of(
            RedshiftDto.builder().tableCatalog("OpsBeach").tableSchema("analytics").tableName("task").columnName("id").dataType("bigint").isNullable("NO").columnDefault(1L).build(),
            RedshiftDto.builder().tableCatalog("OpsBeach").tableSchema("analytics").tableName("task").columnName("name").dataType("varchar").isNullable("NO").build()
        );
    }
    
    @Test
    public void getSchemaTest() {
        var domain = DomainNode.builder().id(1L).name("analytics").build();
        var redshiftDtos = getSchemas();
            when(domainService.get(anyLong())).thenReturn(domain);
            when(schemaRepository.getSchemaByName(anyString())).thenReturn(redshiftDtos);
            when(domainService.update(any(DomainNode.class))).thenReturn(domain);
        var response = redshiftService.getSchema(1L);
        assertEquals(response.getName(), domain.getName());
        domain.setTables(List.of(Table.builder().id(1L).build()));
            when(domainService.get(anyLong())).thenReturn(domain);
        response = redshiftService.getSchema(1L);
        assertEquals(response.getName(), domain.getName());
    }
}
