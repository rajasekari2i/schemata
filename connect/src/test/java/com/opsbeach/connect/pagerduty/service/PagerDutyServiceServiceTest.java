package com.opsbeach.connect.pagerduty.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.ServiceDto;
import com.opsbeach.connect.pagerduty.entity.PagerDutyService;
import com.opsbeach.connect.pagerduty.enums.ServiceStatus;
import com.opsbeach.connect.pagerduty.repository.PagerDutyServiceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class PagerDutyServiceServiceTest {

    @InjectMocks
    private PagerDutyServiceService pagerDutyServiceService;

    @Mock
    private PagerDutyServiceRepository pagerDutyServiceRepository;

    @Spy
    private IdSpecifications<PagerDutyService> serviceSpecifications;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }  

    private List<ServiceDto> getServiceDtos() {
        return List.of(ServiceDto.builder().id("1").name("name").clientId(1L)
                                 .status(ServiceStatus.WARNING).description("description").build());
    }

    @Test
    public void addAllTest() {
        var response = pagerDutyServiceService.addAll(null);
        assertEquals(0, response.size());

        var serviceDtos = getServiceDtos();
        var services = serviceDtos.stream().map(serviceDtos.get(0)::toDomain).collect(Collectors.toList());
        when(pagerDutyServiceRepository.saveAll(ArgumentMatchers.<List<PagerDutyService>>any())).thenReturn(services);
        response = pagerDutyServiceService.addAll(serviceDtos);
        assertEquals(serviceDtos.get(0).getStatus(), response.get(0).getStatus());
    }

    @Test
    public void getAllTest() {
        var response = pagerDutyServiceService.getAll(1L);
        assertEquals(0, response.size());
        
        var serviceDtos = getServiceDtos();
        var services = serviceDtos.stream().map(serviceDtos.get(0)::toDomain).collect(Collectors.toList());
        when(pagerDutyServiceRepository.findAll(ArgumentMatchers.<Specification<PagerDutyService>>any())).thenReturn(services);
        response = pagerDutyServiceService.getAll(1L);
        assertEquals(serviceDtos.get(0).getStatus(), response.get(0).getStatus());
    }

    @Test
    public void deleteAllByIdsTest() {
        pagerDutyServiceService.deleteAllByIds(List.of(1L));
    }
}
