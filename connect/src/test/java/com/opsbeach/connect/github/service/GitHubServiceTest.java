package com.opsbeach.connect.github.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.github.dto.GithubActionDto;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.PullRequest;
import com.opsbeach.connect.github.entity.SchemaFileAudit;
import com.opsbeach.connect.github.entity.Workflow;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.schemata.dto.SchemaValidationDto;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.connect.schemata.validate.Status;
import com.opsbeach.connect.task.dto.ConnectDto;
import com.opsbeach.connect.task.dto.TaskDto;
import com.opsbeach.connect.task.entity.Connect;
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.sharedlib.dto.ClientDto;
import com.opsbeach.sharedlib.dto.UserDto;
import com.opsbeach.sharedlib.exception.SchemaParserException;
import com.opsbeach.sharedlib.security.ApplicationConfig;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.service.GoogleCloudService;
import com.opsbeach.sharedlib.utils.Constants;

public class GitHubServiceTest {

    @InjectMocks
    private GitHubService gitHubService;
    @Mock
    private ApplicationConfig applicationConfig;
    @Mock
    private ConnectService connectService;
    @Mock
    private App2AppService app2AppService;
    @Mock
    private TaskService taskService;
    @Mock
    private ClientRepoService clientRepoService;
    @Mock
    private GoogleCloudService googleCloudService;
    @Mock
    private PullRequestService pullRequestService;
    @Mock
    private SchemaFileAuditService schemaFileAuditService;
    @Mock
    private TableService tableService;
    @Mock
    private WorkflowService workflowService;

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

    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode githubResponses;
    private Map<String, String> github;
    private Map<String, String> gcloud;
    private String githubRedirectURI;
    private String loginUrl;
    private String reposRedirectURI;
    private String accessTokenUrl;
    private String userUrl;
    private String repoDetailsUrl;
    private String githubRefreshTokenUrl;
    private String userOrgUrl;
    private String clientReposUrl;
    private String orgReposUrl;
    private String tarballUrl;
    private String constructPrUrl;
    private String downloadFileUrl;
    private String deleteComment;
    private String createPrComment;
    private String getBranchInfoUrl;
    private String createTreeObjectUrl;
    private String createCommitUrl;
    private String createBranchUrl;
    private String createPrUrl;
    private String pushCommitToBranch;
    private String githubFilePath;
    private String createRepoForAuthenticatedUser;
    private String getUserDetailsUrl;

    @BeforeEach
    public void init() throws StreamReadException, DatabindException, IOException {
        InputStream inputStream = new FileInputStream(new File("src/test/resources/github-response.json"));
        githubResponses = mapper.readValue(inputStream, JsonNode.class);

        inputStream = new FileInputStream(new File("src/test/resources/application-test.yaml"));
        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> data = yaml.load(inputStream);
        gcloud = data.get("gcloud");
        github = data.get("github");
        githubRedirectURI = github.get("redirect-url");
        loginUrl = github.get("login-url");
        reposRedirectURI = github.get("repos-redirect-url");
        accessTokenUrl = github.get("access_token");
        userUrl = github.get("user");
        repoDetailsUrl = github.get("repo-details");
        githubRefreshTokenUrl = github.get("refresh-token");
        userOrgUrl = github.get("user_orgs");
        clientReposUrl = github.get("user_repos");
        orgReposUrl = github.get("organization_repos");
        tarballUrl = github.get("tarball");
        constructPrUrl = github.get("construct-pr-url");
        downloadFileUrl = github.get("download_file_url");
        deleteComment = github.get("delete-comment");
        createPrComment = github.get("create-pr-comment");
        getBranchInfoUrl = github.get("get_branch_info_url");
        createTreeObjectUrl = github.get("create_tree_object_url");
        createCommitUrl = github.get("create_commit_url");
        createBranchUrl = github.get("create_branch_url");
        createPrUrl = github.get("create_pr_url");
        pushCommitToBranch = github.get("push-commit-to-branch");
        githubFilePath = github.get("construct-file-path");
        createRepoForAuthenticatedUser = github.get("create-repo-authenticated-user");
        getUserDetailsUrl = github.get("get-user-details");
    }

