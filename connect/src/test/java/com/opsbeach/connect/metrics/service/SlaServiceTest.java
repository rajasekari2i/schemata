package com.opsbeach.connect.metrics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.metrics.dto.SlaDto;
import com.opsbeach.connect.metrics.entity.Sla;
import com.opsbeach.connect.metrics.repository.SlaRepository;
import com.opsbeach.sharedlib.exception.AlreadyExistException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

public class SlaServiceTest {
    
    @InjectMocks
    private SlaService slaService;

    @Mock
    private SlaRepository slaRepository;

    @Mock
    private ResponseMessage responseMessage;

    @Spy
    private IdSpecifications<Sla> slaSpecifications;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private SlaDto getSlaDto() {
        return SlaDto.builder().id(1L).type(ServiceType.PAGER_DUTY).build();
    }

    @Test
    public void getAllTest() {
        List<Sla> slas = List.of(Sla.builder().type(ServiceType.PAGER_DUTY).build());
        when(slaRepository.findAll()).thenReturn(slas);
        assertEquals(1, slaService.getAll().size());
        when(slaRepository.findAll()).thenReturn(null);
        assertEquals(0, slaService.getAll().size());
    }

    @Test
    public void updateTest() {
        var slaDto = getSlaDto();
        var sla = slaDto.toDomin(slaDto);
            when(slaRepository.findById(1L)).thenReturn(Optional.of(sla));
            when(slaRepository.save(ArgumentMatchers.any(Sla.class))).thenReturn(sla);
        var response = slaService.update(slaDto);
        assertEquals(slaDto.getType(), response.getType());
    }

    @Test
    public void deleteTest() {
        var slaDto = getSlaDto();
        var sla = slaDto.toDomin(slaDto);
            when(slaRepository.findById(1L)).thenReturn(Optional.of(sla));
            when(slaRepository.save(ArgumentMatchers.any(Sla.class))).thenReturn(sla);
        var response = slaService.delete(1L);
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.SLA), response);

        assertThrows(RecordNotFoundException.class, () -> { slaService.delete(2L); });
    }

    @Test
    public void addTest() {
        var slaDto = getSlaDto();
        var sla = slaDto.toDomin(slaDto);
            when(slaRepository.findOne(ArgumentMatchers.<Specification<Sla>>any())).thenReturn(Optional.empty());
            when(slaRepository.save(ArgumentMatchers.any(Sla.class))).thenReturn(sla);
        var response = slaService.add(slaDto);
        assertEquals(slaDto.getType(), response.getType());

            when(slaRepository.findOne(ArgumentMatchers.<Specification<Sla>>any())).thenReturn(Optional.of(sla));
        assertThrows(AlreadyExistException.class, () -> { slaService.add(slaDto); });
    }

    @Test
    public void getByTypeTest() {
        var slaDto = getSlaDto();
        var sla = slaDto.toDomin(slaDto);
            when(slaRepository.findOne(ArgumentMatchers.<Specification<Sla>>any())).thenReturn(Optional.of(sla));
        var response = slaService.getByType(ServiceType.PAGER_DUTY);
        assertEquals(slaDto.getType(), response.getType());

            when(slaRepository.findOne(ArgumentMatchers.<Specification<Sla>>any())).thenReturn(Optional.empty());
        assertThrows(RecordNotFoundException.class, () -> { slaService.getByType(ServiceType.GITHUB); });
    }
}
