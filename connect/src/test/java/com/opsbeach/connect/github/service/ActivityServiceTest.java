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
import com.opsbeach.connect.github.dto.ActivityDto;
import com.opsbeach.connect.github.entity.Activity;
import com.opsbeach.connect.github.repository.ActivityRepository;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

public class ActivityServiceTest {
    
    @InjectMocks
    private ActivityService activityService;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private ResponseMessage responseMessage;

    @Mock
    private IdSpecifications<Activity> activitySpecifications;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private ActivityDto getActivityDto() {
        return ActivityDto.builder().id(1L).build();
    }

    @Test
    public void addTest() {
        var activityDto = getActivityDto();
        var activity = activityDto.toDomain(activityDto);
            when(activityRepository.save(any())).thenReturn(activity);
        var response = activityService.add(activityDto);
        assertEquals(activityDto.getId(), response.getId());
    }

    @Test
    public void getTest() {
        var activityDto = getActivityDto();
        var activity = activityDto.toDomain(activityDto);
            when(activityRepository.findById(activity.getId())).thenReturn(Optional.of(activity));
        var response = activityService.get(activity.getId());
        assertEquals(activityDto.getId(), response.getId());

        assertThrows(RecordNotFoundException.class, () -> { activityService.get(2L); });
    }

    @Test
    public void findAllByWorkflowIdTest() {
        var activityDto = getActivityDto();
        var activity = activityDto.toDomain(activityDto);
            when(activityRepository.findAll(ArgumentMatchers.<Specification<Activity>>any())).thenReturn(List.of(activity));
        var response = activityService.findAllByWorkflowId(2L);
        assertEquals(1, response.size());
        assertEquals(activityDto.getId(), response.get(0).getId());

            when(activityRepository.findAll(ArgumentMatchers.<Specification<Activity>>any())).thenReturn(List.of());
        response = activityService.findAllByWorkflowId(2L);
        assertEquals(0, response.size());
    }
}