    private ConnectDto getConnectDto() {
        return ConnectDto.builder().id(1L).clientId(0L).authToken("token").refreshToken("refreshToken").userName("owner").build();
    }

    private ClientRepo getClientRepo(RepoType repoType) {
        return ClientRepo.builder().id(1L).fullName("owner/repo").defaultBranch("main").connectId(1L)
                .clientId(1L).owner("owner").name("repo").repoType(repoType).build();
    }

    @Test
    public void logInRedirectTest() {
            mockApplicationUser();
        String uri = githubRedirectURI.replace("{smClientId}", "0");
        String url = loginUrl.replace("{clientID}", github.get(Constants.CLIENT_ID)).replace("{redirectURI}", uri);
            when(applicationConfig.getGithub()).thenReturn(github);
            ReflectionTestUtils.setField(gitHubService, "githubRedirectURI", githubRedirectURI);
            ReflectionTestUtils.setField(gitHubService, "loginUrl", loginUrl);
        var response = gitHubService.logInRedirect();
        System.out.println(response);
        assertEquals(response.getLoginRedirectURL(), url);
    }

    @Test
    public void getTokenTest() {
        var smClientId = 0L;
        var connect = Connect.builder().id(1L).clientId(0L).authToken("token").build();
            when(connectService.getModel(ServiceType.GITHUB, smClientId)).thenReturn(Optional.empty());
            ReflectionTestUtils.setField(gitHubService, "reposRedirectURI", reposRedirectURI);
        var accessTokenResponse = githubResponses.get("access_token").asText();
            ReflectionTestUtils.setField(gitHubService, "accessTokenUrl", accessTokenUrl);
            when(app2AppService.getHttpResponse(accessTokenUrl, HttpMethod.POST, null)).thenReturn(accessTokenResponse);
            ReflectionTestUtils.setField(gitHubService, "userUrl", userUrl);
            when(app2AppService.httpGet(userUrl, null, JsonNode.class)).thenReturn(githubResponses.get("user_details"));
        var connectDto = getConnectDto();
            when(connectService.add(any(ConnectDto.class))).thenReturn(connectDto);
        var result = reposRedirectURI + "?success=true" + "&connect_id=" + connectDto.getId();
        assertEquals(gitHubService.getToken("code", smClientId), result);

        accessTokenResponse = githubResponses.get("invalid_access_code").asText();
            when(app2AppService.getHttpResponse(accessTokenUrl, HttpMethod.POST, null)).thenReturn(accessTokenResponse);
        assertEquals(gitHubService.getToken("code", smClientId), reposRedirectURI + "?success=false");

            when(connectService.getModel(ServiceType.GITHUB, smClientId)).thenReturn(Optional.of(connect));
        assertEquals(gitHubService.getToken("code", smClientId), result);
    }
    
    @Test
    public void getDefaultBranchTest() {
        var connectDto = getConnectDto();
            when(connectService.get(anyLong())).thenReturn(connectDto);
            ReflectionTestUtils.setField(gitHubService, "repoDetailsUrl", repoDetailsUrl);
        var url = repoDetailsUrl.replace("{owner}", "owner").replace("{repo}", "repo");
            when(app2AppService.httpGet(url, null, JsonNode.class)).thenReturn(githubResponses.get("repo_details"));
        var response = gitHubService.getDefaultBranch("owner", "repo", 1L);
        assertEquals(response, githubResponses.get("repo_details").get("default_branch").asText());

        response = gitHubService.getDefaultBranch(null, "repo", 1L);
        assertEquals(response, githubResponses.get("repo_details").get("default_branch").asText());
    }

