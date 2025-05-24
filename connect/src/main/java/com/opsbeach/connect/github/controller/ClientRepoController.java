package com.opsbeach.connect.github.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opsbeach.connect.github.dto.ClientRepoDto;
import com.opsbeach.connect.github.dto.GitHubDto;
import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.github.service.ClientRepoService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/client-repo")
public class ClientRepoController {

    private final ClientRepoService clientRepoService;

    @PostMapping("/create")
    public SuccessResponse<Object> create(@RequestBody @Valid GitHubDto gitHubDto) {
        return SuccessResponse.statusCreated(clientRepoService.createNewRepo(gitHubDto));
    }

    @PostMapping
    public SuccessResponse<Object> add(@RequestBody @Valid GitHubDto gitHubDto) {
        return SuccessResponse.statusCreated(clientRepoService.add(gitHubDto));
    }

    @GetMapping("{id}")
    public SuccessResponse<ClientRepoDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(clientRepoService.get(id)); 
    }

    @GetMapping("/repo-types")
    public SuccessResponse<RepoType[]> getRepoTypes() {
        return SuccessResponse.statusOk(clientRepoService.getRepoTypes()); 
    }
    
    @GetMapping
    public SuccessResponse<List<ClientRepoDto>> getAll() {
        return SuccessResponse.statusOk(clientRepoService.getAll());
    }

    @PutMapping("/{id}")
    public SuccessResponse<ClientRepoDto> updateStatus(@PathVariable("id") Long id, 
                                                       @RequestParam("status") ClientRepo.Status status) {
        return SuccessResponse.statusOk(clientRepoService.updateStatus(id, status));
    }

    @PostMapping("/upload")
    public SuccessResponse<String> uploadRepo(@RequestParam("file") MultipartFile repoMultipartFile, 
                                              @RequestParam("repoType") ClientRepo.RepoType repoType) {
        return SuccessResponse.statusCreated(clientRepoService.uploadRepo(repoMultipartFile, repoType));
    }
    
}
