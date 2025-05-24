package com.opsbeach.connect.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.avro.SchemaParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.github.dto.GitHubDto;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.EventAudit;
import com.opsbeach.connect.github.entity.ClientRepo.RepoSource;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.repository.ClientRepoRepository;
import com.opsbeach.connect.schemata.entity.DomainNode;
import com.opsbeach.connect.schemata.processor.avro.AvroSchema;
import com.opsbeach.connect.schemata.processor.json.JsonSchema;
import com.opsbeach.connect.schemata.processor.protobuf.ProtoSchema;
import com.opsbeach.connect.schemata.service.DomainNodeService;
import com.opsbeach.connect.schemata.validate.Status;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.sharedlib.dto.ClientDto;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.exception.AlreadyExistException;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.ApplicationConfig;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.service.GoogleCloudService;
import com.opsbeach.sharedlib.utils.StringUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class ClientRepoServiceTest {
    
    @InjectMocks
    private ClientRepoService clientRepoService;
    @Mock
    private ClientRepoRepository clientRepoRepository;
    @Mock
    private EventAuditService eventAuditService;
    @Mock
    private GitHubService gitHubService;
    @Mock
    private ResponseMessage responseMessage;
    @Mock
    private App2AppService app2AppService;
    @Spy
    private IdSpecifications<ClientRepo> cIdSpecifications;
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<ClientRepo> criteriaQuery;
    @Mock
    private Root<ClientRepo> root;
    @Mock
    private TypedQuery<ClientRepo> typedQuery;
    @Mock
    private EntityManager entityManager;
    @Mock
    private DomainNodeService domainNodeService;
    @Mock
    private DomainService domainService;
    @Mock
    private ConnectService connectService;
    @Mock
    private ApplicationConfig applicationConfig;
    @Mock
    private GoogleCloudService googleCloudService;
    @Mock
    private AvroSchema avroSchema;
    @Mock
    private JsonSchema jsonSchema;
    @Mock
    private ProtoSchema protoSchema;
    private Map<String, String> gcloud;
    private Object homePath;
    @Mock
    private ModelService modelService;
    @Mock
    private SchemaFileAuditService schemaFileAuditService;
    
    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @BeforeEach
    public void init() throws StreamReadException, DatabindException, IOException {
        InputStream inputStream = new FileInputStream(new File("src/test/resources/application-test.yaml"));
        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> data = yaml.load(inputStream);
        gcloud = data.get("gcloud");
        homePath = data.get("home-path");
    }

    private void mockApplicationUser() {
        UserDto userDto = mock(UserDto.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDto);
    }

    private ClientRepo createClientRepo() {
        return ClientRepo.builder().id(1L).name("Table").fullName("full_name").build();
    }

    @Test
    public void getTest() {
        var clientRepo = createClientRepo();
            when(clientRepoRepository.findById(clientRepo.getId())).thenReturn(Optional.of(clientRepo));
        var response = clientRepoService.get(clientRepo.getId());
        assertEquals(response.getName(), clientRepo.getName());

            when(clientRepoRepository.findById(clientRepo.getId())).thenThrow(RecordNotFoundException.class);
        assertThrows(RecordNotFoundException.class, () -> clientRepoService.get(clientRepo.getId()));
    }

    @Test
    public void getAllTest() {
        assertEquals(0, clientRepoService.getAll().size());

        var clientRepo = createClientRepo();
            when(clientRepoRepository.findAll()).thenReturn(List.of(clientRepo));
        var response = clientRepoService.getAll();
        assertEquals(response.get(0).getName(), clientRepo.getName());
    }
    
    private GitHubDto createGitHubDto() {
        var selectedRepos = Map.of("Selected Repo", RepoType.AVRO);
        return GitHubDto.builder().connectId(1L).selectedRepos(selectedRepos)
                        .repos(List.of("opsconnect")).repoOwner("opsbeach").build();
    }

    @Test
    public void addTest() {
        var clientRepo = createClientRepo();
            mockEntityManager();
            when(typedQuery.getResultList()).thenReturn(List.of(clientRepo));
            when(typedQuery.getSingleResult()).thenReturn(clientRepo);
            when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        assertThrows(AlreadyExistException.class, () -> clientRepoService.add(createGitHubDto()));

            when(typedQuery.getResultList()).thenReturn(List.of());
            when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
            when(clientRepoRepository.save(any(ClientRepo.class))).thenReturn(clientRepo);
        var eventAudit = EventAudit.builder().id(1L).build();
        mockApplicationUser();
        ReflectionTestUtils.setField(clientRepoService, "getClientUrl", "clientUrl");
            when(app2AppService.setHeaders(anyMap(), eq(null))).thenReturn(null);
            when(app2AppService.httpGet(anyString(), eq(null), eq(ClientDto.class))).thenReturn(ClientDto.builder().name("client").build());
            when(eventAuditService.addAll(ArgumentMatchers.<List<EventAudit>>any())).thenReturn(List.of(eventAudit));
        var response = clientRepoService.add(createGitHubDto());
        assertEquals(response, Status.SUCCESS.name());
    }

    private void mockEntityManager() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(ClientRepo.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(ClientRepo.class)).thenReturn(root);

        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(criteriaQuery.where(any(Predicate.class))).thenReturn(criteriaQuery);
    }

    @Test
    public void getByFullNameTest() {
        var clientRepo = createClientRepo();
            mockEntityManager();
            when(typedQuery.getResultList()).thenReturn(List.of(clientRepo));
            when(typedQuery.getSingleResult()).thenReturn(clientRepo);

            when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
            var response = clientRepoService.getByFullName("full_name");
        assertEquals(response.getName(), clientRepo.getName());

            when(typedQuery.getResultList()).thenReturn(List.of());
        assertThrows(RecordNotFoundException.class, () -> clientRepoService.getByFullName("full_name"));
    }

    @Test
    public void getActiveRepoIdsTest() {
        var clientRepo = createClientRepo();
            when(clientRepoRepository.findAll(ArgumentMatchers.<Specification<ClientRepo>>any())).thenReturn(List.of(clientRepo));
        var response = clientRepoService.getActiveRepoIds();
        assertEquals(response.get(0), clientRepo.getId());

        when(clientRepoRepository.findAll(ArgumentMatchers.<Specification<ClientRepo>>any())).thenReturn(List.of());
        response = clientRepoService.getActiveRepoIds();
        assertEquals(response.size(), 0);
    }

    @Test
    public void updateStatusTest() {
        var clientRepo = createClientRepo();
            when(clientRepoRepository.findById(anyLong())).thenReturn(Optional.of(clientRepo));
            when(clientRepoRepository.save(any(ClientRepo.class))).thenReturn(clientRepo);
        var response = clientRepoService.updateStatus(1L, ClientRepo.Status.ACTIVE);
        assertEquals(response.getName(), clientRepo.getName());
    }

    @Test
    public void getRepoTypesTest() {
        assertTrue(Arrays.equals(RepoType.values(), clientRepoService.getRepoTypes()));
    }
    
    private ClientRepo getClientRepo() {
        return ClientRepo.builder().id(1L).name("name").fullName("fullName").defaultBranch("default_branch").build();
    }

    private void mockCreateClientRepoMethod(ClientRepo clientRepo) {
        when(clientRepoRepository.save(any(ClientRepo.class))).thenReturn(clientRepo);
        mockApplicationUser();
        ReflectionTestUtils.setField(clientRepoService, "getClientUrl", "{id}");
        when(app2AppService.setHeaders(anyMap(), eq(null))).thenReturn(null);
        when(app2AppService.httpGet(anyString(), eq(null), eq(ClientDto.class))).thenReturn(ClientDto.builder().id(1L).build());
        when(domainNodeService.addDomainNode(anyString(), anyLong(), anyLong())).thenReturn(DomainNode.builder().id(1L).build());
    }

    @Test
    public void createNewRepoTest() {
        var gitHubDto = GitHubDto.builder().repoOwner("owner").privateRepoName("repo").connectId(1L).build();
        assertThrows(AlreadyBuiltException.class , () -> clientRepoService.createNewRepo(gitHubDto));
            when(gitHubService.getRepoDetails(anyString(), anyString(), anyLong())).thenThrow(RecordNotFoundException.class);
        var newRepo = JsonNodeFactory.instance.objectNode();
        newRepo.put("name", "name").put("full_name", "fullName").put("default_branch", "default_branch");
        newRepo.putObject("owner").put("login", "owner");
            when(gitHubService.createNewRepo(anyString(), anyString(), anyLong())).thenReturn(newRepo);
        var clientRepo = getClientRepo();
            mockCreateClientRepoMethod(clientRepo);
        var response = clientRepoService.createNewRepo(gitHubDto);
        assertEquals(clientRepo.getName(), response.getName());
    }

    @Test
    public void getSchemataRepoTest() {
        var clientRepo = getClientRepo();
            when(clientRepoRepository.findOne(ArgumentMatchers.<Specification<ClientRepo>>any())).thenReturn(Optional.of(clientRepo));
        var response = clientRepoService.getSchemataRepo();
        assertEquals(clientRepo.getName(), response.get().getName());

            when(clientRepoRepository.findOne(ArgumentMatchers.<Specification<ClientRepo>>any())).thenReturn(Optional.empty());
        var connectDto = ConnectDto.builder().id(1L).serviceType(ServiceType.GITHUB).build();
            when(connectService.get(ServiceType.GITHUB)).thenReturn(connectDto);
        assertThrows(RecordNotFoundException.class, () -> clientRepoService.getSchemataRepo());

        connectDto = ConnectDto.builder().id(1L).serviceType(ServiceType.GITHUB).repoOrganization("org").build();
            when(connectService.get(ServiceType.GITHUB)).thenReturn(connectDto);
        var newRepo = JsonNodeFactory.instance.objectNode();
        newRepo.put("name", "name").put("full_name", "fullName").put("default_branch", "default_branch");
        newRepo.putObject("owner").put("login", "owner");
            when(gitHubService.getRepoDetails(anyString(), anyString(), any(ConnectDto.class))).thenReturn(newRepo);
            mockCreateClientRepoMethod(clientRepo);
        response = clientRepoService.getSchemataRepo();
        assertEquals(clientRepo.getName(), response.get().getName());

            when(gitHubService.getRepoDetails(anyString(), anyString(), any(ConnectDto.class))).thenThrow(RecordNotFoundException.class);
            when(gitHubService.createNewRepo(anyString(), anyString(), any(ConnectDto.class))).thenReturn(newRepo);
        response = clientRepoService.getSchemataRepo();
        assertEquals(clientRepo.getName(), response.get().getName());
    }

    @Test
    public void initialLoadingTest() throws IOException {
        var eventAudit = EventAudit.builder().id(1L).eventId(1L).initiatedBy("initiatedBy")
                                   .clientName("clientName").clientId(1L).build();
        mockClientRepoAndCreateZipFile(RepoType.AVRO, eventAudit);
            mockApplicationUser();
            when(applicationConfig.getGcloud()).thenReturn(gcloud);
            ReflectionTestUtils.setField(clientRepoService, "homePath", homePath);
            ReflectionTestUtils.setField(clientRepoService, "updateOnboardStatusUrl", "updateOnboardStatusUrl");

        var response = clientRepoService.initialLoading(eventAudit);
        assertEquals(response.getId(), eventAudit.getId());

        mockClientRepoAndCreateZipFile(RepoType.JSON, eventAudit);
        response = clientRepoService.initialLoading(eventAudit);
        assertEquals(response.getId(), eventAudit.getId());

        mockClientRepoAndCreateZipFile(RepoType.PROTOBUF, eventAudit);
        response = clientRepoService.initialLoading(eventAudit);
        assertEquals(response.getId(), eventAudit.getId());

        mockClientRepoAndCreateZipFile(RepoType.YAML, eventAudit);
        response = clientRepoService.initialLoading(eventAudit);
        assertEquals(response.getId(), eventAudit.getId());

        mockClientRepoAndCreateZipFile(null, eventAudit);
        assertThrows(SchemaParseException.class, () -> clientRepoService.initialLoading(eventAudit));
    }

    private void mockClientRepoAndCreateZipFile(RepoType repoType, EventAudit eventAudit) throws IOException {
        var clientRepo = ClientRepo.builder().id(1L).connectId(1L).fullName("fullName").name("name")
                                   .repoType(repoType).defaultBranch("defaultBranch").repositorySource(RepoSource.GITHUB).build();
            when(clientRepoRepository.findById(clientRepo.getId())).thenReturn(Optional.of(clientRepo));
        var repoFolderPath = StringUtil.constructStringEmptySeparator(homePath.toString(), eventAudit.getInitiatedBy(), "-", clientRepo.getName());
        new File(repoFolderPath).mkdir();
        Files.copy(Paths.get("src/test/resources/sampleRepo/repo-bucket.tar.gz"), Paths.get(repoFolderPath.concat("/repo-bucket.tar.gz")), StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    public void getModelTest() {
        assertThrows(RecordNotFoundException.class, () -> clientRepoService.getModel(1L));
    }

    @Test
    public void rollBackRecordsCreatedByIdTest() {
        clientRepoService.rollBackRecordsCreatedById(1L);
    }
}