    @Test
    public void generateNewTokenTest() {
        var taskDto = TaskDto.builder().id(1L).createdAt(LocalDateTime.now().minusHours(1)).connectId(1L).executionInterval(28800000).build();
            when(taskService.get(anyLong())).thenReturn(taskDto);
        gitHubService.generateNewToken(1L);

        taskDto.setCreatedAt(LocalDateTime.now().minusHours(10));
            when(taskService.get(anyLong())).thenReturn(taskDto);
        var connectDto = getConnectDto();
            when(connectService.get(anyLong())).thenReturn(connectDto);
            ReflectionTestUtils.setField(gitHubService, "githubRefreshTokenUrl", githubRefreshTokenUrl);
            when(applicationConfig.getGithub()).thenReturn(github);
        var url = UriComponentsBuilder.fromUriString(githubRefreshTokenUrl)
                                      .queryParam("refresh_token", connectDto.getRefreshToken())
                                      .queryParam("client_id", github.get(Constants.CLIENT_ID))
                                      .queryParam("client_secret", github.get(Constants.CLIENT_SECRET))
                                      .queryParam("grant_type", "refresh_token").buildAndExpand().toUriString();
        var accessTokenResponse = githubResponses.get("access_token").asText();
            when(app2AppService.getHttpResponse(url, HttpMethod.POST, null)).thenReturn(accessTokenResponse);
        gitHubService.generateNewToken(1L);
    }

    @Test
    public void getUserOrganizationTest() {
            when(connectService.get(anyLong())).thenReturn(getConnectDto());
            ReflectionTestUtils.setField(gitHubService, "userOrgUrl", userOrgUrl);
            when(app2AppService.httpGetEntities(userOrgUrl, null, JsonNode.class)).thenReturn(List.of(githubResponses.get("user_details")));
        var response = gitHubService.getUserOrganization(1L);
        assertEquals(response.size(), 1);
        assertEquals(response.get(0), githubResponses.get("user_details").get("login").asText());
    }

    @Test
    public void getReposTest() {
        var connectDto = getConnectDto();
            when(connectService.get(anyLong())).thenReturn(connectDto);
            ReflectionTestUtils.setField(gitHubService, "clientReposUrl", clientReposUrl);
        var url = clientReposUrl.replace("{userId}", connectDto.getUserName());
            when(app2AppService.httpGet(url, null, JsonNode.class)).thenReturn(githubResponses.get("user_repos"));
        var response = gitHubService.getRepos(1L, null);
        assertEquals(response.getRepos().size(), 1);
        assertEquals(response.getRepos().get(0), githubResponses.get("user_repos").get("items").get(0).get("name").asText());

            ReflectionTestUtils.setField(gitHubService, "orgReposUrl", orgReposUrl);
        url = orgReposUrl.replace("{orgName}", connectDto.getUserName());
            when(app2AppService.httpGetEntities(url, null, JsonNode.class)).thenReturn(List.of(githubResponses.get("repo_details")));
        response = gitHubService.getRepos(1L, connectDto.getUserName());
        assertEquals(response.getRepos().size(), 1);
        assertEquals(response.getRepos().get(0), githubResponses.get("repo_details").get("name").asText());        
    }

    private void mockDownloadTarballMethod(ClientRepo clientRepo, byte[] content) {
        ReflectionTestUtils.setField(gitHubService, "tarballUrl", tarballUrl);
        var url = tarballUrl.replace("{FullName}", clientRepo.getFullName()).replace("{branchName}", clientRepo.getDefaultBranch());
            when(app2AppService.restTemplateExchange(url, HttpMethod.GET, null, byte[].class)).thenReturn(content);
            when(clientRepoService.getClient()).thenReturn(ClientDto.builder().id(1L).name("opsbeach").build());
            when(applicationConfig.getGcloud()).thenReturn(gcloud);
    }

    @Test
    public void downloadTarballTest() {
        var clientRepo = getClientRepo(RepoType.AVRO);
        var content = "Conetent".getBytes();
            mockDownloadTarballMethod(clientRepo, content);
        var response = gitHubService.downloadTarball(clientRepo, getConnectDto());
        assertEquals(response, content);
    }

    private void mockDownloadFileMethod(ClientRepo clientRepo, GithubActionDto githubActionDto, PullRequest pullRequest) {
            ReflectionTestUtils.setField(gitHubService, "downloadFileUrl", downloadFileUrl);
        var url = downloadFileUrl.replace("{owner}", clientRepo.getFullName().split("/")[0])
                                 .replace("{repo}", clientRepo.getFullName().split("/")[1])
                                 .replace("{path}", githubActionDto.getFilesChanged());
        url = UriComponentsBuilder.fromUriString(url).queryParam("ref", pullRequest.getSourceBranch()).toUriString();
        var fileContent = JsonNodeFactory.instance.objectNode().put("content", Base64.getEncoder().encode("Content".getBytes()));
            when(app2AppService.httpGet(url, null, JsonNode.class)).thenReturn(fileContent);
    }

