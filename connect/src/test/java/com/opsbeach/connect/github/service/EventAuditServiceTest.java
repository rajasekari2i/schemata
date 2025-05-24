package com.opsbeach.connect.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
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
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.github.dto.EventAuditDto;
import com.opsbeach.connect.github.entity.EventAudit;
import com.opsbeach.connect.github.entity.EventAudit.Type;
import com.opsbeach.connect.github.repository.EventAuditRepository;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.exception.SchemaParserException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.service.GoogleCloudService;

public class EventAuditServiceTest {
 
    @InjectMocks
    private EventAuditService eventAuditService;

    @Mock
    private EventAuditRepository eventAuditRepository;

    @Mock
    private ConnectService connectService;

    @Mock
    private ResponseMessage responseMessage;

    @Mock
    private IdSpecifications<EventAudit> eventAuditSpecifications;

    @Mock
    private GoogleCloudService googleCloudService;

    @Mock
    private ClientRepoService clientRepoService;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private void mockApplicationUser() {
        UserDto userDto = mock(UserDto.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDto);
    }

    @Test
    public void addTest() {
            when(connectService.get(anyLong())).thenReturn(ConnectDto.builder().id(1L).build());
        var eventAuditDto = EventAuditDto.builder().id(1L).build();
        var eventAudit = eventAuditDto.toDomain(eventAuditDto);
            when(eventAuditRepository.save(any(EventAudit.class))).thenReturn(eventAudit);
        var response = eventAuditService.add(eventAuditDto);
        assertEquals(eventAudit.getId(), response.getId());
    }

    @Test
    public void getTest() {
        var eventAudit = EventAudit.builder().id(1L).build();
            when(eventAuditRepository.findById(1L)).thenReturn(Optional.of(eventAudit));
        var response = eventAuditService.get(1L);
        assertEquals(eventAudit.getId(), response.getId());

        assertThrows(RecordNotFoundException.class, () -> { eventAuditService.get(2L); });
    }

    @Test
    public void getAllTest() {
        assertEquals(0, eventAuditService.getAll().size());
        var eventAudit = EventAudit.builder().id(1L).build();
            when(eventAuditRepository.findAll()).thenReturn(List.of(eventAudit));
        var response = eventAuditService.getAll();
        assertEquals(1, response.size());
    }

    @Test
    public void getInitialLoadStatusTest() {
        assertEquals(0, eventAuditService.getInitialLoadStatus().size());
        var eventAudit = EventAudit.builder().id(1L).status(EventAudit.Status.PENDING).build();
            when(eventAuditRepository.findAll(ArgumentMatchers.<Specification<EventAudit>>any())).thenReturn(List.of(eventAudit));
        var response = eventAuditService.getInitialLoadStatus();
        assertEquals(1, response.size());
        assertEquals(eventAudit.getStatus(), response.get(0).getStatus());
    }

    @Test
    public void addAllTest() {
        var eventAudit = EventAudit.builder().id(1L).status(EventAudit.Status.PENDING).build();
            when(eventAuditRepository.saveAll(anyList())).thenReturn(List.of(eventAudit));
        var response = eventAuditService.addAll(List.of(eventAudit));
        assertEquals(1, response.size());
        assertEquals(eventAudit.getStatus(), response.get(0).getStatus());
    }

    @Test
    public void updateStatusTest() {
        var eventAudit = EventAudit.builder().id(1L).status(EventAudit.Status.PENDING).build();
            when(eventAuditRepository.findById(1L)).thenReturn(Optional.of(eventAudit));
        eventAudit.setStatus(EventAudit.Status.IN_PROGRESS);
            when(eventAuditRepository.save(any(EventAudit.class))).thenReturn(eventAudit);
        var response = eventAuditService.updateStatus(1L, EventAudit.Status.IN_PROGRESS);
        assertEquals(EventAudit.Status.IN_PROGRESS, response.getStatus());

        eventAudit.setStatus(EventAudit.Status.COMPLETED);
            when(eventAuditRepository.save(any(EventAudit.class))).thenReturn(eventAudit);
        response = eventAuditService.updateStatus(1L, EventAudit.Status.COMPLETED);
        assertEquals(EventAudit.Status.COMPLETED, response.getStatus());
    }

    @Test
    public void processEventAuditTest() {
        var eventAudit = EventAudit.builder().id(1L).type(Type.REPOSITORY_INITIAL_PULL).clientId(1L)
                                   .status(EventAudit.Status.COMPLETED).build();
            when(eventAuditRepository.findById(1L)).thenReturn(Optional.of(eventAudit));
            mockApplicationUser();
        assertThrows(AlreadyBuiltException.class, () -> eventAuditService.processEventAudit(1L));

        eventAudit.setStatus(EventAudit.Status.PENDING);
            when(eventAuditRepository.findById(1L)).thenReturn(Optional.of(eventAudit));
            ReflectionTestUtils.setField(eventAuditService, "clientRepoService", clientRepoService);
        assertTrue(eventAuditService.processEventAudit(1L));

            when(clientRepoService.initialLoading(any(EventAudit.class))).thenThrow(SchemaParserException.class);
        assertTrue(eventAuditService.processEventAudit(1L));
        
        eventAudit = EventAudit.builder().id(1L).type(Type.CSV_FILE_UPLOAD).clientId(1L)
                                   .status(EventAudit.Status.PENDING).build();
            when(eventAuditRepository.findById(1L)).thenReturn(Optional.of(eventAudit));
        assertTrue(eventAuditService.processEventAudit(1L));
    }
}
