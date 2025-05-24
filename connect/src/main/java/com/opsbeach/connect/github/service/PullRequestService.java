package com.opsbeach.connect.github.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.github.dto.DashboardDto;
import com.opsbeach.connect.github.dto.PullRequestDto;
import com.opsbeach.connect.github.entity.PullRequest;
import com.opsbeach.connect.github.entity.PullRequest.Status;
import com.opsbeach.connect.github.repository.PullRequestRepository;
import com.opsbeach.connect.metrics.service.SlaService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.utils.DateUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PullRequestService {
    
    private final PullRequestRepository pullRequestRepository;

    private final ResponseMessage responseMessage;

    private final IdSpecifications<PullRequest> pullRequestSpecifications;

    private final SlaService slaService;

    private final EntityManager entityManager;

    public PullRequest addModel(PullRequest pullRequest) {
        return pullRequestRepository.save(pullRequest);
    }

    public PullRequestDto get(Long id) {
        var pullRequest = getModel(id);
        return pullRequest.toDto(pullRequest);
    }

    public PullRequest getModel(Long id) {
        return pullRequestRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.PULL_REQUEST)));
    }

    public PullRequest findByRepoIdAndNumber(Long clientRepoId, String number){
        return pullRequestRepository.findOne(pullRequestSpecifications.findByClientRepoId(clientRepoId).and(pullRequestSpecifications.findByNumber(number))).orElse(null);
    }

    public PullRequest updateModel(PullRequest pullRequest) {
        get(pullRequest.getId());
        return pullRequestRepository.save(pullRequest);
    }

    public PullRequestDto updateStatus(Long id, Status status) {
        var pullRequest = getModel(id);
        pullRequest.setStatus(status);
        pullRequest = pullRequestRepository.save(pullRequest);
        return pullRequest.toDto(pullRequest);
    }

    public List<PullRequestDto> getAll(Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, CREATED_AT));
        var pullRequests = pullRequestRepository.findAll(pageable);
        return pullRequests.getContent().stream().map(m -> m.toDto(m)).collect(Collectors.toList());
    }

    public Long getCountWithWorkflow(Long clientRepoId) {
        var specification = pullRequestSpecifications.findByClientRepoId(clientRepoId).and(pullRequestSpecifications.workflowIsNotNull());
        return Long.valueOf(pullRequestRepository.count(specification));
    }

    public Long getCount(Status status, LocalDateTime fromDateTime) {
        if (status == null) {
            return Long.valueOf(pullRequestRepository.count(pullRequestSpecifications.greaterThanCreatedAt(fromDateTime)));
        }
        var specification = pullRequestSpecifications.findByPullRequestStatus(status);
        if (Objects.nonNull(fromDateTime)) specification = specification.and(pullRequestSpecifications.greaterThanCreatedAt(fromDateTime));
        // if (Status.OPEN.equals(status)) specification = specification.or(pullRequestSpecifications.findByPullRequestStatus(Status.REOPENED));
        return Long.valueOf(pullRequestRepository.count(specification));
    }

    public Long prCountBySlaTime(boolean isExceeded) {
        var clientId = SecurityUtil.getClientId();
        SecurityUtil.setClientId(0L);  // need to set clientId 0 (default value added in migration) untill we get SLA time from user
        var sla = slaService.getByType(ServiceType.GITHUB);  // then only it will fetch sla time for all the clients.
        SecurityUtil.setClientId(clientId);
        return prCountBySlaTime(isExceeded, sla.getSlaTime());
    }

    private Long prCountBySlaTime(boolean isExceeded, Long slaTime) {
        var date = LocalDateTime.now().minusSeconds(slaTime);
        var specification = isExceeded ? pullRequestSpecifications.lessThanCreatedAt(date)
                                       : pullRequestSpecifications.greaterThanCreatedAt(date);
        specification = specification.and(pullRequestSpecifications.findByPullRequestStatus(Status.OPEN)
                                      .or(pullRequestSpecifications.findByPullRequestStatus(Status.REOPENED)));
        return Long.valueOf(pullRequestRepository.count(specification));
    }

    private static final String CREATED_AT = "createdAt";

    public DashboardDto getDashboardMetrics() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PullRequest> query = builder.createQuery(PullRequest.class);
        Root<PullRequest> root = query.from(PullRequest.class);
        Map<String, Object> openCloseGraph = new HashMap<>();
        Map<String, Object> slaMeanGraph = new HashMap<>();

        var clientId = SecurityUtil.getClientId();
        SecurityUtil.setClientId(0L);  // need to set clientId 0 (default value added in migration) untill we get SLA time from user
        var sla = slaService.getByType(ServiceType.GITHUB);  // then only it will fetch sla time for all the clients.
        SecurityUtil.setClientId(clientId);
        
        List<PullRequest> openPrsCurrentWeek = new ArrayList<>();  // PR's created this week and still OPEN
        List<PullRequest> openPrsLastWeek = new ArrayList<>();  // PR's created last week and still OPEN
        List<PullRequest> closePrsCurrentWeek = new ArrayList<>(); // PR's created this week and CLOSED
        List<PullRequest> closePrsLastWeek = new ArrayList<>(); // PR's created last week and CLOSED
        List<PullRequest> totalPrsCurrentWeek = new ArrayList<>(); // total PR's created this week 
        List<PullRequest> totalPrsLastWeek = new ArrayList<>(); // total PR's created last week 
        Multimap<LocalDate, PullRequest> openPullRequests = LinkedHashMultimap.create();  // to collect open PR's of Last 7 days.
        Multimap<LocalDate, PullRequest> closedPullRequests = LinkedHashMultimap.create();  // to collect closed PR's of Last 7 days.

        var dateTimeNow = LocalDateTime.now();
        var oneWeekBeforeDateTime = LocalDate.now().minusDays(7).atStartOfDay();
        var twoWeekBeforeDateTime = oneWeekBeforeDateTime.minusDays(7);

        var predicate = builder.between(root.get(CREATED_AT), twoWeekBeforeDateTime, dateTimeNow);
        predicate = builder.and(builder.equal(root.get(Constants.CLIENT_ID), SecurityUtil.getClientId()), predicate);
        query.select(root).where(predicate);  // get PR's created from starting of LastWeek till now.
        var openPrsFromLastTwoWeek = entityManager.createQuery(query).getResultList();

        openPrsFromLastTwoWeek.forEach(pullRequest -> {
            var createdAt = pullRequest.getCreatedAt();
            if (DateUtil.isBefore(createdAt, oneWeekBeforeDateTime)) {
                if (pullRequest.getStatus().equals(Status.OPEN) || pullRequest.getStatus().equals(Status.REOPENED)) {
                    openPrsLastWeek.add(pullRequest);
                } else {
                    closePrsLastWeek.add(pullRequest);
                }
                totalPrsLastWeek.add(pullRequest);
            } else {
                if (pullRequest.getStatus().equals(Status.OPEN) || pullRequest.getStatus().equals(Status.REOPENED)) {
                    openPrsCurrentWeek.add(pullRequest);
                    openPullRequests.put(createdAt.toLocalDate(), pullRequest);
                } else {
                    closePrsCurrentWeek.add(pullRequest);
                    closedPullRequests.put(createdAt.toLocalDate(), pullRequest);
                }
                totalPrsCurrentWeek.add(pullRequest);
            }
        });

        for (int i=0; i<7; i++) {
            var date = LocalDate.now().minusDays(i);
            var GraphMetrics = getGraphMetrics(openPullRequests.get(date), closedPullRequests.get(date), sla.getSlaTime());
            openCloseGraph.put(LocalDate.now().minusDays(i).toString(), GraphMetrics.get("openCloseGraph"));
            slaMeanGraph.put(LocalDate.now().minusDays(i).toString(), GraphMetrics.get("slaMeanGraph"));
        }

        var openCountCurrentWeek = openPrsCurrentWeek.size();
        var openCountLastWeek = openPrsLastWeek.size();
        var openPrPercent = openCountLastWeek > 0 ? (((openCountCurrentWeek - openCountLastWeek)*100)/openCountLastWeek) 
                                                  : (openCountCurrentWeek > 0 ? 100 : 0);

        var closeCountCurrentWeek = closePrsCurrentWeek.size();
        var closeCountLastWeek = closePrsLastWeek.size();
        var closePrPercent = closeCountLastWeek > 0 ? (((closeCountCurrentWeek - closeCountLastWeek)*100)/closeCountLastWeek) 
                                                    : (closeCountCurrentWeek > 0 ? 100 : 0);

        var totalCountCurrentWeek = totalPrsCurrentWeek.size();
        var totalCountLastWeek = totalPrsLastWeek.size();
        var totalPrPercent = totalCountLastWeek > 0 ? (((totalCountCurrentWeek - totalCountLastWeek)*100)/totalCountLastWeek) 
                                                    : (totalCountCurrentWeek > 0 ? 100 : 0);

        var slaTimeExcededCurrentWeek = getSlaMeanCount(openPrsCurrentWeek, sla.getSlaTime()).get("slaTimeExceded");
        var slaTimeExcededLastWeek = getSlaMeanCount(openPrsLastWeek, sla.getSlaTime()).get("slaTimeExceded");
        var slaTimeExcededPercent = slaTimeExcededLastWeek > 0 ? (((slaTimeExcededCurrentWeek - slaTimeExcededLastWeek)*100)/slaTimeExcededLastWeek) 
                                                               : (slaTimeExcededCurrentWeek > 0 ? 100 : 0);
        return DashboardDto.builder().openCloseGraph(openCloseGraph).slaMeanGraph(slaMeanGraph)
                                     .openPrsCount(openCountCurrentWeek).openPrPercent(openPrPercent)
                                     .closePrsCount(closeCountCurrentWeek).closePrPercent(closePrPercent)
                                     .totalPrsCount(totalCountCurrentWeek).totalPrPercent(totalPrPercent)
                                     .slaMiss(slaTimeExcededCurrentWeek).slaMissPercent(slaTimeExcededPercent)
                                     .build();

    }

    public Map<String, Integer> getSlaMeanCount(List<PullRequest> pullRequests, long slaTime) {
        int slaTimeExceded = 0;
        int slaTimeNotExceded = 0;
        for (var pullRequest : pullRequests) {
            if (pullRequest.getStatus().equals(Status.CLOSED) || pullRequest.getStatus().equals(Status.MERGED)) {
                if (DateUtil.secondsBetweenDate(pullRequest.getCreatedAt(), pullRequest.getUpdatedAt()) > slaTime) slaTimeExceded++;
                else slaTimeNotExceded++;
            }
            else if (DateUtil.secondsBetweenDate(pullRequest.getCreatedAt(), LocalDateTime.now()) > slaTime) slaTimeExceded++;
            else slaTimeNotExceded++;
        }
        return Map.of("slaTimeExceded", slaTimeExceded, "slaTimeNotExceded", slaTimeNotExceded);
    }

    private Map<String, Object> getGraphMetrics(Collection<PullRequest> openPullRequests, Collection<PullRequest> closePullRequests, long slaTime) {
        var slaMeanGraph = getSlaMeanCount(new ArrayList<>(openPullRequests), slaTime);
        var openCloseGraph = Map.of("open", openPullRequests.size(), "close", closePullRequests.size());
        return Map.of("openCloseGraph", openCloseGraph, "slaMeanGraph", slaMeanGraph);
    }
}