    @Test
    public void pullRequestActionTest() throws IOException {
        var githubActionDto = GithubActionDto.builder().prName("prName").prNumber("1").repoName("owner/repo")
                                .sourceBranch("develop").targetBranch("main").sha("sha").status("open")
                                .raisedBy("user").filesChanged("filePath").build();
        var clientRepo = getClientRepo(RepoType.AVRO);
        var connectDto = getConnectDto();
            mockApplicationUser();
            when(clientRepoService.getByFullName(anyString())).thenReturn(clientRepo);
            when(connectService.get(anyLong())).thenReturn(connectDto);
            ReflectionTestUtils.setField(gitHubService, "constructPrUrl", constructPrUrl);
        var pullRequest = PullRequest.builder().id(1L).number("1").sourceBranch("develop").status(PullRequest.Status.OPEN).build();
            when(pullRequestService.addModel(any(PullRequest.class))).thenReturn(pullRequest);
            when(pullRequestService.updateModel(any(PullRequest.class))).thenReturn(pullRequest);
            mockDownloadFileMethod(clientRepo, githubActionDto, pullRequest);

        var response = mapper.convertValue(gitHubService.pullRequestAction(githubActionDto), Map.class);
        assertTrue(mapper.convertValue(response.get("isMerge"), Boolean.class));

        pullRequest.setSha("sha");
            when(pullRequestService.findByRepoIdAndNumber(anyLong(), anyString())).thenReturn(pullRequest);
        response = mapper.convertValue(gitHubService.pullRequestAction(githubActionDto), Map.class);
        assertTrue(mapper.convertValue(response.get("isMerge"), Boolean.class));

        pullRequest.setStatus(PullRequest.Status.CLOSED);
        response = mapper.convertValue(gitHubService.pullRequestAction(githubActionDto), Map.class);
        assertTrue(mapper.convertValue(response.get("isMerge"), Boolean.class));

        pullRequest.setSha("sha1");
        pullRequest.setStatus(PullRequest.Status.OPEN);
        pullRequest.setValidationStatus(Status.SUCCESS);
        clientRepo = getClientRepo(RepoType.PROTOBUF);
            mockDownloadTarballMethod(clientRepo, "Content".getBytes());
            when(clientRepoService.getByFullName(anyString())).thenReturn(clientRepo);
        response = mapper.convertValue(gitHubService.pullRequestAction(githubActionDto), Map.class);
        assertTrue(mapper.convertValue(response.get("isMerge"), Boolean.class));
        
        githubActionDto = GithubActionDto.builder().prName("prName").prNumber("1").repoName("owner/repo")
                                .sourceBranch("develop").targetBranch("main").sha("sha").status("closed")
                                .raisedBy("user").filesChanged("filePath").build();
        response = mapper.convertValue(gitHubService.pullRequestAction(githubActionDto), Map.class);
        assertTrue(mapper.convertValue(response.get("isMerge"), Boolean.class));
        
        githubActionDto = GithubActionDto.builder().prName("prName").prNumber("1").repoName("owner/repo")
                                .sourceBranch("develop").targetBranch("main").sha("sha").status("merged")
                                .raisedBy("user").filesChanged("filePath").build();
        assertEquals(mapper.convertValue(gitHubService.pullRequestAction(githubActionDto), String.class), "Changes Accepted.");

        pullRequest.setWorkflowId(1L);
        ReflectionTestUtils.setField(gitHubService, "workflowService", workflowService);
        assertEquals(mapper.convertValue(gitHubService.pullRequestAction(githubActionDto), String.class), "Changes Accepted.");

        assertThrows(IllegalArgumentException.class, () -> 
                gitHubService.pullRequestAction(GithubActionDto.builder().prNumber("1").repoName("owner/repo").status("opened").build()));
    }

