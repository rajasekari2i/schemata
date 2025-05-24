package com.opsbeach.connect.askob.routing.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.askob.routing.dto.AskobRoutingDto;
import com.opsbeach.connect.askob.routing.entity.AskobRouting;
import com.opsbeach.connect.askob.routing.repository.AskobRoutingRepository;
import com.opsbeach.connect.askob.workspace.service.AskobWorkspaceService;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AskobRoutingService {
    
    private final AskobRoutingRepository askobRoutingRepository;

    private final AskobWorkspaceService askobWorkspaceService;

    private final IdSpecifications<AskobRouting> routingSpecifications;

    private final ResponseMessage responseMessage;

    public AskobRoutingDto add(AskobRoutingDto askobRoutingDto) {
        askobWorkspaceService.get(askobRoutingDto.getOriginWorkspaceId());
        askobWorkspaceService.get(askobRoutingDto.getToWorkspaceId());
        var askobRouting = askobRoutingDto.toDomin(askobRoutingDto);
        return askobRouting.toDto(askobRoutingRepository.save(askobRouting));
    }

    public AskobRoutingDto get(Long id) {
        var askobRouting = askobRoutingRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.ASKOB_ROUTING)));
        return askobRouting.toDto(askobRouting);
    }

    public List<AskobRoutingDto> getAll(String channelOrigin) {
        Specification<AskobRouting> specifications = Specification.where(null);
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(channelOrigin))) {
            specifications = specifications.and(routingSpecifications.findByChannelOrigin(channelOrigin));
        }
        var askobRoutings =  askobRoutingRepository.findAll(specifications);
        return ObjectUtils.isEmpty(askobRoutings) ? List.of() : askobRoutings.stream().map(askobRoutings.get(0)::toDto).collect(Collectors.toList());
    }

    public AskobRoutingDto update(AskobRoutingDto askobRoutingDto) {
        get(askobRoutingDto.getId());
        return add(askobRoutingDto);
    }

    public String delete(Long id) {
        var askobRoutingDto = get(id);
        var askobRouting = askobRoutingDto.toDomin(askobRoutingDto);
        askobRouting.setIsDeleted(Boolean.TRUE);
        askobRoutingRepository.save(askobRouting);
        return responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.ASKOB_ROUTING);
    }
}
