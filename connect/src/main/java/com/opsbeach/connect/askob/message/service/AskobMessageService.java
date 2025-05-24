package com.opsbeach.connect.askob.message.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.askob.message.dto.AskobMessageDto;
import com.opsbeach.connect.askob.message.entity.AskobMessage;
import com.opsbeach.connect.askob.message.repository.AskobMessageRepository;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AskobMessageService {
    
    private final AskobMessageRepository askobMessageRepository;

    private final IdSpecifications<AskobMessage> messageSpecifications;

    private final ResponseMessage responseMessage;

    public AskobMessageDto add(AskobMessageDto askobMessageDto) {
        var askobMessage = askobMessageDto.toDomin(askobMessageDto);
        return askobMessage.toDto(askobMessageRepository.save(askobMessage));
    }

    public AskobMessageDto get(Long id) {
        var askobMessage = askobMessageRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, Constants.ASKOB_MESSAGE, id.toString())));
        return askobMessage.toDto(askobMessage);
    }

    public AskobMessageDto getByMessageTs(String messageTs) {
        var askobMessage = askobMessageRepository.findOne(messageSpecifications.findByMessageTs(messageTs)).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, Constants.ASKOB_MESSAGE, messageTs)));
        return askobMessage.toDto(askobMessage);
    }

    public List<AskobMessageDto> getAll() {
        var askobMessages = askobMessageRepository.findAll();
        return ObjectUtils.isEmpty(askobMessages) ? List.of() : askobMessages.stream().map(askobMessages.get(0)::toDto).collect(Collectors.toList());
    }

    public AskobMessageDto update(AskobMessageDto askobMessageDto) {
        get(askobMessageDto.getId());
        return add(askobMessageDto);
    }

    public String delete(Long id) {
        var askobMessageDto = get(id);
        var askobMessage = askobMessageDto.toDomin(askobMessageDto);
        askobMessage.setIsDeleted(Boolean.TRUE);
        askobMessageRepository.save(askobMessage);
        return responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.ASKOB_MESSAGE);
    }
}