    @Test
    public void validateSchemaTest() throws IllegalArgumentException, IOException {
        var githubActionDto = GithubActionDto.builder().prName("prName").prNumber("1").repoName("owner/repo")
                                .sourceBranch("develop").targetBranch("main").sha("sha").status("closed")
                                .raisedBy("user").filesChanged("filePath").schemaValidationMessage("message").build();
        var clientRepo = getClientRepo(RepoType.PROTOBUF);
        var connectDto = getConnectDto();
            mockApplicationUser();
            when(clientRepoService.getByFullName(anyString())).thenReturn(clientRepo);
            when(connectService.get(anyLong())).thenReturn(connectDto);
            ReflectionTestUtils.setField(gitHubService, "constructPrUrl", constructPrUrl);
        var pullRequest = PullRequest.builder().id(1L).number("1").sourceBranch("develop").status(PullRequest.Status.OPEN).build();
            when(pullRequestService.addModel(any(PullRequest.class))).thenReturn(pullRequest);
            when(pullRequestService.updateModel(any(PullRequest.class))).thenReturn(pullRequest);
            ReflectionTestUtils.setField(gitHubService, "downloadFileUrl", downloadFileUrl);
        var response = mapper.convertValue(gitHubService.validateSchema(githubActionDto), JsonNode.class);
        assertTrue(response.get("status").asBoolean());

        githubActionDto = GithubActionDto.builder().prName("prName").prNumber("1").repoName("owner/repo")
                            .sourceBranch("develop").targetBranch("main").sha("sha").status("open")
                            .raisedBy("user").filesChanged("filePath").build();
        pullRequest.setIssueCommentId(1L);
            when(pullRequestService.findByRepoIdAndNumber(anyLong(), anyString())).thenReturn(pullRequest);
            ReflectionTestUtils.setField(gitHubService, "deleteComment", deleteComment);
        response = mapper.convertValue(gitHubService.validateSchema(githubActionDto), JsonNode.class);
        assertTrue(response.get("status").asBoolean());

        var schemaValidationMessage = "Incompatible fields: Summary[filename=category.proto, schemaName=Category, fieldName=is_active, fieldType=TYPE_BOOL] ";
        githubActionDto = GithubActionDto.builder().prName("prName").prNumber("1").repoName("owner/repo")
                            .sourceBranch("develop").targetBranch("main").sha("sha").status("open")
                            .raisedBy("user").schemaValidationMessage(schemaValidationMessage).filesChanged("filePath").build();
            ReflectionTestUtils.setField(gitHubService, "createPrComment", createPrComment);
            when(app2AppService.setHeaders(anyMap(), any(JsonNode.class))).thenReturn(new HttpEntity<Object>("body"));
            when(app2AppService.httpPost(anyString(), any(), any())).thenReturn(githubResponses.get("pr_comment"));
        response = mapper.convertValue(gitHubService.validateSchema(githubActionDto), JsonNode.class);
        assertFalse(response.get("status").asBoolean());

        clientRepo = getClientRepo(RepoType.AVRO);
            when(clientRepoService.getByFullName(anyString())).thenReturn(clientRepo);
        githubActionDto = GithubActionDto.builder().prName("prName").prNumber("1").repoName("owner/repo")
                            .sourceBranch("develop").targetBranch("main").sha("sha").status("open")
                            .raisedBy("user").filesChanged("filePath").build();
            mockDownloadFileMethod(clientRepo, githubActionDto, pullRequest);
        var table = Table.builder().id(1L).build();
            when(schemaFileAuditService.getTablesFromFileContent(any(byte[].class), anyBoolean(), anyString())).thenReturn(List.of(table));
        pullRequest.setIssueCommentId(null);
            when(pullRequestService.findByRepoIdAndNumber(anyLong(), anyString())).thenReturn(pullRequest);
        var schemaValidationDto = SchemaValidationDto.builder().status(true).build();
            when(tableService.schemaCompare(anyMap(), any(ClientRepo.class), anyLong())).thenReturn(schemaValidationDto);

        response = mapper.convertValue(gitHubService.validateSchema(githubActionDto), JsonNode.class);
        assertTrue(response.get("status").asBoolean());

        schemaValidationDto = SchemaValidationDto.builder().status(false).errorMessages(List.of())
            .errorMap(Map.of("filePath", Map.of("schema", List.of("Invalid Field")))).build();
            when(tableService.schemaCompare(anyMap(), any(ClientRepo.class), anyLong())).thenReturn(schemaValidationDto);
        response = mapper.convertValue(gitHubService.validateSchema(githubActionDto), JsonNode.class);
        assertFalse(response.get("status").asBoolean());

            when(schemaFileAuditService.getTablesFromFileContent(any(byte[].class), anyBoolean(), anyString())).thenThrow(new SchemaParserException("Invalid Avro File"));
        response = mapper.convertValue(gitHubService.validateSchema(githubActionDto), JsonNode.class);
        assertFalse(response.get("status").asBoolean());
    }

