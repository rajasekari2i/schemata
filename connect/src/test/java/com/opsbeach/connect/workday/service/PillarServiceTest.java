package com.opsbeach.connect.workday.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.workday.dto.PillarDto;
import com.opsbeach.connect.workday.entity.Pillar;
import com.opsbeach.connect.workday.repository.PillarRepository;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

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

public class PillarServiceTest {
    
    @InjectMocks
    private PillarService pillarService;

    @Mock
    private PillarRepository pillarRepository;

    @Spy
    private IdSpecifications<Pillar> pillarSpecifications;

    @Mock
    private ResponseMessage responseMessage;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private List<Pillar> getPillars() {
        List<Pillar> pillars = new ArrayList<>();
        pillars.add(Pillar.builder().id(1L).name("Analytics").description("Analytics pillar for devloper").clientId(1L).build());
        pillars.add(Pillar.builder().id(2L).name("Data Streaming").description("Streaming of Data").clientId(1L).build());
        return pillars;
    }

    @Test
    public void getByNameTestFail() {
        assertThrows(RecordNotFoundException.class, () -> { pillarService.getByName("Data Streaming"); });    
    }

    @Test
    public void getByNameTestPass() {
        PillarDto pillarDto = PillarDto.builder().name("Analytics").build();
        var pillars = getPillars();
        when(pillarRepository.findOne(ArgumentMatchers.<Specification<Pillar>>any())).thenReturn(Optional.of(pillars.get(0)));
        var response = pillarService.getByName("Analytics");
        assertEquals(pillarDto.getName(), response.getName());
    }

    @Test
    public void addTestPass() {
        var pillar = getPillars().get(1);
        var pillarDto = pillar.toDto(pillar);
        when(pillarRepository.save(ArgumentMatchers.<Pillar>any())).thenReturn(pillar);
        var response = pillarService.add(pillarDto);
        assertEquals(pillarDto.getName(), response.getName());
    }

    @Test
    public void addTestFail() {
        PillarDto pillarDto = PillarDto.builder().name("Analytics").build();
        var pillars = getPillars();
        when(pillarRepository.findOne(ArgumentMatchers.<Specification<Pillar>>any())).thenReturn(Optional.of(pillars.get(0)));
        assertThrows(IllegalArgumentException.class, () -> {
            pillarService.add(pillarDto);
        });
    }

    @Test
    public void getAllTest() {
        var pillars = getPillars();
        when(pillarRepository.findAll()).thenReturn(pillars);
        var pillarDtos = pillars.stream().map(pillars.get(0)::toDto).collect(Collectors.toList());
        var response = pillarService.getAll();
        assertEquals(pillarDtos.get(0).getId(), response.get(0).getId());
        assertEquals(pillarDtos.get(1).getId(), response.get(1).getId());
        when(pillarRepository.findAll()).thenReturn(null);
        assertEquals(0, pillarService.getAll().size());
    }

    @Test
    public void updateTestPass() {
        var pillarDto = PillarDto.builder().id(1L).name("Analytics").description("Analytics pillar for devloper").clientId(1L).build();
        var pillars = getPillars();
        when(pillarRepository.findById(pillarDto.getId())).thenReturn(Optional.of(pillars.get(0)));
        when(pillarRepository.save(ArgumentMatchers.<Pillar>any())).thenReturn(pillars.get(0));
        var response = pillarService.update(pillarDto);
        assertEquals(pillarDto.getId(), response.getId());
        assertEquals(pillarDto.getDescription(), response.getDescription());
    }

    @Test
    public void deleteTestPass() {
        Pillar pillar = getPillars().get(1);
        when(pillarRepository.findById(pillar.getId())).thenReturn(Optional.of(pillar));
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.DELETED, "Pillar"), pillarService.delete(pillar.getId()));

        assertThrows(RecordNotFoundException.class, () -> { pillarService.delete(1L); });
    }
}
