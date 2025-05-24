package com.opsbeach.connect.github.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.enums.TaskType;
import com.opsbeach.connect.github.dto.ClientRepoDto;
import com.opsbeach.connect.github.dto.GitHubDto;
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
import com.opsbeach.connect.task.service.ConnectService;
import com.opsbeach.connect.task.service.TaskService;
import com.opsbeach.sharedlib.security.ApplicationConfig;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.service.GoogleCloudService;
import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.sharedlib.utils.DateUtil;
import com.opsbeach.sharedlib.utils.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {
    private final App2AppService app2AppService;
    private final ConnectService connectService;
    private final GoogleCloudService googleCloudService;
    private final SchemaFileAuditService schemaFileAuditService;
    private final TaskService taskService;
    private final ClientRepoService clientRepoService;
    private final ApplicationConfig applicationConfig;
    private final TableService tableService;
    private final PullRequestService pullRequestService;
    @Value("${github.access_token}")
    private String accessTokenUrl;
    @Value("${github.user}")
    private String userUrl;
    @Value("${github.user_orgs}")
    private String userOrgUrl;
    @Value("${github.user_repos}")
    private String clientReposUrl;
    @Value("${github.organization_repos}")
    private String orgReposUrl;
    @Value("${github.tarball}")
    private String tarballUrl;
    @Value("${application.web.baseURL}")
    private String webBaseUrl;
    @Value("${github.refresh-token}")
    private String githubRefreshTokenUrl;
    @Value("${github.redirect-url}")
    private String githubRedirectURI;
    @Value("${github.repos-redirect-url}")
    private String reposRedirectURI;
    @Value("${github.login-url}")
    private String loginUrl;
    @Value("${github.download_file_url}")
    private String downloadFileUrl;
    @Value("${github.create_branch_url}")
    private String createBranchUrl;
    @Value("${github.get_branch_info_url}")
    private String getBranchInfoUrl;
    @Value("${github.create_tree_object_url}")
    private String createTreeObjectUrl;
    @Value("${github.create_commit_url}")
    private String createCommitUrl;
    @Value("${github.create_pr_url}")
    private String createPrUrl;
    @Value("${github.repo-details}")
    private String repoDetailsUrl;
    @Value("${github.create-repo-authenticated-user}")
    private String createRepoForAuthenticatedUser;
    @Value("${github.get-user-details}")
    private String getUserDetailsUrl;
    @Value("${github.construct-pr-url}")
    private String constructPrUrl;
    @Value("${github.push-commit-to-branch}")
    private String pushCommitToBranch;
    @Value("${github.create-pr-comment}")
    private String createPrComment;
    @Value("${github.delete-comment}")
    private String deleteComment;

    @Lazy
    @Autowired
    private WorkflowService workflowService;

    private static final String ORGANIZATION = "Organization";
    

    public GitHubDto logInRedirect() {
        String clientID = applicationConfig.getGithub().get(Constants.CLIENT_ID);
        String redirectURI = githubRedirectURI.replace("{smClientId}", SecurityUtil.getClientId().toString());
        String updatedLoginUrl = loginUrl.replace("{clientID}", clientID).replace("{redirectURI}", redirectURI);
        return GitHubDto.builder().loginRedirectURL(updatedLoginUrl).build();
    }

    @Transactional
    public String getToken(String code, Long smClientId) {
        var connect = connectService.getModel(ServiceType.GITHUB, smClientId);
        if (connect.isPresent()) return reposRedirectURI + "?success=true" + "&connect_id=" + connect.get().getId();
        
        JsonNodeFactory jnf = JsonNodeFactory.instance;
        ObjectNode payload = jnf.objectNode();
        payload.put("client_id", applicationConfig.getGithub().get(Constants.CLIENT_ID));
        payload.put("client_secret", applicationConfig.getGithub().get(Constants.CLIENT_SECRET));
        payload.put("code", code);

        var response = app2AppService.getHttpResponse(accessTokenUrl, HttpMethod.POST, app2AppService.setHeaders(payload));
        String message = response.split("&")[0].split("=")[1];
        if (message.equals("bad_verification_code")) {
            return reposRedirectURI + "?success=false";
        }
        var credentials = getCredentials(response);
        var userName = getUserDetails(credentials.get("access_token")).get("login").asText();
        var connectDto = createConnect(credentials, userName, smClientId);
        createRefreshTokenTask(connectDto, Long.parseLong(credentials.get("expires_in"))*1000);
        return reposRedirectURI + "?success=true" + "&connect_id=" + connectDto.getId();
    }

    private Map<String, String> getCredentials(String response) {
        Map<String, String> credentials = new HashMap<>();
        var stringArray = response.split("&");
        for(String val : stringArray) {
            var arr = val.split("=");
            if (arr.length > 1) {
                credentials.put(arr[0], arr[1]);
            }
        }
        return credentials;
    }

    // need to change execution interval from (SECONDS) to (MILLI_SECONDS)
    public void createRefreshTokenTask(ConnectDto connectDto, long executionInterval) {
        taskService.add(TaskDto.builder().taskType(TaskType.RENEWAL_ACCESS_TOKEN).serviceType(ServiceType.GITHUB).connectId(connectDto.getId())
                               .executionInterval(executionInterval).url(githubRefreshTokenUrl).clientId(connectDto.getClientId()).build());
        log.info("Task saved");
    }

    private Map<String, String> getHeaders(ConnectDto connectDto) {
        return Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + connectDto.getAuthToken(),
                      HttpHeaders.ACCEPT, "application/vnd.github+json");
    }

    public JsonNode getRepoDetails(String owner, String repoName, Long connectId) {
        var connectDto = connectService.get(connectId);
        return getRepoDetails(owner, repoName, connectDto);
    }

    public JsonNode getRepoDetails(String owner, String repoName, ConnectDto connectDto) {
        owner = ObjectUtils.isEmpty(owner) ? connectDto.getUserName() : owner;
        var url = repoDetailsUrl.replace("{owner}", owner).replace("{repo}", repoName);
        return app2AppService.httpGet(url, app2AppService.setHeaders(getHeaders(connectDto), null), JsonNode.class);
    }

    public String getDefaultBranch(String owner, String repoName, Long connectId) {
        return getRepoDetails(owner, repoName, connectId).get("default_branch").asText();
    }
    
    public void generateNewToken(Long taskId) {
        var taskDto = taskService.get(taskId);
        if (DateUtil.secondsBetweenDate(taskDto.getCreatedAt(), LocalDateTime.now()) < (taskDto.getExecutionInterval()/1000)) return;
        var connectDto = connectService.get(taskDto.getConnectId());
        var url = UriComponentsBuilder.fromUriString(githubRefreshTokenUrl)
                                      .queryParam("refresh_token", connectDto.getRefreshToken())
                                      .queryParam("client_id", applicationConfig.getGithub().get(Constants.CLIENT_ID))
                                      .queryParam("client_secret", applicationConfig.getGithub().get(Constants.CLIENT_SECRET))
                                      .queryParam("grant_type", "refresh_token").buildAndExpand().toUriString();
        var response = app2AppService.getHttpResponse(url, HttpMethod.POST, null);
        log.info(response);
        var credentials = getCredentials(response);
        connectDto.setAuthToken(credentials.get("access_token"));
        connectDto.setRefreshToken(credentials.get("refresh_token"));
        connectService.update(connectDto);
        taskDto.setExecutionInterval(Long.parseLong(credentials.get("expires_in"))*1000);
        taskDto.setLastSyncDate(LocalDateTime.now());
        taskService.update(taskDto);        
    }

    private ConnectDto createConnect(Map<String, String> credentials, String userName, Long clientId) {
        var connectDto = ConnectDto.builder().authType(AuthType.BEARER).serviceType(ServiceType.GITHUB)
                                                 .authToken(credentials.get("access_token"))
                                                 .refreshToken(credentials.get("refresh_token"))
                                                 .userName(userName)
                                                 .clientId(clientId)
                                                 .build();
        return connectService.add(connectDto);
    }

    public JsonNode getUserDetails(ConnectDto connectDto, String userName) {
        var url = getUserDetailsUrl.replace("{userName}", userName);
        return app2AppService.httpGet(url, app2AppService.setHeaders(getHeaders(connectDto), null), JsonNode.class);
    }

    public List<String> getUserOrganization(Long connectId) {
        var connectDto = connectService.get(connectId);
        var nodes = app2AppService.httpGetEntities(userOrgUrl, app2AppService.setHeaders(getHeaders(connectDto), null), JsonNode.class);
        List<String> orgNames = new LinkedList<>();
        for(JsonNode node : nodes) {
            orgNames.add(node.get("login").asText());
        }
        return orgNames;
    }
    public JsonNode getUserDetails(String token) {
        return app2AppService.httpGet(userUrl, app2AppService.setHeaders(Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + token), null), JsonNode.class);
    }

    public GitHubDto getRepos(Long connectId, String orgName) {
        var connectDto = connectService.get(connectId);
        List<String> repoNames = new LinkedList<>();
        String repoOwner = null;
        if (ObjectUtils.isEmpty(orgName)) { // pulling user repos
            var url = clientReposUrl.replace("{userId}", connectDto.getUserName());
            var nodes = app2AppService.httpGet(url, app2AppService.setHeaders(getHeaders(connectDto), null), JsonNode.class);
            nodes.get("items").forEach(node -> {
                repoNames.add(node.get("name").asText());
            });
            repoOwner = connectDto.getUserName();
        } else { // pulling org repos
            var url = orgReposUrl.replace("{orgName}", orgName);
            var nodes = app2AppService.httpGetEntities(url, app2AppService.setHeaders(getHeaders(connectDto), null), JsonNode.class);
            for (JsonNode jsonNode: nodes) {
                repoNames.add(jsonNode.get("name").asText());
            }
            repoOwner = orgName;
        }
        return GitHubDto.builder().connectId(connectId).repos(repoNames).repoOwner(repoOwner).user(connectDto.getUserName()).build();
    }

    public byte[] downloadTarball(ClientRepo clientRepo, ConnectDto connectDto) {
        return downloadTarball(clientRepo.getFullName(), clientRepo.getDefaultBranch(), connectDto);        
    }

    public byte[] downloadTarball(String repoFullName, String sourceBranch, ConnectDto connectDto) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.AUTHORIZATION, "Bearer " + connectDto.getAuthToken());
        headerMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        log.info("Downloading Tarball for repo = "+repoFullName);
        var url = tarballUrl.replace("{FullName}", repoFullName).replace("{branchName}", sourceBranch);
        var content = app2AppService.restTemplateExchange(url, HttpMethod.GET, app2AppService.setHeaders(headerMap, null), byte[].class);
        pushInGoogleBucket(repoFullName, content, sourceBranch);
        return content;
    }

    private boolean pushInGoogleBucket(String repoFullName, byte[] content, String branchName) {
        var clientDto = clientRepoService.getClient();
        String objectName = StringUtil.constructStringEmptySeparator(clientDto.getName(), "/", repoFullName, "/", branchName);
        googleCloudService.publish(applicationConfig.getGcloud().get("repo-bucket"), objectName, null, content);
        return true;
    }

    // this method is for github action
    @Transactional
    public Object pullRequestAction(GithubActionDto githubActionDto) throws IOException {
        var clientRepo = clientRepoService.getByFullName(githubActionDto.getRepoName());
        var connectDto = connectService.get(clientRepo.getConnectId());
        SecurityUtil.setClientId(clientRepo.getClientId());
        var pullRequest = pullRequestService.findByRepoIdAndNumber(clientRepo.getId(), githubActionDto.getPrNumber());
        if (Objects.isNull(pullRequest)) {
            pullRequest = getPullRequest(githubActionDto, clientRepo, null, PullRequest.Status.OPEN, null, null, null);
            pullRequest.setSha(null);   // Sha should be null while creating PR for first time.
            pullRequest = pullRequestService.addModel(pullRequest);
        }
        return switch (githubActionDto.getStatus()) {
            case "open" -> pullRequestOpen(githubActionDto, pullRequest, connectDto, clientRepo);
            case "merged" -> pullRequestMerged(pullRequest, clientRepo);
            case "closed" -> pullRequestClosed(pullRequest, clientRepo);
            default -> throw new IllegalArgumentException("Unexpected value: " + githubActionDto.getStatus());
        };
    }

    private void updateWorkflowStatus(PullRequest pullRequest) {
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(pullRequest.getWorkflowId()))) {
            workflowService.updateStatus(pullRequest.getWorkflowId(), Workflow.Status.PR_MERGED);
        }
    }

    private Object pullRequestMerged(PullRequest pullRequest, ClientRepo clientRepo) {
        // merge changes
        tableService.acceptChanges(pullRequest.getId(), clientRepo.getId());
        pullRequestService.updateStatus(pullRequest.getId(), PullRequest.Status.MERGED);
        updateWorkflowStatus(pullRequest);
        return "Changes Accepted.";
    }

    private Object pullRequestClosed(PullRequest pullRequest, ClientRepo clientRepo) {
        //revert changes in neo4j of this PR.
        tableService.revertChanges(pullRequest.getId(), clientRepo.getId());
        pullRequestService.updateStatus(pullRequest.getId(), PullRequest.Status.CLOSED);
        return Map.of("tables", Map.of("closed", 0.0), "isMerge", true);
    }

    private Object pullRequestOpen(GithubActionDto githubActionDto, PullRequest pullRequest, 
                                   ConnectDto connectDto, ClientRepo clientRepo) throws IOException {
        var validationStatus = Objects.isNull(pullRequest.getValidationStatus()) ? Status.SUCCESS : pullRequest.getValidationStatus();
        if (pullRequest.getSha() == null) {
            pullRequest = pullRequestService.updateModel(getPullRequest(githubActionDto, clientRepo, pullRequest.getId(), PullRequest.Status.OPEN, 
                                                          null, pullRequest.getErrorMessage(), validationStatus));
            //save the modified deltas in neo4j.
            return fetchContentAndStore(githubActionDto, pullRequest, connectDto, clientRepo);
        }
        if (pullRequest.getStatus().equals(PullRequest.Status.CLOSED)) {
            // if the previous status of this pr is closed means then now the incomeing PR is reopend.
            pullRequest = pullRequestService.updateModel(getPullRequest(githubActionDto, clientRepo, pullRequest.getId(), PullRequest.Status.REOPENED, 
                                                                        pullRequest.getWorkflowId(), pullRequest.getErrorMessage(), validationStatus));
            return fetchContentAndStore(githubActionDto, pullRequest, connectDto, clientRepo);
        }
        // if SHA is not equal, then some additional changes done in existing PR.
        if (Boolean.FALSE.equals(githubActionDto.getSha().equals(pullRequest.getSha()))) {
            //revert changes in neo4j of this PR.
            tableService.revertChanges(pullRequest.getId(), clientRepo.getId());
            // update SHA and other details in pull_request table.
            pullRequest = pullRequestService.updateModel(getPullRequest(githubActionDto, clientRepo, pullRequest.getId(), pullRequest.getStatus(), 
                                                                        pullRequest.getWorkflowId(), pullRequest.getErrorMessage(), validationStatus));
            //save the new modified deltas in neo4j.
            return fetchContentAndStore(githubActionDto, pullRequest, connectDto, clientRepo);
        } 
        // if SHA is equal then leave it.
        return Map.of("tables", Map.of("same-pr", 0.0), "isMerge", true);
    }

    private Object fetchContentAndStore(GithubActionDto githubActionDto, PullRequest pullRequest, 
                                        ConnectDto connectDto, ClientRepo clientRepo) throws IOException {
        var paths = githubActionDto.getFilesChanged().split(" ");
        if (clientRepo.getRepoType().equals(RepoType.PROTOBUF)) {
            downloadTarball(clientRepo.getFullName(), pullRequest.getSourceBranch(), connectDto);
            var tables = schemaFileAuditService.saveDeltaForProtoSchema(paths , clientRepo, pullRequest);
            return Map.of("tables", tableService.computeScores(tables), "isMerge", true);
        }
        Map<String, byte[]> fileContentMap = new HashMap<>();
        for (String path : paths) {
            var content = downloadFile(path, githubActionDto.getRepoName(), githubActionDto.getSourceBranch(), connectDto);
            fileContentMap.put(path, content);
        }
        var tables = schemaFileAuditService.saveDelta(fileContentMap, clientRepo, pullRequest.getId());
        return Map.of("tables", tableService.computeScores(tables), "isMerge", true);
    }

    private PullRequest getPullRequest(GithubActionDto githubActionDto, ClientRepo clientRepo, Long prId, PullRequest.Status status, 
                                       Long workflowId, String errorMessage, com.opsbeach.connect.schemata.validate.Status validationStatus) {
        var prUrl = constructPrUrl.replace("{repoFullName}", clientRepo.getFullName()).replace("{prNumber}", githubActionDto.getPrNumber());
        return  PullRequest.builder().number(githubActionDto.getPrNumber()).clientRepoId(clientRepo.getId())
                           .status(status).sourceBranch(githubActionDto.getSourceBranch()).validationStatus(validationStatus)
                           .targetBranch(githubActionDto.getTargetBranch()).sha(githubActionDto.getSha())
                           .id(prId).workflowId(workflowId).url(prUrl).errorMessage(errorMessage)
                           .build();
    }

    private byte[] downloadFile(String path, String repoName, String sourceBranch, ConnectDto connectDto) throws IOException {
        var url = downloadFileUrl.replace("{owner}", repoName.split("/")[0])
                                 .replace("{repo}", repoName.split("/")[1])
                                 .replace("{path}", path);
        url = UriComponentsBuilder.fromUriString(url).queryParam("ref", sourceBranch).toUriString();
        var fileResponse = app2AppService.httpGet(url, app2AppService.setHeaders(Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + connectDto.getAuthToken()), null), JsonNode.class);
        return Base64.getDecoder().decode(fileResponse.get("content").asText().replace("\n", ""));
    }

    public Object validateSchema(GithubActionDto githubActionDto) throws IOException {
        var clientRepo = clientRepoService.getByFullName(githubActionDto.getRepoName());
        var connectDto = connectService.get(clientRepo.getConnectId());
        SecurityUtil.setClientId(clientRepo.getClientId());
        var pullRequest = pullRequestService.findByRepoIdAndNumber(clientRepo.getId(), githubActionDto.getPrNumber());
        if (Objects.isNull(pullRequest)) {
            pullRequest = getPullRequest(githubActionDto, clientRepo, null, PullRequest.Status.OPEN, null, null, null);
            pullRequest.setSha(null);   // Sha should be null while creating PR for first time.
            pullRequest = pullRequestService.addModel(pullRequest);
        }
        if (githubActionDto.getStatus().equalsIgnoreCase("closed")) {
            var schemaValidationDto = SchemaValidationDto.builder().status(true).build();
            return updatePrValidationStatus(schemaValidationDto, pullRequest, connectDto, clientRepo.getOwner(), clientRepo.getName());
        }
        if (Objects.nonNull(githubActionDto.getSchemaValidationMessage())) {
            var messages = Arrays.asList(githubActionDto.getSchemaValidationMessage().split("Summary"));
            var schemaValidationDto = SchemaValidationDto.builder().status(false).errorMessages(messages).build();
            return updatePrValidationStatus(schemaValidationDto, pullRequest, connectDto, clientRepo.getOwner(), clientRepo.getName());
        }
        if (clientRepo.getRepoType().equals(RepoType.PROTOBUF)) {
            var schemaValidationDto = SchemaValidationDto.builder().status(true).build();
            return updatePrValidationStatus(schemaValidationDto, pullRequest, connectDto, clientRepo.getOwner(), clientRepo.getName());
        }
        Map<String, Table> newTableMap = new HashMap<>();
        var paths = githubActionDto.getFilesChanged().split(" ");
        for (String path : paths) {
            var content = downloadFile(path, githubActionDto.getRepoName(), githubActionDto.getSourceBranch(), connectDto);
            try {
                var fileType = path.substring(path.lastIndexOf(".") + 1);
                var tables = schemaFileAuditService.getTablesFromFileContent(content, Boolean.FALSE, fileType);
                newTableMap.put(path, tables.get(tables.size() - 1));
            } catch (Exception e) {
                var message = StringUtil.constructStringEmptySeparator("{ ",e.getMessage()," in file - ", path, " }");
                var schemaValidationDto = SchemaValidationDto.builder().status(false).errorMessages(List.of(message)).build();
                return updatePrValidationStatus(schemaValidationDto, pullRequest, connectDto, clientRepo.getOwner(), clientRepo.getName());
            }
        }
        var validationMessage = tableService.schemaCompare(newTableMap, clientRepo, pullRequest.getId());
        return updatePrValidationStatus(validationMessage, pullRequest, connectDto, clientRepo.getOwner(), clientRepo.getName());
    }

    private JsonNode updatePrValidationStatus(SchemaValidationDto schemaValidationDto, PullRequest pullRequest,
                                              ConnectDto connectDto, String owner, String repo) {
        var payload = JsonNodeFactory.instance.objectNode();
        if (Boolean.FALSE.equals(schemaValidationDto.getStatus())) {
            pullRequest.setValidationStatus(com.opsbeach.connect.schemata.validate.Status.ERROR);
            pullRequest.setSha(null);  // set sha to null while validation fails.
            //revert changes in neo4j of this PR.
            tableService.revertChanges(pullRequest.getId(), pullRequest.getClientRepoId());
            var messages = createValidationErrorMessage(schemaValidationDto);
            pullRequest.setErrorMessage(messages.get(1));
            var issueCommentId = createPrComment(owner, repo, pullRequest.getNumber(), messages.get(0), connectDto).get("id").asLong();
            if (Objects.nonNull(pullRequest.getIssueCommentId())) deleteComment(owner, repo, pullRequest.getIssueCommentId(), connectDto);
            pullRequest.setIssueCommentId(issueCommentId);
            payload.put("status", false).put("message", pullRequest.getErrorMessage());
        } else {
            pullRequest.setValidationStatus(com.opsbeach.connect.schemata.validate.Status.SUCCESS);
            pullRequest.setErrorMessage(null);
            if (Objects.nonNull(pullRequest.getIssueCommentId())) deleteComment(owner, repo, pullRequest.getIssueCommentId(), connectDto);
            pullRequest.setIssueCommentId(null);
            payload.put("status", true).put("message", "SUCCESS");
        }
        pullRequestService.updateModel(pullRequest);
        return payload;
    }

    private List<String> createValidationErrorMessage(SchemaValidationDto schemaValidationDto) {
        // StringBuilder message = new StringBuilder("<html><body><h2>This PR has some errors: <h2><p>");
        List<String> messages = new ArrayList<>();
        StringBuilder message = new StringBuilder("# Data Contract violation detected in the PR: \n");
        if (Objects.nonNull(schemaValidationDto.getErrorMap())) {
            message.append("| File name | Schema | Details |\n");
            message.append("|-------|-------|-------|\n");
            for (Map.Entry<String, Map<String, List<String>>> entry : schemaValidationDto.getErrorMap().entrySet()) {
                var fileName = entry.getKey();
                for (Map.Entry<String, List<String>> tableEntry : entry.getValue().entrySet()) {
                    var tableName = tableEntry.getKey();
                    for (String value : tableEntry.getValue()) {
                        message.append("|**").append(fileName).append("**|**").append(tableName).append("**|**").append(value).append("**|\n");
                        messages.add(value);
                    }
                }
            }
        }
        schemaValidationDto.getErrorMessages().forEach(msg -> {
            message.append("#### ➡ ").append(msg).append("\n");
            messages.add(msg);
        });
        log.info(message.toString()+"    \n"+message);
        return List.of(message.toString(), String.join(", \n", messages));
    }

    public String commitAndPushInMainBranch(Map<SchemaFileAudit, String> fileContentMap, String commitMessage) {
        var clientRepoDto = clientRepoService.get(fileContentMap.entrySet().iterator().next().getKey().getClientRepoId());
        var connectDto = connectService.get(clientRepoDto.getConnectId());
        // Get Main branch info for SHA. 
        log.info("getting main branch info");
        var mainBranchInfo = getBranchInfo(clientRepoDto.getFullName(), connectDto, clientRepoDto.getDefaultBranch());
        // create tree object of files in github.
        log.info("Push files to git tree");
        var treeObject = pushFilesToGit(fileContentMap, connectDto, clientRepoDto.getFullName(),
                            clientRepoDto.getDefaultBranch(), mainBranchInfo.get("object").get("sha").asText());
        // then create the commit of files by using SHA of tree object.
        log.info("Create commit using tree sha");
        var commitObject = createCommit(treeObject.get("sha").asText(), mainBranchInfo.get("object").get("sha").asText(), clientRepoDto.getFullName(), connectDto, commitMessage);
        // push latest commit to default branch
        log.info("Push Commit to main branch");
        pushCommitToBranch(commitObject.get("sha").asText(), clientRepoDto.getDefaultBranch(), clientRepoDto.getFullName(), connectDto);
        return commitObject.get("sha").asText();
    }

    public JsonNode pushCommitToBranch(String commitSha, String branchName, String repoFullName, ConnectDto connectDto) {
        var url = pushCommitToBranch.replace("{owner}", repoFullName.split("/")[0])
                                    .replace("{repo}", repoFullName.split("/")[1])
                                    .replace("{branchName}", branchName);
        // var entity = app2AppService.setHeaders(getHeaders(connectDto), Map.of("sha", commitSha));
        var body = JsonNodeFactory.instance.objectNode().put("sha", commitSha).toString();
        return app2AppService.httpPatch(url, body, getHeaders(connectDto), JsonNode.class);
    }

    public PullRequest commitAndRaisePr(Map<SchemaFileAudit, String> fileContentMap, Workflow workflow) {
        var clientRepoDto = clientRepoService.get(fileContentMap.entrySet().iterator().next().getKey().getClientRepoId());
        var connectDto = connectService.get(clientRepoDto.getConnectId());
        // Get Main branch info for SHA. 
        log.info("getting main branch info");
        var mainBranchInfo = getBranchInfo(clientRepoDto.getFullName(), connectDto, clientRepoDto.getDefaultBranch());
        // create tree object of files in github.
        log.info("Push files to git tree");
        var treeObject = pushFilesToGit(fileContentMap, connectDto, clientRepoDto.getFullName(),
                            clientRepoDto.getDefaultBranch(), mainBranchInfo.get("object").get("sha").asText());
        // then create the commit of files by using SHA of tree object.
        log.info("Create commit using tree sha");
        var commitObject = createCommit(treeObject.get("sha").asText(), mainBranchInfo.get("object").get("sha").asText(), clientRepoDto.getFullName(), connectDto, workflow.getPurpose());
        var newBranchName = "schemata-labs-"+workflow.getTitle().replace(" ", "-")+"-"+workflow.hashCode();
        // create the new branch with the commit SHA.
        log.info("Creating new branch");
        createBranch(connectDto, newBranchName, clientRepoDto.getFullName(), commitObject.get("sha").asText());
        // raise the PR.
        log.info("Raise PR in new Branch");
        var prInfo = raisePr(clientRepoDto.getDefaultBranch(), newBranchName, workflow.getTitle(), workflow.getPurpose(), clientRepoDto.getFullName(), connectDto);
        // store the PR info in our DB and retrun it.
        return createPullRequest(prInfo, workflow.getId(), clientRepoDto, newBranchName);
    }

    private PullRequest createPullRequest(JsonNode prInfo, Long workflowId, ClientRepoDto clientRepoDto, String sourceBranch) {
        var prUrl = constructPrUrl.replace("{repoFullName}", clientRepoDto.getFullName()).replace("{prNumber}", prInfo.get("number").asText());
        var pullRequest = PullRequest.builder().number(prInfo.get("number").asText())
                                    .status(PullRequest.Status.OPEN)
                                    .clientRepoId(clientRepoDto.getId())
                                    .workflowId(workflowId)
                                    .sha(prInfo.get("head").get("sha").asText())
                                    .sourceBranch(sourceBranch)
                                    .targetBranch(clientRepoDto.getDefaultBranch())
                                    .url(prUrl)
                                    .build();
        return pullRequestService.addModel(pullRequest);
    }

    public JsonNode getBranchInfo(String repoFullName, ConnectDto connectDto, String branchName) {
        var url = getBranchInfoUrl.replace("{owner}", repoFullName.split("/")[0])
                                  .replace("{repo}", repoFullName.split("/")[1])
                                  .replace("{branchName}", branchName);
        return app2AppService.httpGet(url, app2AppService.setHeaders(getHeaders(connectDto), null), JsonNode.class);
    }

    public JsonNode createBranch(ConnectDto connectDto, String branchName, String repoFullName, String commitSha) {
        var url = createBranchUrl.replace("{owner}", repoFullName.split("/")[0])
                                 .replace("{repo}", repoFullName.split("/")[1]);
        var body = Map.of("ref", "refs/heads/"+branchName, "sha", commitSha);
        var entity = app2AppService.setHeaders(getHeaders(connectDto), body);
        return app2AppService.httpPost(url, entity, JsonNode.class);
    }

    public JsonNode pushFilesToGit(Map<SchemaFileAudit, String> fileContentMap, ConnectDto connectDto, 
                                   String repoFullName, String baseBranchName, String baseTreeSha) {
        var url = createTreeObjectUrl.replace("{owner}", repoFullName.split("/")[0])
                                     .replace("{repo}", repoFullName.split("/")[1]);
        var body = createTreeObject(fileContentMap, repoFullName, baseBranchName, baseTreeSha);
        var entity = app2AppService.setHeaders(getHeaders(connectDto), body);
        return app2AppService.httpPost(url, entity, JsonNode.class);
    }

    private ObjectNode createTreeObject(Map<SchemaFileAudit, String> fileContentMap,
                        String repoFullName, String baseBranchName, String baseTreeSha) {
        JsonNodeFactory jnf = JsonNodeFactory.instance;
        ObjectNode payload = jnf.objectNode();
            payload.put("base_tree", baseTreeSha);
            var tree = payload.putArray("tree");
            var basePath = StringUtil.constructStringEmptySeparator("https://github.com/", repoFullName, "/tree/", baseBranchName, "/");
            for (Map.Entry<SchemaFileAudit, String> entrySet : fileContentMap.entrySet()) {
                var path = entrySet.getKey().getPath();
                var blob = tree.addObject();
                blob.put("path", path.substring(basePath.length(), path.length()));
                blob.put("mode", "100644");
                blob.put("type", "blob");
                blob.put("content", entrySet.getValue());
            }
        System.out.println(payload.toPrettyString());
        return payload;
    }

    public JsonNode createCommit(String treeSha, String parentSha, String repoFullName, ConnectDto connectDto, String message) {
        var url = createCommitUrl.replace("{owner}", repoFullName.split("/")[0])
                                 .replace("{repo}", repoFullName.split("/")[1]);
        var body = Map.of("message", message, "tree", treeSha, "parents", List.of(parentSha));
        var entity = app2AppService.setHeaders(getHeaders(connectDto), body);
        return app2AppService.httpPost(url, entity, JsonNode.class);
    }

    public JsonNode raisePr(String baseBranch, String headBranch, String title, String message, 
                            String repoFullName, ConnectDto connectDto) {
        var url = createPrUrl.replace("{owner}", repoFullName.split("/")[0])
                             .replace("{repo}", repoFullName.split("/")[1]);
        var body = Map.of("title", title, "body", message, "head", headBranch, "base", baseBranch);
        var entity = app2AppService.setHeaders(getHeaders(connectDto), body);
        return app2AppService.httpPost(url, entity, JsonNode.class);
    }

    protected JsonNode createNewRepo(String owner, String repoName, Long connectId) {
        var connectDto = connectService.get(connectId);
        return createNewRepo(owner, repoName, connectDto);
    }

    protected JsonNode createNewRepo(String owner, String repoName, ConnectDto connectDto) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("name", repoName);
        payload.put("description", "Schemata Labs managed private repo");
        payload.put("private", true);
        payload.put("has_issues", true);
        payload.put("auto_init", true);
        payload.put("default_branch", "main");
        var url = createRepoForAuthenticatedUser;
        if (Boolean.FALSE.equals(ObjectUtils.isEmpty(owner))) {
            var userDetails = getUserDetails(connectDto, owner);
            if (userDetails.get("type").asText().equals(ORGANIZATION)) {
                url = orgReposUrl.replace("{orgName}", owner);
            }
        }
        return app2AppService.httpPost(url, app2AppService.setHeaders(getHeaders(connectDto), payload), JsonNode.class);
    }

    public JsonNode createPrComment(String owner, String repo, String prNumber, String message, Long connectId) {
        var connectDto = connectService.get(connectId);
        return createPrComment(owner, repo, prNumber, message, connectDto);
    }

    public JsonNode createPrComment(String owner, String repo, String prNumber, String message, ConnectDto connectDto) {
        var url =  createPrComment.replace("{owner}", owner).replace("{repo}", repo).replace("{pr_number}", prNumber);
        var body= JsonNodeFactory.instance.objectNode().put("body", message);
        var entity = app2AppService.setHeaders(getHeaders(connectDto), body);
        return app2AppService.httpPost(url, entity, JsonNode.class);
    }

    public void deleteComment(String owner, String repo, Long commentId, Long connectId) {
        var connectDto = connectService.get(connectId);
        deleteComment(owner, repo, commentId, connectDto);
    }

    public void deleteComment(String owner, String repo, Long commentId, ConnectDto connectDto) {
        var url = deleteComment.replace("{owner}", owner).replace("{repo}", repo).replace("{comment_id}", commentId.toString());
        app2AppService.httpDelete(url, app2AppService.setHeaders(getHeaders(connectDto), null), JsonNode.class);
    }
}
