package com.opsbeach.connect.askob.messagerouting.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.askob.message.service.AskobMessageService;
import com.opsbeach.connect.askob.messagerouting.dto.MessageRoutingDto;
import com.opsbeach.connect.askob.messagerouting.entity.MessageRouting;
import com.opsbeach.connect.askob.messagerouting.repository.MessageRoutingRepository;
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
public class MessageRoutingService {
    
    private final AskobWorkspaceService askobWorkspaceService;

    private final AskobMessageService askobMessageService;

    private final MessageRoutingRepository messageRoutingRepository;

    private final IdSpecifications<MessageRouting> routingSpecifications;

    private final ResponseMessage responseMessage;

    public MessageRoutingDto add(MessageRoutingDto messageRoutingDto) {
        askobWorkspaceService.get(messageRoutingDto.getFromWorkspaceId());
        askobWorkspaceService.get(messageRoutingDto.getToWorkspaceId());
        askobMessageService.get(messageRoutingDto.getFromMessageId());
        askobMessageService.get(messageRoutingDto.getToMessageId());
        var messageRouting = messageRoutingDto.toDomin(messageRoutingDto);
        return  messageRouting.toDto(messageRoutingRepository.save(messageRouting));
    }

    public MessageRoutingDto get(Long id) {
        var messageRouting = messageRoutingRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.MESSAGE_ROUTING)));
        return messageRouting.toDto(messageRouting);
    }

    public List<MessageRoutingDto> getAll(Long fromMessageId) {
        Specification<MessageRouting> specifications = Specification.where(null);
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(fromMessageId))) {
            specifications = specifications.and(routingSpecifications.findByFromMessageId(fromMessageId));
        }
        var messageRoutings = messageRoutingRepository.findAll(specifications);
        return !ObjectUtils.isEmpty(messageRoutings) ? messageRoutings.stream().map(messageRoutings.get(0)::toDto).toList() : List.of();
    }

    public MessageRoutingDto update(MessageRoutingDto messageRoutingDto) {
        get(messageRoutingDto.getId());
        return add(messageRoutingDto);
    }

    public String delete(Long id) {
        var messageRoutingDto = get(id);
        var messageRouting = messageRoutingDto.toDomin(messageRoutingDto);
        messageRouting.setIsDeleted(Boolean.TRUE);
        messageRoutingRepository.save(messageRouting);
        return responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.MESSAGE_ROUTING);
    }
}
