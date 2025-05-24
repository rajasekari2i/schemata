package com.opsbeach.connect.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.github.dto.PullRequestDto;
import com.opsbeach.connect.github.entity.PullRequest;
import com.opsbeach.connect.github.entity.PullRequest.Status;
import com.opsbeach.connect.github.repository.PullRequestRepository;
import com.opsbeach.connect.metrics.dto.SlaDto;
import com.opsbeach.connect.metrics.service.SlaService;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class PullRequestServiceTest {
    
    @InjectMocks
    private PullRequestService pullRequestService;

    @Mock
    private PullRequestRepository pullRequestRepository;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private ResponseMessage responseMessage;

    @Spy
    private IdSpecifications<PullRequest> pIdSpecifications;

    @Mock
    private SlaService slaService;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<PullRequest> criteriaQuery;

    @Mock
    private Root<PullRequest> root;

    @Mock
    private TypedQuery<PullRequest> typedQuery;
    
    @Mock
    private EntityManager entityManager;

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

    private void mockEntityManager() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(PullRequest.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(PullRequest.class)).thenReturn(root);

        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);
    }

    private PullRequestDto getPullRequestDto() {
        return PullRequestDto.builder().id(1L).workflowId(2L).build();
    }

    @Test
    public void addModelTest() {
        var pullRequest = PullRequest.builder().id(1L).workflowId(2L).build();
            when(pullRequestRepository.save(any(PullRequest.class))).thenReturn(pullRequest);
        var response = pullRequestService.addModel(pullRequest);
        assertEquals(pullRequest.getId(), response.getId());
    }

    @Test
    public void getTest() {
        var pullRequestDto = getPullRequestDto();
        var pullRequest = pullRequestDto.toDomain(pullRequestDto);
            when(pullRequestRepository.findById(pullRequestDto.getId())).thenReturn(Optional.of(pullRequest));
        var response = pullRequestService.get(pullRequest.getId());
        assertEquals(pullRequestDto.getId(), response.getId());

        assertThrows(RecordNotFoundException.class, () -> { pullRequestService.get(2L); });
    }

    @Test
    public void updateStatusTest() {
        var pullRequestDto = getPullRequestDto();
        var pullRequest = pullRequestDto.toDomain(pullRequestDto);
            when(pullRequestRepository.findById(anyLong())).thenReturn(Optional.of(pullRequest));
            when(pullRequestRepository.save(any())).thenReturn(pullRequest);
        var response = pullRequestService.updateStatus(1L, PullRequest.Status.OPEN);
        assertEquals(pullRequestDto.getId(), response.getId());
    }

    @Test
    public void findByRepoIdAndNumberTest() {
        var pullRequestDto = getPullRequestDto();
        var pullRequest = pullRequestDto.toDomain(pullRequestDto);
            when(pullRequestRepository.findOne(ArgumentMatchers.<Specification<PullRequest>>any())).thenReturn(Optional.of(pullRequest));
        var response = pullRequestService.findByRepoIdAndNumber(1L, "404");
        assertEquals(pullRequest.getNumber(), response.getNumber());

            when(pullRequestRepository.findOne(ArgumentMatchers.<Specification<PullRequest>>any())).thenReturn(Optional.empty());
        response = pullRequestService.findByRepoIdAndNumber(1L, "404");
        assertNull(pullRequestService.findByRepoIdAndNumber(1L, "404"));
    }

    @Test
    public void updateModel() {
        var pullRequestDto = getPullRequestDto();
        var pullRequest = pullRequestDto.toDomain(pullRequestDto);
            when(pullRequestRepository.findById(pullRequestDto.getId())).thenReturn(Optional.of(pullRequest));
            when(pullRequestRepository.save(any(PullRequest.class))).thenReturn(pullRequest);
        var response = pullRequestService.updateModel(pullRequest);
        assertEquals(pullRequestDto.getId(), response.getId());
    }

    @Test
    public void getAllTest() {
        var pullRequestDto = getPullRequestDto();
        var pullRequest = pullRequestDto.toDomain(pullRequestDto);
        Page<PullRequest> pageList = new PageImpl<>(List.of(pullRequest));
            when(pullRequestRepository.findAll(any(Pageable.class))).thenReturn(pageList);
        var response = pullRequestService.getAll(Pageable.ofSize(3));
        assertEquals(pullRequestDto.getId(), response.get(0).getId());
        assertEquals(pageList.getSize(), response.size());

            when(pullRequestRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));
        response = pullRequestService.getAll(Pageable.ofSize(3));
        assertEquals(0, response.size());
    }

    @Test
    public void getCountWithWorkflowTest() {
        when(pullRequestRepository.count(ArgumentMatchers.<Specification<PullRequest>>any())).thenReturn(2L);
        assertEquals(2L, pullRequestService.getCountWithWorkflow(1L));
    }

    @Test
    public void getCountTest() {
            when(pullRequestRepository.count(ArgumentMatchers.<Specification<PullRequest>>any())).thenReturn(10L);
        var response = pullRequestService.getCount(null, LocalDateTime.now());
        assertEquals(10L, response);

            when(pullRequestRepository.count(ArgumentMatchers.<Specification<PullRequest>>any())).thenReturn(5L);
        response = pullRequestService.getCount(Status.OPEN, LocalDateTime.now());
        assertEquals(5L, response);
        response = pullRequestService.getCount(Status.OPEN, null);
        assertEquals(5L, response);
    }

    @Test
    public void prCountBySlaTimeTest() {
        when(slaService.getByType(ServiceType.GITHUB)).thenReturn(SlaDto.builder().slaTime(123).type(ServiceType.GITHUB).build());
        when(pullRequestRepository.count(ArgumentMatchers.<Specification<PullRequest>>any())).thenReturn(5L);
        mockApplicationUser();
        assertEquals(5L, pullRequestService.prCountBySlaTime(false));
        assertEquals(5L, pullRequestService.prCountBySlaTime(true));
    }

    @Test
    public void getDashboardMetricsTest() {
        var pullRequests = List.of(
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(3)).status(PullRequest.Status.MERGED).updatedAt(LocalDateTime.now().minusHours(4)).workflowId(2L).build(),
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(3)).status(PullRequest.Status.OPEN).updatedAt(LocalDateTime.now().minusHours(4)).workflowId(2L).build(),
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(3)).status(PullRequest.Status.REOPENED).updatedAt(LocalDateTime.now().minusDays(3).plusHours(3)).workflowId(2L).build(),
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(9)).status(PullRequest.Status.MERGED).updatedAt(LocalDateTime.now().minusHours(4)).workflowId(2L).build(),
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(9)).status(PullRequest.Status.OPEN).updatedAt(LocalDateTime.now().minusDays(4)).workflowId(2L).build()
        );
            when(slaService.getByType(ServiceType.GITHUB)).thenReturn(SlaDto.builder().slaTime(123).type(ServiceType.GITHUB).build());
            mockApplicationUser();
            mockEntityManager();
            when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(pullRequests);
        var response = pullRequestService.getDashboardMetrics();
        assertEquals(2L, response.getOpenPrsCount());
        assertEquals(1L, response.getClosePrsCount());
        assertEquals(100, response.getOpenPrPercent());
        assertEquals(50, response.getTotalPrPercent());

            when(typedQuery.getResultList()).thenReturn(List.of());
        response = pullRequestService.getDashboardMetrics();

        pullRequests = List.of(
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(3)).status(PullRequest.Status.MERGED).updatedAt(LocalDateTime.now().minusHours(4)).workflowId(2L).build(),
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(3)).status(PullRequest.Status.OPEN).updatedAt(LocalDateTime.now().minusHours(4)).workflowId(2L).build(),
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(3)).status(PullRequest.Status.REOPENED).updatedAt(LocalDateTime.now().minusDays(3).plusHours(3)).workflowId(2L).build()
        );
        when(typedQuery.getResultList()).thenReturn(pullRequests);
        response = pullRequestService.getDashboardMetrics();
    }

    @Test
    public void getSlaMeanCountTest() {
        var pullRequests = List.of(
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(3)).status(PullRequest.Status.MERGED).updatedAt(LocalDateTime.now().minusHours(4)).workflowId(2L).build(),
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(3)).status(PullRequest.Status.OPEN).updatedAt(LocalDateTime.now().minusHours(4)).workflowId(2L).build(),
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now()).status(PullRequest.Status.CLOSED).updatedAt(LocalDateTime.now().minusDays(3).plusHours(3)).workflowId(2L).build(),
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now().minusDays(9)).status(PullRequest.Status.MERGED).updatedAt(LocalDateTime.now().minusHours(4)).workflowId(2L).build(),
            PullRequest.builder().id(1L).createdAt(LocalDateTime.now()).status(PullRequest.Status.OPEN).updatedAt(LocalDateTime.now().minusDays(4)).workflowId(2L).build()
        );
        var response = pullRequestService.getSlaMeanCount(pullRequests, 123);
        assertEquals(3, response.get("slaTimeExceded"));
    }
}
