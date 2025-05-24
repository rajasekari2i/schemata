package com.opsbeach.connect.github.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.github.dto.AutoCompleteModelDto;
import com.opsbeach.connect.github.dto.ModelDto;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Domain;
import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.repository.ModelRepository;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.SecurityUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModelService {
    
    private final ModelRepository modelRepository;
    private final IdSpecifications<Model> modelSpecifications;
    private final ResponseMessage responseMessage;
    private final ClientRepoService clientRepoService;
    private final EntityManager entityManager;
    private final DomainService domainService;
    @Lazy @Autowired
    private TableService tableService;

    private static final String NAME = "name";
    private static final String NAME_SPACE = "nameSpace";
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_REPO_ID = "clientRepoId";
    private static final String PULL_REQUEST_ID = "pullRequestId";

    public List<Model> addAll(List<Model> models) {
        return modelRepository.saveAll(models);
    }

    public List<ModelDto> getAll(Long domainId, Long clientRepoId, String path) {
        return toDtos(getAllModel(domainId, clientRepoId, path));
    }

    public List<Model> getAllModel(Long domainId, Long clientRepoId, String path) {
        Specification<Model> specification = Specification.where(null);
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(domainId))) {
            specification = specification.and(modelSpecifications.findByDomainId(domainId));
        }
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(clientRepoId))) {
            specification = specification.and(modelSpecifications.findByClientRepoIds(List.of(clientRepoId)));
        } else {
            specification = specification.and(modelSpecifications.findByClientRepoIds(clientRepoService.getActiveRepoIds()));
        }
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(path))) {
            specification = specification.and(modelSpecifications.findByPath(path));
        }
        return modelRepository.findAll(specification);
    }

    private List<ModelDto> toDtos(List<Model> models) {
        return models.isEmpty() ? List.of() : models.stream().map(models.get(0)::toDto).toList();
    }

    public List<Model> findByFullNames(Set<String> fullNames, Long clientRepoId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Model> query = criteriaBuilder.createQuery(Model.class);
        Root<Model> root = query.from(Model.class);

        Expression<String> fullName = criteriaBuilder.concat(criteriaBuilder.concat(root.get(NAME_SPACE), "."), root.get(NAME));
        Predicate clientIdPredicate = criteriaBuilder.equal(root.get(CLIENT_ID), SecurityUtil.getClientId());
        Predicate clientRepoIdPredicate = criteriaBuilder.equal(root.get(CLIENT_REPO_ID), clientRepoId);
        Predicate finalPredicate = criteriaBuilder.and(fullName.in(fullNames), criteriaBuilder.and(clientIdPredicate, clientRepoIdPredicate));

        query.select(root).where(finalPredicate);
        
        return entityManager.createQuery(query).getResultList();
    }

    public List<Model> findByFullNames(List<String> fullNames) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Model> query = criteriaBuilder.createQuery(Model.class);
        Root<Model> root = query.from(Model.class);

        Expression<String> fullName = criteriaBuilder.concat(criteriaBuilder.concat(root.get(NAME_SPACE), "."), root.get(NAME));
        Predicate clientIdPredicate = criteriaBuilder.equal(root.get(CLIENT_ID), SecurityUtil.getClientId());
        Predicate finalPredicate = criteriaBuilder.and(fullName.in(fullNames), clientIdPredicate);

        query.select(root).where(finalPredicate);
        
        return entityManager.createQuery(query).getResultList();
    }

    @Transactional
    public int updateModelSetPrIdToNull(Long prId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaUpdate<Model> update = criteriaBuilder.createCriteriaUpdate(Model.class);
        Root<Model> root = update.from(Model.class);

        update.set(PULL_REQUEST_ID, null).where(criteriaBuilder.equal(root.get(PULL_REQUEST_ID), prId));

        return entityManager.createQuery(update).executeUpdate();
    }

    @Transactional
    public int deleteModelByPrId(Long prId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaDelete<Model> delete = criteriaBuilder.createCriteriaDelete(Model.class);
        Root<Model> root = delete.from(Model.class);

        delete.where(criteriaBuilder.equal(root.get(PULL_REQUEST_ID), prId));

        return entityManager.createQuery(delete).executeUpdate();
    }

    public List<Model> findBySchemaFileAudit(Long schemaFileAuditId) {
        return modelRepository.findAll(modelSpecifications.findBySchemaFileAudit(schemaFileAuditId));
    }

    public Model addModel(Model model) {
        return modelRepository.save(model);
    }

    public Model getModel(Long id) {
        return modelRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.MODEL)));
    }

    public List<AutoCompleteModelDto> findByNameLike(String name) {
        return modelRepository.findByNameLike(name+"%", SecurityUtil.getClientId(), ClientRepo.Status.ACTIVE.name());
    }

    public List<Long> getNodeIds() {
        var activeRepoIds = clientRepoService.getActiveRepoIds();
        var models = modelRepository.findAll(modelSpecifications.findByClientRepoIds(activeRepoIds));
        return models.isEmpty() ? List.of() : models.stream().map(Model::getNodeId).toList();
    }

    public ClientRepo getByNodeId(Long nodeId) {
        var repoId = findByNodeId(nodeId).get(0).getClientRepoId();
        return clientRepoService.getModel(repoId);
    }

    public List<Model> findByNodeId(Long nodeId) {
        return modelRepository.findAll(modelSpecifications.findByNodeId(nodeId));
    }

    public List<Model> findModelByNameAndNameSpace(String nameSpace, String name) {
        var specification = modelSpecifications.findByName(name).and(modelSpecifications.findByNameSpace(nameSpace));
        return modelRepository.findAll(specification);
    }

    public List<Model> createModels(List<Table> tables, SchemaFileAudit schemaFileAudit, Domain domain) {
        List<Model> models = new ArrayList<>();
        tables.forEach(table -> models.add(createModel(table, schemaFileAudit, domain, null)));
        return modelRepository.saveAll(models);
    }

    public Model createModel(SchemaFileAudit schemaFileAudit, Long clientRepoId, Table table) {
        var domain = domainService.getAll(clientRepoId).get(0);
        var model = createModel(table, schemaFileAudit, domain.toDomain(domain), null);
        return addModel(model);
    }

    public Model createModel(Table table, SchemaFileAudit schemaFileAudit, Domain domain, Long prId) {
        return Model.builder().clientId(schemaFileAudit.getClientId())
                              .type(table.getType())
                              .name(table.getName())
                              .nameSpace(table.getNameSpace())
                              .schemaFileAuditId(schemaFileAudit.getId())
                              .path(schemaFileAudit.getPath())
                              .nodeId(table.getId())
                              .domainId(domain.getId())
                              .clientRepoId(domain.getClientRepoId())
                              .checksum(schemaFileAudit.getChecksum())
                              .pullRequestId(prId)
                              .build();
    }

    public void deleteAllByClientRepoId(Long clientRepoId) {
        var models = modelRepository.findAll(modelSpecifications.findByClientRepoId(clientRepoId));
        if (models.isEmpty()) return;
        var tableIds = models.stream().map(Model::getNodeId).toList();
        tableService.deleteByIds(tableIds);
        modelRepository.deleteAllByClientRepoId(clientRepoId);
    }
}
