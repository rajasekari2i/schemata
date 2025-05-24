package com.opsbeach.connect.github.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.stereotype.Service;

import com.google.cloud.tasks.v2.HttpRequest;
import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.github.dto.EventAuditDto;
import com.opsbeach.connect.github.entity.EventAudit;
import com.opsbeach.connect.github.repository.EventAuditRepository;
import com.opsbeach.connect.schemata.processor.protobuf.ProtoSchema;
import com.opsbeach.connect.schemata.validate.Status;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.GoogleCloudException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.service.GoogleCloudService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventAuditService {
    
    private final EventAuditRepository eventAuditRepository;

    private final ResponseMessage responseMessage;

    private final IdSpecifications<EventAudit> eventAuditSpecifications;

    private final GoogleCloudService googleCloudService;

    @Lazy
    @Autowired
    private ProtoSchema protoSchema;

    @Lazy
    @Autowired
    private ClientRepoService clientRepoService;

    @Lazy
    @Autowired
    private GitHubService gitHubService;

    @Value("${github.process_event_audit}")
    private String processEventAuditUrl;

    public EventAuditDto add(EventAuditDto eventAuditDto) {
        var eventAudit = addModel(eventAuditDto.toDomain(eventAuditDto));
        return eventAudit.toDto(eventAudit);
    }

    public EventAudit addModel(EventAudit eventAudit) {
        return eventAuditRepository.save(eventAudit);
        
    }

    public EventAuditDto get(Long id) {
        var eventAudit = getModel(id);
        return eventAudit.toDto(eventAudit);
    }

    public EventAudit getModel(Long id) {
        return eventAuditRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.EVENT_AUDIT)));
    }

    public List<EventAuditDto> getAll() {
        var eventAudits = eventAuditRepository.findAll();
        return eventAudits.isEmpty() ? List.of() : eventAudits.stream().map(eventAudits.get(0)::toDto).toList();
    }

    public List<EventAuditDto> getInitialLoadStatus() {
        var eventAudits = eventAuditRepository.findAll(eventAuditSpecifications.findByType(EventAudit.Type.REPOSITORY_INITIAL_PULL));
        return eventAudits.isEmpty() ? List.of() : eventAudits.stream().map(eventAudits.get(0)::toDto).toList();
    }

    public List<EventAudit> addAll(List<EventAudit> eventAudits) {
        return eventAuditRepository.saveAll(eventAudits);
    }

    public EventAuditDto updateStatus(Long id, EventAudit.Status status) {
        var eventAudit = getModel(id);
        eventAudit = updateStatus(eventAudit, status);
        return eventAudit.toDto(eventAudit);
    }

    public EventAudit updateStatus(EventAudit eventAudit, EventAudit.Status status) {
        eventAudit.setStatus(status);
        if (EventAudit.Status.COMPLETED.equals(status)) eventAudit.setError(null);
        return eventAuditRepository.save(eventAudit);
    }

    public String pushEventAuditIdToTask(List<Long> eventAuditIds) {
        String url = processEventAuditUrl;
        try {
            List<HttpRequest> requests = new ArrayList<>(eventAuditIds.size());
            for (Long eventAuditId : eventAuditIds) {
                log.info("Creating Request for Event Audit: " + eventAuditId);
                // Construct the task body.
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put(HttpHeaders.AUTHORIZATION, AuthType.BEARER.getKey() + " " + SecurityUtil.getAccessToken());
                headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                HttpRequest request = HttpRequest.newBuilder()
                        .setHttpMethod(com.google.cloud.tasks.v2.HttpMethod.POST)
                        .setUrl(url.replace("{eventAuditId}", eventAuditId.toString()))
                        .putAllHeaders(headerMap)
                        .build();
                requests.add(request);
            }
            // Instead of using google cloud queue need to use inbuild local Job scheduling mechanism.
            googleCloudService.pushRequestInTask(requests);
        } catch (IOException e) {
            throw new GoogleCloudException(ErrorCode.CLOUD_TASK_CREATION_ERROR, responseMessage.getErrorMessage(ErrorCode.CLOUD_TASK_CREATION_ERROR, e.getMessage()));
        }
        return Status.SUCCESS.name();
    }

    public boolean processEventAudit(Long eventAuditId) {
        var eventAudit = getModel(eventAuditId);
        log.info("CALLED SUCCESSFULLY FROM TASK OF EVENT ID ="+eventAuditId.toString());
        SecurityUtil.setClientId(eventAudit.getClientId());
        if (eventAudit.getStatus().equals(EventAudit.Status.COMPLETED)) throw new AlreadyBuiltException("EVENT_AUDIT ALREADY PROCESSED");
        updateStatus(eventAudit, EventAudit.Status.IN_PROGRESS);
        LocalDateTime localDateTime = LocalDateTime.now();
        log.info("START TIME FOR PROCESSING EVENT OF ID - "+eventAuditId.toString()+" is = " +localDateTime.toString());
        try {
            switch (eventAudit.getType()) {
                case REPOSITORY_INITIAL_PULL -> clientRepoService.initialLoading(eventAudit);
                case CSV_FILE_UPLOAD -> log.info("IN PROGRESS");
            }
        } catch (Exception e) {
            eventAudit.setError(e.getMessage());
            eventAudit.setStatus(EventAudit.Status.ERROR);
            eventAuditRepository.save(eventAudit);
            // if (eventAudit.getType().equals(EventAudit.Type.REPOSITORY_INITIAL_PULL))
                clientRepoService.rollBackRecordsCreatedById(eventAudit.getEventId());
            log.info("OOPS.. An error occured while processign EventAudit ID -"+eventAuditId);
            e.printStackTrace();
            return true;
        }
        log.info("PROCESSING OF EVENT OF ID - "+eventAuditId+" IS COMPLETED, TIME TAKEN = "+ ChronoUnit.SECONDS.between(localDateTime, LocalDateTime.now()));
        return true;
    }
}