    private void mockGetBranchInfoMethod(ClientRepo clientRepo) {
        var url = getBranchInfoUrl.replace("{owner}", clientRepo.getOwner()).replace("{repo}", clientRepo.getName()).replace("{branchName}", clientRepo.getDefaultBranch());
            ReflectionTestUtils.setField(gitHubService, "getBranchInfoUrl", getBranchInfoUrl);
            when(app2AppService.httpGet(url, null, JsonNode.class)).thenReturn(githubResponses.get("branch_info"));
    }

    private void mockPushFilesToGitMethod(ClientRepo clientRepo) {
        var url = createTreeObjectUrl.replace("{owner}", clientRepo.getOwner()).replace("{repo}", clientRepo.getName());
            ReflectionTestUtils.setField(gitHubService, "createTreeObjectUrl", createTreeObjectUrl);
            when(app2AppService.httpPost(url, null, JsonNode.class)).thenReturn(githubResponses.get("create_tree_object"));
    }

    private void mockCreateCommitMethod(ClientRepo clientRepo) {
        var url = createCommitUrl.replace("{owner}", clientRepo.getOwner()).replace("{repo}", clientRepo.getName());
            ReflectionTestUtils.setField(gitHubService, "createCommitUrl", createCommitUrl);
            when(app2AppService.httpPost(url, null, JsonNode.class)).thenReturn(githubResponses.get("create_commit"));
    }

    private void mockPushCommitToBranchMethod(ClientRepo clientRepo) {
        var url = pushCommitToBranch.replace("{owner}", clientRepo.getOwner()).replace("{repo}", clientRepo.getName()).replace("{branchName}", clientRepo.getDefaultBranch());
            ReflectionTestUtils.setField(gitHubService, "pushCommitToBranch", pushCommitToBranch);
            when(app2AppService.httpPost(url, null, JsonNode.class)).thenReturn(githubResponses.get("push_commit_to_branch"));
    }

    @Test
    public void commitAndPushInMainBranchTest() {
        var clientRepo = getClientRepo(RepoType.PROTOBUF);
        var connectDto = getConnectDto();
            when(clientRepoService.get(anyLong())).thenReturn(clientRepo.toDto(clientRepo));
            when(connectService.get(anyLong())).thenReturn(connectDto);
            mockGetBranchInfoMethod(clientRepo);
            mockPushFilesToGitMethod(clientRepo);
            mockCreateCommitMethod(clientRepo);
            mockPushCommitToBranchMethod(clientRepo);
        var response = gitHubService.commitAndPushInMainBranch(generateFileContentMap(clientRepo), "commit message");
        assertEquals(response, githubResponses.get("push_commit_to_branch").get("sha").asText());
    }

    private Map<SchemaFileAudit, String> generateFileContentMap(ClientRepo clientRepo) {
        var githubPath = githubFilePath.replace("{repoFullName}", clientRepo.getFullName())
                    .replace("{branch}", clientRepo.getDefaultBranch()).replace("{filePath}", "src/product.proto");
        var schemaFileAudit = SchemaFileAudit.builder().id(1L).clientRepoId(clientRepo.getId()).path(githubPath).build();
        return Map.of(schemaFileAudit, "Content");
    }

    private void mockCreateBranchMethod(ClientRepo clientRepo) {
        var url = createBranchUrl.replace("{owner}", clientRepo.getOwner()).replace("{repo}", clientRepo.getName()).replace("{branchName}", clientRepo.getDefaultBranch());
            ReflectionTestUtils.setField(gitHubService, "createBranchUrl", createBranchUrl);
            when(app2AppService.httpPost(url, null, JsonNode.class)).thenReturn(githubResponses.get("branch_info"));
    }

