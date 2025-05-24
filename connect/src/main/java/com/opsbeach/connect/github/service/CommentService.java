package com.opsbeach.connect.github.service;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.github.dto.CommentDto;
import com.opsbeach.connect.github.entity.Comment;
import com.opsbeach.connect.github.repository.CommentRepository;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;

    private final PullRequestService pullRequestService;

    private final ResponseMessage responseMessage;

    public CommentDto add(CommentDto commentDto) {
        if (!ObjectUtils.isEmpty(commentDto.getCommentableId())) get(commentDto.getCommentableId());
        pullRequestService.get(commentDto.getPullRequestId());
        var comment = commentRepository.save(commentDto.toDomain(commentDto));
        return comment.toDto(comment);
    }

    public CommentDto get(Long id) {
        var comment = getModel(id);
        return comment.toDto(comment);
    }

    public Comment getModel(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.COMMENT)));
    }

    public CommentDto updateIsResolved(Long id, boolean isResolved) {
        var comment = getModel(id);
        comment.setIsResolved(isResolved);
        comment = commentRepository.save(comment);
        return comment.toDto(comment);
    }
}
