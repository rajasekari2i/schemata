package com.opsbeach.connect.core.specification;

import java.time.LocalDateTime;
import java.util.List;

import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.EventAudit;
import com.opsbeach.connect.github.entity.PullRequest;
import com.opsbeach.sharedlib.security.SecurityUtil;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Specification for querying database.
 * </p>
 */
@Component
public class IdSpecifications<T> {

    private static final String CLIENT_ID = "clientId";
    private static final String INCIDENT_CREATED_AT = "incidentCreatedAt";
    private static final String METRICS_CREATED_AT = "metricsCreatedAt";
    private static final String RESOLVED_AT = "resolvedAt";
    private static final String STATUS = "status";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String NAME_SPACE = "nameSpace";
    private static final String DELETED = "isDeleted";
    private static final String SERVICE_TYPE = "serviceType";
    private static final String TASK_TYPE = "taskType";
    private static final String KEY = "key";
    private static final String CHANNEL_ORIGIN = "channelOrigin";
    private static final String MESSAGE_TS = "messageTs";
    private static final String FROM_MESSAGE_ID = "fromMessageId";
    private static final String SOURCE_ID = "sourceId";
    private static final String TICKET_ID = "ticketId";
    private static final String UPDATED_AT = "updatedAt";
    private static final String INCIDENT_ID = "incidentId";
    private static final String CANONICAL_ID = "canonicalId";
    private static final String DOMAIN_ID = "domainId";
    private static final String WORKFLOW_ID = "workflowId";
    private static final String CLIENT_REPO_ID = "clientRepoId";
    private static final String FULL_NAME = "fullName";
    private static final String PATH = "path";
    private static final String NUMBER = "number";
    private static final String FILE_TYPE = "fileType";
    private static final String FILE_NAME = "fileName";
    private static final String TYPE = "type";
    private static final String NODE_ID = "nodeId";
    private static final String SCHEMA_FILE_AUDIT_ID = "schemaFileAuditId";
    private static final String CREATED_AT = "createdAt";
    private static final String PULL_REQUEST_ID = "pullRequestId";

    public Specification<T> findById(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(ID), id);
    }

    public Specification<T> findByClientId() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(CLIENT_ID), SecurityUtil.getClientId());
    }

    public Specification<T> findByClientId(Long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(CLIENT_ID), id);
    }

    public Specification<T> greaterThanIncidentCreatedAt(LocalDateTime date) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(INCIDENT_CREATED_AT), date);
    }

    public Specification<T> greaterThanIncidentMetricsCreatedAt(LocalDateTime date) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(METRICS_CREATED_AT), date);
    }

    public Specification<T> statusNotEqualTo(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(STATUS), name);
    }

    public Specification<T> statusEqualTo(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(STATUS), name);
    }

    public Specification<T> resolvedIsNull() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.isNull(root.get(RESOLVED_AT));
    }

    public Specification<T> resolvedIsNotNull() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get(RESOLVED_AT));
    }

    public Specification<T> findByName(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(NAME), name);
    }

    public Specification<T> findByNameSpace(String nameSpace) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(NAME_SPACE), nameSpace);
    }

    public Specification<T> findByDeleted(Boolean isDeleted) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(DELETED), isDeleted);
    }

    public Specification<T> findByServiceType(ServiceType serviceType) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(SERVICE_TYPE), serviceType);
    }

    public Specification<T> findByServiceTypes(List<ServiceType> serviceTypes) {
        return (root, criteriaQuery, criteriaBuilder) -> root.get(SERVICE_TYPE).in(serviceTypes);
    }

    public Specification<T> findByTaskType(TaskType taskType) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(TASK_TYPE), taskType);
    }

    public Specification<T> findByKey(String key) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(KEY), key);
    }

    public Specification<T> findByChannelOrigin(String channelOrigin) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(CHANNEL_ORIGIN), channelOrigin);
    }

    public Specification<T> findByMessageTs(String messageTs) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(MESSAGE_TS), messageTs);
    }

    public Specification<T> findByFromMessageId(Long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(FROM_MESSAGE_ID), id);
    }

    public Specification<T> findMetricsBySourceId(String id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(SOURCE_ID), id);
    }

    public Specification<T> findTicketAuditByListOfTicketId(List<Long> ids) {
        return (root, criteriaQuery, criteriaBuilder) -> root.get(TICKET_ID).in(ids);
    }

    public Specification<T> greaterThanUpdatedAt(LocalDateTime updatedAt) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(UPDATED_AT), updatedAt);
    }

    public Specification<T> findIncidentLogEntryByIncidentId(List<String> ids) {
        return (root, criteriaQuery, criteriaBuilder) -> root.get(INCIDENT_ID).in(ids);
    }

    public Specification<T> findByTicketId(String id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(TICKET_ID), id);
    }

    public Specification<T> findTicketByCanonicalId(String id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(CANONICAL_ID), id);
    }

    public Specification<T> findByDomainId(Long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(DOMAIN_ID), id);
    }

    public Specification<T> findByWorkflowId(Long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(WORKFLOW_ID), id);
    }

    public Specification<T> findByClientRepoId(Long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(CLIENT_REPO_ID), id);
    }

    public Specification<T> findByClientRepoIds(List<Long> ids) {
        return (root, criteriaQuery, criteriaBuilder) -> root.get(CLIENT_REPO_ID).in(ids);
    }

    public Specification<T> findByClientRepoStatus(ClientRepo.Status status) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(STATUS), status);
    }

    public Specification<T> findByFullName(String name) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(FULL_NAME), name);
    }

    public Specification<T> findByPath(String path) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(PATH), path);
    }

    public Specification<T> findByNumber(String number) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(NUMBER), number);
    }

    public Specification<T> findByFileType(String fileType) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(FILE_TYPE), fileType);
    }

    public Specification<T> findByFileName(String fileName) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(FILE_NAME), fileName);
    }

    public Specification<T> findByType(EventAudit.Type type) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(TYPE), type);
    }

    public Specification<T> findByNodeId(Long nodeId) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(NODE_ID), nodeId);
    }

    public Specification<T> findByPullRequestStatus(PullRequest.Status status) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(STATUS), status);
    }

    public Specification<T> findBySchemaFileAudit(Long schemaFileAuditId) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(SCHEMA_FILE_AUDIT_ID), schemaFileAuditId);
    }

    public Specification<T> greaterThanCreatedAt(LocalDateTime dateTime) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(CREATED_AT), dateTime);
    }

    public Specification<T> lessThanCreatedAt(LocalDateTime dateTime) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(CREATED_AT), dateTime);
    }

    public Specification<T> findBySlaType(ServiceType serviceType) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(TYPE), serviceType);
    }

    public Specification<T> findByPullRequest(Long prId) {
        if (prId == null) {
            return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.isNull(root.get(PULL_REQUEST_ID));
        }
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get(PULL_REQUEST_ID), prId);
    }

    public Specification<T> workflowIsNotNull() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get(WORKFLOW_ID));
    }
}
