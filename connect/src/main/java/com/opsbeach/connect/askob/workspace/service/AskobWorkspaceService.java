package com.opsbeach.connect.askob.workspace.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.askob.workspace.dto.AskobWorkspaceDto;
import com.opsbeach.connect.askob.workspace.entity.AskobWorkspace;
import com.opsbeach.connect.askob.workspace.repository.AskobWorkspaceRepository;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AskobWorkspaceService {
    
    private final AskobWorkspaceRepository askobWorkspaceRepository;

    private final IdSpecifications<AskobWorkspace> workspaceSpecification;

    private final ResponseMessage responseMessage;

    public AskobWorkspaceDto add(AskobWorkspaceDto askobWorkspaceDto) {
        var askobWorkspace = askobWorkspaceDto.toDomin(askobWorkspaceDto);
        return askobWorkspace.toDto(askobWorkspaceRepository.save(askobWorkspace));
    }

    public AskobWorkspaceDto get(Long id) {
        var askobWorkspace = askobWorkspaceRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.ASKOB_WORKSPACE)));
        return askobWorkspace.toDto(askobWorkspace);
    }

    public List<AskobWorkspaceDto> getAll(String key) {
        Specification<AskobWorkspace> specifications = Specification.where(null);
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(key))) {
            specifications = specifications.and(workspaceSpecification.findByKey(key));
        }
        var askobWorkspaces = askobWorkspaceRepository.findAll(specifications);
        return ObjectUtils.isEmpty(askobWorkspaces) ? List.of() : askobWorkspaces.stream().map(askobWorkspaces.get(0)::toDto).collect(Collectors.toList());
    }

    public AskobWorkspaceDto update(AskobWorkspaceDto askobWorkspaceDto) {
        get(askobWorkspaceDto.getId());
        return add(askobWorkspaceDto);
    }

    public String delete(Long id) {
        var askobWorkspaceDto = get(id);
        var askobWorkspace = askobWorkspaceDto.toDomin(askobWorkspaceDto);
        askobWorkspace.setIsDeleted(Boolean.TRUE);
        askobWorkspaceRepository.save(askobWorkspace);
        return responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.ASKOB_WORKSPACE);
    }
}
