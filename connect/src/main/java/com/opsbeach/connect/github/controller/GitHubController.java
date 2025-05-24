package com.opsbeach.connect.github.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.opsbeach.connect.github.dto.GitHubDto;
import com.opsbeach.connect.github.dto.GithubActionDto;
import com.opsbeach.connect.github.service.GitHubService;
import com.opsbeach.sharedlib.dto.GenericResponseDto;
import com.opsbeach.sharedlib.response.SuccessResponse;
import com.opsbeach.sharedlib.utils.Constants;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/v1/github")
@RequiredArgsConstructor
public class GitHubController {
    
    private final GitHubService gitHubService;

    @PostMapping("/market-place-events")
    public SuccessResponse<GenericResponseDto> postMethodName(@RequestBody JsonNode entity) {
        System.out.println(entity.toPrettyString());
        return SuccessResponse.statusOk(GenericResponseDto.builder().status(Constants.SUCCESS).build());
    }
    

    @GetMapping("/login")
    public SuccessResponse<Object> logInRedirect(RedirectAttributes attributes) {
        return SuccessResponse.statusOk(gitHubService.logInRedirect());
    }

    @GetMapping("/signin/callback")
    public RedirectView integrateGithub(@RequestParam("code") String code, @RequestParam("smClientId") Long smClientId,  RedirectAttributes attributes) {
        return new RedirectView(gitHubService.getToken(code, smClientId));
    }

    @GetMapping("/repos")
    public SuccessResponse<GitHubDto> getRepos(@RequestParam("connectId") Long connectId,
                                               @RequestParam(name = "organizationName", required = false) String orgName) {
        return SuccessResponse.statusOk(gitHubService.getRepos(connectId, orgName));
    }

    @GetMapping("/orgs/connect/{connectId}")
    public SuccessResponse<List<String>> logInRedirect(@PathVariable("connectId") Long connectId) {
        return SuccessResponse.statusOk(gitHubService.getUserOrganization(connectId));
    } 

    @PutMapping("/compute-score")
    public SuccessResponse<Object> computeScore(@RequestBody GithubActionDto githubActionDto) throws IOException {
        return SuccessResponse.statusOk(gitHubService.pullRequestAction(githubActionDto));
    }

    @PutMapping("/validate-schema")
    public SuccessResponse<Object> validateSchema(@RequestBody GithubActionDto githubActionDto) throws IOException {
        return SuccessResponse.statusOk(gitHubService.validateSchema(githubActionDto));
    }
}
