package com.opsbeach.connect.github.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.github.dto.ActivityDto;
import com.opsbeach.connect.github.entity.Activity;
import com.opsbeach.connect.github.repository.ActivityRepository;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityService {
    
    private final ActivityRepository activityRepository;

    private final WorkflowService workflowService;

    private final ResponseMessage responseMessage;

    private final IdSpecifications<Activity> activitySpecifications;

    public ActivityDto add(ActivityDto activityDto) {
        workflowService.get(activityDto.getWorkflowId());
        var activity = activityRepository.save(activityDto.toDomain(activityDto));
        return activity.toDto(activity);
    }

    public ActivityDto get(Long id) {
        var activity = activityRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.ACTIVITY)));
        return activity.toDto(activity);
    }

    public List<ActivityDto> findAllByWorkflowId(Long workflowId) {
        Specification<Activity> specification = Specification.where(null);
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(workflowId))) {
            specification = specification.and(activitySpecifications.findByWorkflowId(workflowId));
        }
        var activities = activityRepository.findAll(specification);
        return activities.isEmpty() ? List.of() : activities.stream().map(activities.get(0)::toDto).toList();
    }
}
