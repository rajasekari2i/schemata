package com.opsbeach.connect.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.github.dto.AutoCompleteModelDto;
import com.opsbeach.connect.github.dto.DomainDto;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.Domain;
import com.opsbeach.connect.github.entity.Model;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.repository.ModelRepository;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class ModelServiceTest {
    
    @InjectMocks
    private ModelService modelService;
    @Mock
    private ModelRepository modelRepository;
    @Spy
    private IdSpecifications<Model> modelSpecifications;
    @Mock
    private ClientRepoService clientRepoService;
    @Mock
    private DomainService domainService;
    @Mock
    private TableService tableService;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<Model> criteriaQuery;
    @Mock
    private Root<Model> root;
    @Mock
    private TypedQuery<Model> typedQuery;
    @Mock
    private EntityManager entityManager;
    @Mock
    private Expression<String> expression;
    @Mock
    private CriteriaUpdate<Model> criteriaUpdate;
    @Mock
    private CriteriaDelete<Model> criteriaDelete;
    @Mock
    private ResponseMessage responseMessage;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private List<Model> getModels() {
        return List.of(Model.builder().name("model").clientRepoId(1L).build());
    }

    private void mockEntityManager() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Model.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Model.class)).thenReturn(root);

        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);
    }

    private void mockApplicationUser() {
        UserDto userDto = mock(UserDto.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDto);
    }

    @Test
    public void addAllTest() {
        var models = getModels();
            when(modelRepository.saveAll(ArgumentMatchers.<List<Model>>any())).thenReturn(models);
        var response = modelService.addAll(models);
        assertEquals(models.get(0).getName(), response.get(0).getName());
    }

    @Test
    public void getAllTest() {
        var models = getModels();
            when(modelRepository.findAll(ArgumentMatchers.<Specification<Model>>any())).thenReturn(models);
            when(clientRepoService.getActiveRepoIds()).thenReturn(List.of(1L));
        var response = modelService.getAll(null, null, null);
        assertEquals(models.get(0).getName(), response.get(0).getName());
            
            when(modelRepository.findAll(ArgumentMatchers.<Specification<Model>>any())).thenReturn(List.of());
        response = modelService.getAll(1L, 2L, "path");
        assertEquals(0, response.size());
    }

    @Test
    public void findBySchemaFileAuditTest() {
        var models = getModels();
            when(modelRepository.findAll(ArgumentMatchers.<Specification<Model>>any())).thenReturn(models);
        var response = modelService.findBySchemaFileAudit(1L);
        assertEquals(models.get(0).getName(), response.get(0).getName());
    }

    @Test
    public void addModelTest() {
        var model = getModels().get(0);
            when(modelRepository.save(any(Model.class))).thenReturn(model);
        var response = modelService.addModel(model);
        assertEquals(model.getName(), response.getName());
    }

    @Test
    public void getModelTest() {
        var model = getModels().get(0);
            when(modelRepository.findById(1L)).thenReturn(Optional.of(model));
        var response = modelService.getModel(1L);
        assertEquals(model.getName(), response.getName());

        assertThrows(RecordNotFoundException.class, () -> modelService.getModel(2L));
    }

    @Test
    public void getNodeIdsTest() {
        var models = getModels();
            when(modelRepository.findAll(ArgumentMatchers.<Specification<Model>>any())).thenReturn(models);
        var response = modelService.getNodeIds();
        assertEquals(models.get(0).getNodeId(), response.get(0));

            when(modelRepository.findAll(ArgumentMatchers.<Specification<Model>>any())).thenReturn(List.of());
        response = modelService.getNodeIds();
        assertEquals(0, response.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findByFullNames() {
        var models = getModels();
            mockEntityManager();
            Path<Object> path = mock(Path.class);
            when(root.get("name")).thenReturn(path);
            when(criteriaBuilder.concat(null, ".")).thenReturn(expression);
            when(criteriaBuilder.concat(any(Expression.class), any(Path.class))).thenReturn(expression);
            when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
            when(typedQuery.getResultList()).thenReturn(models);
        var response = modelService.findByFullNames(Set.of("names"), 1L);
        assertEquals(response.get(0).getName(), models.get(0).getName());

        response = modelService.findByFullNames(List.of("names"));
        assertEquals(response.get(0).getName(), models.get(0).getName());
    }

    @Test
    public void updateModelSetPrIdToNullTest() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createCriteriaUpdate(Model.class)).thenReturn(criteriaUpdate);
        when(criteriaUpdate.from(Model.class)).thenReturn(root);

        when(criteriaUpdate.set("pullRequestId", null)).thenReturn(criteriaUpdate);
        when(criteriaUpdate.where(any(Predicate.class))).thenReturn(criteriaUpdate);
        when(entityManager.createQuery(criteriaUpdate)).thenReturn(typedQuery);
        when(typedQuery.executeUpdate()).thenReturn(2);
        assertEquals(modelService.updateModelSetPrIdToNull(3L), 2);
    }

    @Test
    public void deleteModelByPrIdTest() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createCriteriaDelete(Model.class)).thenReturn(criteriaDelete);
        when(criteriaDelete.from(Model.class)).thenReturn(root);

        when(criteriaDelete.where(any(Predicate.class))).thenReturn(criteriaDelete);
        when(entityManager.createQuery(criteriaDelete)).thenReturn(typedQuery);
        when(typedQuery.executeUpdate()).thenReturn(2);
        assertEquals(modelService.deleteModelByPrId(3L), 2);
    }

    @Test
    public void findByNameLikeTest() {
        mockApplicationUser();
        var autoCompleteModelDto = new AutoCompleteModelDto() {
            @Override
            public Long getNodeId() {
                return 1L;
            }

            @Override
            public String getName() {
                return "name";
            }

            @Override
            public String getNameSpace() {
                return "nameSpace";
            }
        };
        when(modelRepository.findByNameLike(anyString(), anyLong(), anyString())).thenReturn(List.of(autoCompleteModelDto));
        var response = modelService.findByNameLike("name");
        assertEquals(response.get(0).getName(), autoCompleteModelDto.getName());
    }

    @Test
    public void getTableRepoTypeTest() {
        var models = getModels();
            when(modelRepository.findAll(ArgumentMatchers.<Specification<Model>>any())).thenReturn(models);
            when(clientRepoService.getModel(anyLong())).thenReturn(ClientRepo.builder().repoType(RepoType.AVRO).build());
        var response = modelService.getByNodeId(2L);
        assertEquals(response.getRepoType(), RepoType.AVRO);
    }

    @Test
    public void findModelByNameAndNameSpaceTest() {
        var models = getModels();
        when(modelRepository.findAll(ArgumentMatchers.<Specification<Model>>any())).thenReturn(models);
        var response = modelService.findModelByNameAndNameSpace("name", "nameSpace");
        assertEquals(response.get(0).getName(), models.get(0).getName());
    }

    @Test
    public void createModelsTest() {
        var schemaFileAudit = SchemaFileAudit.builder().id(1L).path("path").checksum("checksum").build();
        var domain = Domain.builder().id(1L).clientRepoId(2L).build();
        var table = Table.builder().id(1L).name("model").nameSpace("nameSpace").type("RECORD").build();
        when(modelRepository.saveAll(anyList())).thenReturn(getModels());
        var models = modelService.createModels(List.of(table), schemaFileAudit, domain);
        assertEquals(models.get(0).getName(), table.getName());
        assertNull(models.get(0).getPullRequestId());
    }

    @Test
    public void createModelTest() {
        var schemaFileAudit = SchemaFileAudit.builder().id(1L).path("path").checksum("checksum").build();
        var domain = DomainDto.builder().id(1L).clientRepoId(2L).build();
        var table = Table.builder().id(1L).name("model").nameSpace("nameSpace").type("RECORD").build();
            when(domainService.getAll(anyLong())).thenReturn(List.of(domain));
            when(modelRepository.save(any(Model.class))).thenReturn(getModels().get(0));
        var model = modelService.createModel(schemaFileAudit, 1L, table);
        assertEquals(model.getName(), table.getName());
    }

    @Test
    public void deleteAllByClientRepoIdTest() {
        modelService.deleteAllByClientRepoId(1L);
        var models = getModels();
            when(modelRepository.findAll(ArgumentMatchers.<Specification<Model>>any())).thenReturn(models);
            ReflectionTestUtils.setField(modelService, "tableService", tableService);
        modelService.deleteAllByClientRepoId(1L);
    }
}
