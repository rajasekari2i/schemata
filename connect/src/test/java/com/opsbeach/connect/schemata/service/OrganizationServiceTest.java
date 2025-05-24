package com.opsbeach.connect.schemata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.opsbeach.connect.schemata.entity.Organization;
import com.opsbeach.connect.schemata.repository.OrganizationRepository;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

public class OrganizationServiceTest {
    
    @InjectMocks
    private OrganizationService organizationService;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private ResponseMessage responseMessage;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private Organization getOrg() {
        return Organization.builder().id(1L).name("OpsBeach").build();
    }

    @Test
    public void addTest() {
        var organization = getOrg();
            when(organizationRepository.save(any(Organization.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = organizationService.add(1L, organization.getName());
        assertEquals(response.getClinetId(), 1L);
            when(organizationRepository.findByClinetId(anyLong())).thenReturn(organization);
        response = organizationService.add(1L, organization.getName());
        assertEquals(response.getName(), organization.getName());
    }

    @Test
    public void updateTest() {
        var organization = getOrg();
            when(organizationRepository.findById(anyLong())).thenReturn(Optional.of(organization));
            when(organizationRepository.save(any(Organization.class))).thenReturn(organization);
        assertEquals(organizationService.update(organization).getName(), organization.getName());
    }
    
    @Test
    public void getTestFail() {
        var organization = getOrg();
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        var response = organizationService.get(1L);
        assertEquals(response.getId(), organization.getId());
        assertThrows(RecordNotFoundException.class, () -> { organizationService.get(2L); });
    }

    @Test
    public void getAllTest() {
        var organization = getOrg();
            when(organizationRepository.findAll()).thenReturn(List.of(organization));
        var response = organizationService.getAll();
        assertEquals(organization.getName(), response.get(0).getName());
    }
}
