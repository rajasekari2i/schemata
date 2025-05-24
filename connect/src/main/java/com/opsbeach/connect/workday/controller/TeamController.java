package com.opsbeach.connect.workday.controller;

import java.util.List;

import javax.validation.Valid;

import com.opsbeach.connect.workday.dto.TeamDto;
import com.opsbeach.connect.workday.service.TeamService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/team")
@RequiredArgsConstructor
public class TeamController {
    
    private final TeamService teamService;

    @Transactional
    @PostMapping
    public SuccessResponse<TeamDto> add(@RequestBody @Valid TeamDto teamDto) {
        return SuccessResponse.statusCreated(teamService.add(teamDto));
    }

    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<TeamDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(teamService.get(id));
    }

    @Transactional
    @GetMapping("/search")
    public SuccessResponse<TeamDto> getByName(@RequestParam("name") String name) {
        return SuccessResponse.statusOk(teamService.getByName(name));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<TeamDto>> getAll() {
        return SuccessResponse.statusOk(teamService.getAll());
    }

    @Transactional
    @PutMapping
    public SuccessResponse<TeamDto> update(@RequestBody @Valid TeamDto teamDto) {
        return SuccessResponse.statusOk(teamService.update(teamDto));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public SuccessResponse<String> delete(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(teamService.delete(id));
    }
}
