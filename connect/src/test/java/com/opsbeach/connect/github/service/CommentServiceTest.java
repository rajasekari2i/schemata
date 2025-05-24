package com.opsbeach.connect.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.github.dto.CommentDto;
import com.opsbeach.connect.github.repository.CommentRepository;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

public class CommentServiceTest {
    
    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PullRequestService pullRequestService;

    @Mock
    private ResponseMessage responseMessage;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private CommentDto getCommentDto() {
        return CommentDto.builder().id(1L).build();
    }

    @Test
    public void addTest() {
        var commentDto = getCommentDto();
        var comment = commentDto.toDomain(getCommentDto());
            when(commentRepository.save(any())).thenReturn(comment);
        var response = commentService.add(commentDto);
        assertEquals(comment.getId(), response.getId());

        commentDto = CommentDto.builder().id(2L).isResolved(Boolean.FALSE).commentableId(1L).build();
            when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        response = commentService.add(commentDto);
        assertEquals(comment.getCommentableId(), response.getCommentableId());
    }

    @Test
    public void getTest() {
        var commentDto = getCommentDto();
        var comment = commentDto.toDomain(getCommentDto());
            when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        var request = commentService.get(1L);
        assertEquals(comment.getId(), request.getId());

        assertThrows(RecordNotFoundException.class, () -> { commentService.get(2L); });
    }

    @Test
    public void updateIsResolvedTest() {
        var commentDto = getCommentDto();
        var comment = commentDto.toDomain(getCommentDto());
            when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        comment.setIsResolved(Boolean.TRUE);
            when(commentRepository.save(any())).thenReturn(comment);
        var response = commentService.updateIsResolved(1L, Boolean.TRUE);
        assertEquals(Boolean.TRUE, response.isResolved());
    }
}
