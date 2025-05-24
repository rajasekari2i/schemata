package com.opsbeach.connect.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.github.dto.DomainDto;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Domain;
import com.opsbeach.connect.github.repository.DomainRepository;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

public class DomainServiceTest {
    
    @InjectMocks 
    private DomainService domainService;

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private ResponseMessage responseMessage;

    @Mock
    private IdSpecifications<Domain> domainSpecifications;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private DomainDto getDomainDto() {
        return DomainDto.builder().id(1L).name("Domain").build();
    }

    @Test
    public void addTest() {
        var domainDto = getDomainDto();
        var domain = domainDto.toDomain(domainDto);
            when(domainRepository.save(any(Domain.class))).thenReturn(domain);
        var response = domainService.add(domainDto);
        assertEquals(domain.getName(), response.getName());
    }

    @Test
    public void getTest() {
        var domainDto = getDomainDto();
        var domain = domainDto.toDomain(domainDto);
            when(domainRepository.findById(1L)).thenReturn(Optional.of(domain));
        var response = domainService.get(1L);
        assertEquals(domain.getName(), response.getName());

        assertThrows(RecordNotFoundException.class, () -> { domainService.get(2L); });
    }

    @Test
    public void getAllTest() {
        var domainDto = getDomainDto();
        var domain = domainDto.toDomain(domainDto);
            when(domainRepository.findAll(ArgumentMatchers.<Specification<Domain>>any())).thenReturn(List.of(domain));
        var response = domainService.getAll(null);
        assertEquals(1, response.size());
        assertEquals(domain.getName(), response.get(0).getName());

            when(domainRepository.findAll(ArgumentMatchers.<Specification<Domain>>any())).thenReturn(List.of());
        response = domainService.getAll(1L);
        assertEquals(0, response.size());
    }

    @Test
    public void addDomainTest() {
        var clientRepo = ClientRepo.builder().id(1L).clientId(2L).fullName("fullName").build();
        var domain = Domain.builder().id(1L).nodeId(1L).name(clientRepo.getFullName()).build();
            when(domainRepository.save(any(Domain.class))).thenReturn(domain);
        var response = domainService.addDomain(clientRepo, 1L);
        assertEquals(domain.getName(), response.getName());
        assertEquals(domain.getNodeId(), response.getNodeId());
    }

    @Test
    public void getDefaultDomainTest() {
        var domain = Domain.builder().id(1L).nodeId(1L).name("FullName").build();
            when(domainRepository.findOne(ArgumentMatchers.<Specification<Domain>>any())).thenReturn(Optional.of(domain));
        var response = domainService.getDefaultDomain("FullName");
        assertEquals(domain.getName(), response.getName());
    }

    @Test
    public void deleteByClientRepoIdTest() {
        domainService.deleteByClientRepoId(1L);
    }
}
