package com.opsbeach.connect.github.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.avro.SchemaParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.AlreadyBuiltException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.github.dto.ClientRepoDto;
import com.opsbeach.connect.github.dto.GitHubDto;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.EventAudit;
import com.opsbeach.connect.github.entity.ClientRepo.RepoSource;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.repository.ClientRepoRepository;
import com.opsbeach.connect.schemata.processor.avro.AvroSchema;
import com.opsbeach.connect.schemata.processor.json.JsonSchema;
import com.opsbeach.connect.schemata.processor.protobuf.ProtoSchema;
import com.opsbeach.connect.schemata.service.DomainNodeService;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.sharedlib.dto.ClientDto;
import com.opsbeach.sharedlib.exception.AlreadyExistException;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.ApplicationConfig;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.service.GoogleCloudService;
import com.opsbeach.sharedlib.utils.FileUtil;
import com.opsbeach.sharedlib.utils.StringUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class ClientRepoService {

    @Autowired
    private ClientRepoRepository clientRepoRepository;
    @Autowired
    private ResponseMessage responseMessage;
    @Autowired
    private EventAuditService eventAuditService;
    @Autowired
    private App2AppService app2AppService;
    @Autowired
    private IdSpecifications<ClientRepo> clientRepoSpecifications;
    @Autowired
    private DomainService domainService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ConnectService connectService;
    @Autowired
    private GoogleCloudService googleCloudService;
    @Autowired
    private ApplicationConfig applicationConfig;
    @Lazy @Autowired
    private GitHubService gitHubService;
    @Lazy @Autowired
    private AvroSchema avroSchema;
    @Lazy @Autowired
    private JsonSchema jsonSchema;
    @Lazy @Autowired
    private ProtoSchema protoSchema;
    @Lazy @Autowired
    private DomainNodeService domainNodeService;
    @Lazy @Autowired
    private SchemaFileAuditService schemaFileAuditService;
    @Lazy @Autowired
    private ModelService modelService;

    private static final String SCHEMATA = "schemata";

    @Value("${application.user.get-client-url}")
    private String getClientUrl;

    @Value("${server.home-path}")
    private String homePath;

    @Value("${server.repo-storage-path}")
    private String repoStoragePath;

    @Value("${application.user.update-onboard-status}")
    private String updateOnboardStatusUrl;

    public ClientRepo addModel(ClientRepo clientRepo) {
        return clientRepoRepository.save(clientRepo);
    }

    public ClientRepoDto get(Long id) {
        var clientRepo = getModel(id);
        return clientRepo.toDto(clientRepo);
    }

    public ClientRepo getModel(Long id) {
        return clientRepoRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.CLIENT_REPO)));
    }

    public List<ClientRepoDto> getAll() {
        var clientRepos = getAllModels();
        return clientRepos.isEmpty() ? List.of() : clientRepos.stream().map(clientRepos.get(0)::toDto).toList();
    }

    public List<ClientRepo> getAllModels() {
        return clientRepoRepository.findAll();
    }

    public RepoType[] getRepoTypes() {
        return RepoType.values();
    }

    public List<Long> getActiveRepoIds() {
        var specification = clientRepoSpecifications.findByClientRepoStatus(ClientRepo.Status.ACTIVE);
        var clientRepos = clientRepoRepository.findAll(specification);
        return clientRepos.isEmpty() ? List.of() : clientRepos.stream().map(ClientRepo::getId).toList();
    }

    public ClientRepoDto updateStatus(Long id, ClientRepo.Status status) {
        var clientRepo = getModel(id);
        clientRepo.setStatus(status);
        clientRepo = addModel(clientRepo);
        return clientRepo.toDto(clientRepo);
    }

    @Transactional
    public ClientRepoDto createNewRepo(GitHubDto gitHubDto) {
        boolean isRepoExists = true;
        try {
            gitHubService.getRepoDetails(gitHubDto.getRepoOwner(), gitHubDto.getPrivateRepoName(), gitHubDto.getConnectId());
        } catch (Exception e) {
            isRepoExists = false;
        }
        if (Boolean.TRUE.equals(isRepoExists)) {
            var repoFullName = gitHubDto.getRepoOwner() + "/" + gitHubDto.getPrivateRepoName();
            throw new AlreadyBuiltException(responseMessage.getErrorMessage(ErrorCode.ALREADY_EXISTS, repoFullName));
        }
        var response = gitHubService.createNewRepo(gitHubDto.getRepoOwner(), gitHubDto.getPrivateRepoName(), gitHubDto.getConnectId());
        var clientRepo = createClientRepo(response, gitHubDto.getConnectId());
        return clientRepo.toDto(clientRepo);
    }

    private ClientRepo createClientRepo(JsonNode response, Long connectId) {
        var clientRepo = ClientRepo.builder().owner(response.get("owner").get("login").asText())
                                   .name(response.get("name").asText()).fullName(response.get("full_name").asText())
                                   .connectId(connectId).status(ClientRepo.Status.ACTIVE).repoType(RepoType.JSON)
                                   .defaultBranch(response.get("default_branch").asText()).build();
        clientRepo = addModel(clientRepo);
        var clientDto = getClient();
        var domainNode = domainNodeService.addDomainNode(clientRepo.getFullName(), clientDto.getId(), clientRepo.getId());
        domainService.addDomain(clientRepo, domainNode.getId());
        return clientRepo;
    }

    @Transactional
    public String add(GitHubDto gitHubDto) {
        List<EventAudit> eventAudits = new LinkedList<>();
        var selectedRepos = gitHubDto.getSelectedRepos().keySet();
        for (String repo : selectedRepos) {
            if (findByFullName(gitHubDto.getRepoOwner()+"/"+repo).isEmpty()) {
                eventAudits.add(createEventAudit(repo, gitHubDto));
            } else {
                throw new AlreadyExistException(ErrorCode.ALREADY_EXISTS, responseMessage.getErrorMessage(ErrorCode.ALREADY_EXISTS, gitHubDto.getRepoOwner()+"/"+repo));
            }
        }
        eventAudits = eventAuditService.addAll(eventAudits);
        var ids = eventAudits.stream().map(EventAudit::getId).toList();
        eventAuditService.processEventAuditsAsync(ids);
        return "SUCCESS";
    }

    private EventAudit createEventAudit(String repo, GitHubDto gitHubDto) {
        var repoType = gitHubDto.getSelectedRepos().get(repo);
        var repoFullName = gitHubDto.getRepoOwner()+"/"+repo;
        var defaultBranch = gitHubService.getDefaultBranch(gitHubDto.getRepoOwner(), repo, gitHubDto.getConnectId());
        var clientRepo = ClientRepo.builder().owner(gitHubDto.getRepoOwner()).name(repo).fullName(repoFullName)
                                   .connectId(gitHubDto.getConnectId()).status(ClientRepo.Status.ACTIVE)
                                   .repoType(repoType).defaultBranch(defaultBranch).repositorySource(RepoSource.GITHUB).build();
        clientRepo = addModel(clientRepo);
        return EventAudit.builder().eventId(clientRepo.getId()).type(EventAudit.Type.REPOSITORY_INITIAL_PULL).clientName(getClient().getName())
                         .initiatedBy(gitHubDto.getRepoOwner()).status(EventAudit.Status.PENDING).build();
    }

    public String uploadRepo(MultipartFile repoMultipartFile, RepoType repoType) {
        var clientDto = getClient();
        var storagePath = repoStoragePath+"/"+clientDto.getName();
        File file = new File(storagePath);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        try {
            // Save file to disk
            Path filepath = Paths.get(storagePath, repoMultipartFile.getOriginalFilename());
            Files.write(filepath, repoMultipartFile.getBytes());
            log.info("Repo File loaded successfully: " + filepath.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalError("File not uploaded");
        }
        var repoName = FileUtil.getBaseFileName(repoMultipartFile.getOriginalFilename());
        var filepath = storagePath+"/"+repoMultipartFile.getOriginalFilename();
        var clientRepo = ClientRepo.builder().name(repoName).fullName(repoName).status(ClientRepo.Status.ACTIVE)
                                   .repoType(repoType).repositorySource(RepoSource.LOCAL).folderPath(filepath).build();
        clientRepo = addModel(clientRepo);
        var eventAudit = EventAudit.builder().eventId(clientRepo.getId()).type(EventAudit.Type.REPOSITORY_INITIAL_PULL).clientName(clientDto.getName())
                         .status(EventAudit.Status.PENDING).build();
        eventAuditService.addModel(eventAudit);
        eventAuditService.processEventAuditsAsync(List.of(eventAudit.getId()));
        return "SUCCESS";
    }

    private static final String FULL_NAME = "fullName";

    private Optional<ClientRepo> findByFullName(String fullName) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ClientRepo> query = builder.createQuery(ClientRepo.class);
        Root<ClientRepo> root = query.from(ClientRepo.class);

        query.select(root).where(builder.equal(root.get(FULL_NAME), fullName));
        var typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList().size() == 0 ? Optional.empty() : Optional.of(typedQuery.getSingleResult());
    }

    public ClientDto getClient() {
        return getClient(SecurityUtil.getClientId());
    }

    public ClientDto getClient(Long id) {
        var url = getClientUrl.replace("{id}", id.toString());
        var entity = app2AppService.setHeaders(App2AppService.authorizationHeader("Bearer " + SecurityUtil.getAccessToken()), null);
        var res = app2AppService.httpGet(url, entity, ClientDto.class);
        return res;
    }

    public ClientRepo getByFullName(String fullName) {
        var clientRepo =  findByFullName(fullName);
        return clientRepo.orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND, Constants.CLIENT_REPO)));
    }

    public Optional<ClientRepo> getSchemataRepo() {
        var clientRepo = clientRepoRepository.findOne(clientRepoSpecifications.findByName(SCHEMATA));
        if (clientRepo.isEmpty()) {
            var connectDto = connectService.get(ServiceType.GITHUB);
            if (Objects.isNull(connectDto.getRepoOrganization())) throw new RecordNotFoundException(ErrorCode.REPO_ORGANIZATION_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.REPO_ORGANIZATION_NOT_FOUND));
            JsonNode response;
            try {
                response = gitHubService.getRepoDetails(connectDto.getRepoOrganization(), SCHEMATA, connectDto);
            } catch (Exception e) {
                response = gitHubService.createNewRepo(connectDto.getRepoOrganization(), SCHEMATA, connectDto);
            }
            clientRepo = Optional.of(createClientRepo(response, connectDto.getId()));
        }
        return clientRepo;
    }

    public EventAudit initialLoading(EventAudit eventAudit) {
        var clientRepo = getModel(eventAudit.getEventId());
        switch (clientRepo.getRepositorySource()) {
            case GITHUB -> githubInitialLoading(eventAudit, clientRepo);
            case LOCAL -> localInitialLoading(eventAudit, clientRepo);
        }
        return eventAudit;
    }

    private EventAudit githubInitialLoading(EventAudit eventAudit, ClientRepo clientRepo) {
        var connectDto = connectService.get(clientRepo.getConnectId());
        gitHubService.downloadTarball(clientRepo, connectDto);
        var repoFolderPath = StringUtil.constructStringEmptySeparator(homePath, eventAudit.getInitiatedBy(), "-", clientRepo.getName());
        new File(repoFolderPath).mkdir();
        String objectName = StringUtil.constructStringEmptySeparator(eventAudit.getClientName(), "/", clientRepo.getFullName(), "/", clientRepo.getDefaultBranch());
        pullFilesFromBucket(applicationConfig.getGcloud().get("repo-bucket"), objectName, repoFolderPath);
        // NOW SEND THE ROOT FOLDER PATH TO RESPECTIVE SCHEMA FOR PARSING AND SAVING.
        try {
            switch (clientRepo.getRepoType()) {
                case AVRO -> avroSchema.parseFolder(repoFolderPath, clientRepo);
                case JSON -> jsonSchema.parseFolder(repoFolderPath, clientRepo);
                case PROTOBUF -> protoSchema.parseFolder(repoFolderPath, clientRepo);
                case YAML -> log.info("IN PROGRESS");
            }
        } catch (Exception e) {
            log.info("Error Occured While Parsing");
            FileUtil.deleteDirectory(repoFolderPath);
            throw new SchemaParseException(e.getMessage());
        }
        eventAuditService.updateStatus(eventAudit, EventAudit.Status.COMPLETED);
        log.info("Cleaning folder downloaded from BUCKET");
        FileUtil.deleteDirectory(repoFolderPath);
        updateOnboardStatus();
        return eventAudit;
    }

    private void localInitialLoading(EventAudit eventAudit, ClientRepo clientRepo) {
        var zipFilePath = clientRepo.getFolderPath();
        var repoFolderPath = zipFilePath.substring(0, zipFilePath.lastIndexOf('/'));
        log.info("UnZip the downloaded tar.gz file");
        FileUtil.unzip(zipFilePath, repoFolderPath);
        FileUtil.deleteFile(zipFilePath);
        clientRepo.setFolderPath(repoFolderPath);
        addModel(clientRepo);
        // NOW SEND THE ROOT FOLDER PATH TO RESPECTIVE SCHEMA FOR PARSING AND SAVING.
        try {
            switch (clientRepo.getRepoType()) {
                case AVRO -> avroSchema.parseFolder(repoFolderPath, clientRepo);
                case JSON -> jsonSchema.parseFolder(repoFolderPath, clientRepo);
                case PROTOBUF -> protoSchema.parseFolder(repoFolderPath, clientRepo);
                case YAML -> log.info("IN PROGRESS");
            }
        } catch (Exception e) {
            log.info("Error Occured While Parsing");
            throw new SchemaParseException(e.getMessage());
        }
        eventAuditService.updateStatus(eventAudit, EventAudit.Status.COMPLETED);
        log.info("Cleaning folder downloaded from BUCKET");
        updateOnboardStatus();
    }

    private void updateOnboardStatus() {
        var url = updateOnboardStatusUrl.replace("{id}", SecurityUtil.getClientId().toString())
                                        .replace("{isOnboarded}", Boolean.TRUE.toString());
        app2AppService.httpPut(url, app2AppService.setHeaders(Map.of(HttpHeaders.AUTHORIZATION, "Bearer "+SecurityUtil.getAccessToken()), null), JsonNode.class);
    }

    private boolean pullFilesFromBucket(String bucketName, String objectName, String repoFolderPath) {
        String destFilePath =  repoFolderPath + '/' + bucketName + ".tar.gz";
        log.info("pulling file from bucket");
        googleCloudService.downloadFile(bucketName, objectName, destFilePath);
        log.info("UnZip the downloaded tar.gz file");
        FileUtil.uncompressTarGZ(repoFolderPath, destFilePath);
        FileUtil.deleteFile(destFilePath);
        return true;
    }

    public void rollBackRecordsCreatedById(Long clientRepoId) {
        modelService.deleteAllByClientRepoId(clientRepoId);
        schemaFileAuditService.deleteAllByClientRepoId(clientRepoId);
        domainService.deleteByClientRepoId(clientRepoId);
        domainNodeService.deleteByClientRepoId(clientRepoId);
    }
}