    private void mockRaisePrMethod(ClientRepo clientRepo) {
        var url = createPrUrl.replace("{owner}", clientRepo.getOwner()).replace("{repo}", clientRepo.getName());
            ReflectionTestUtils.setField(gitHubService, "createPrUrl", createPrUrl);
            when(app2AppService.httpPost(url, null, JsonNode.class)).thenReturn(githubResponses.get("pr_info"));
    }

    @Test
    public void commitAndRaisePrTest() {
        var clientRepo = getClientRepo(RepoType.PROTOBUF);
        var connectDto = getConnectDto();
            when(clientRepoService.get(anyLong())).thenReturn(clientRepo.toDto(clientRepo));
            when(connectService.get(anyLong())).thenReturn(connectDto);
            mockGetBranchInfoMethod(clientRepo);
            mockPushFilesToGitMethod(clientRepo);
            mockCreateCommitMethod(clientRepo);
            mockCreateBranchMethod(clientRepo);
            mockRaisePrMethod(clientRepo);
        var workflow = Workflow.builder().id(1L).title("title").purpose("purpose").build();
            ReflectionTestUtils.setField(gitHubService, "constructPrUrl", constructPrUrl);
        var response = gitHubService.commitAndRaisePr(generateFileContentMap(clientRepo), workflow);
        assertNull(response);
    }

    @Test
    public void createNewRepoTest() {
        var clientRepo = getClientRepo(RepoType.PROTOBUF);
        var connectDto = getConnectDto();
            when(connectService.get(anyLong())).thenReturn(connectDto);
            ReflectionTestUtils.setField(gitHubService, "createRepoForAuthenticatedUser", createRepoForAuthenticatedUser);
            when(app2AppService.setHeaders(anyMap(), any(JsonNode.class))).thenReturn(new HttpEntity<Object>("body"));
            when(app2AppService.httpPost(anyString(), any(), any())).thenReturn(githubResponses.get("repo_details"));
        var response = gitHubService.createNewRepo(null, clientRepo.getName(), 1L);
        assertEquals(response.get("id").asInt(), githubResponses.get("repo_details").get("id").asInt());

        var url = getUserDetailsUrl.replace("{userName}", clientRepo.getOwner());
            ReflectionTestUtils.setField(gitHubService, "getUserDetailsUrl", getUserDetailsUrl);
            when(app2AppService.httpGet(url, null, JsonNode.class)).thenReturn(JsonNodeFactory.instance.objectNode().put("type", "user"));
        response = gitHubService.createNewRepo(clientRepo.getOwner(), clientRepo.getName(), 1L);
        assertEquals(response.get("id").asInt(), githubResponses.get("repo_details").get("id").asInt());

            ReflectionTestUtils.setField(gitHubService, "orgReposUrl", orgReposUrl);
            when(app2AppService.httpGet(url, null, JsonNode.class)).thenReturn(JsonNodeFactory.instance.objectNode().put("type", "Organization"));
        response = gitHubService.createNewRepo(clientRepo.getOwner(), clientRepo.getName(), 1L);
        assertEquals(response.get("id").asInt(), githubResponses.get("repo_details").get("id").asInt());
    }

    @Test
    public void createPrCommentTest() {
        var connectDto = getConnectDto();
            when(connectService.get(anyLong())).thenReturn(connectDto);
            ReflectionTestUtils.setField(gitHubService, "createPrComment", createPrComment);
            when(app2AppService.setHeaders(anyMap(), any(JsonNode.class))).thenReturn(new HttpEntity<Object>("body"));
            when(app2AppService.httpPost(anyString(), any(), any())).thenReturn(githubResponses.get("pr_comment"));
        var response = gitHubService.createPrComment("owner", "repo", "1", "message", 1L);
        assertEquals(response.get("id").asInt(), githubResponses.get("pr_comment").get("id").asInt());
    }

    @Test
    public void deleteCommentTest() {
        var connectDto = getConnectDto();
            when(connectService.get(anyLong())).thenReturn(connectDto);
            ReflectionTestUtils.setField(gitHubService, "deleteComment", deleteComment);
        gitHubService.deleteComment("owner", "repo", 1L, 1L);
    }
}
