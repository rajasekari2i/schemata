package com.opsbeach.connect.workday.service;

import java.util.List;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.workday.dto.TeamDto;
import com.opsbeach.connect.workday.entity.Team;
import com.opsbeach.connect.workday.repository.TeamRepository;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamService {
    
    private final TeamRepository teamRepository;

    private final PillarService pillarService;

    private final IdSpecifications<Team> teamSpecifications;

    private final ResponseMessage responseMessage;

    public TeamDto add(TeamDto teamDto) {
        Specification<Team> bSpecification = teamSpecifications.findByName(teamDto.getName());
        var team = teamRepository.findOne(bSpecification).orElse(null);
        if(!ObjectUtils.isEmpty(team)) { throw new IllegalArgumentException(responseMessage.getErrorMessage(ErrorCode.ALREADY_EXISTS, Constants.TEAM)); }
        var pillarDto = pillarService.getByName(teamDto.getPillarName());
        teamDto.setPillarId(pillarDto.getId());
        team = teamDto.toDomain(teamDto);
        return team.toDto(teamRepository.save(team));
    }

    public TeamDto getByName(String name) {
        Specification<Team> bSpecification = teamSpecifications.findByName(name);
        var team = teamRepository.findOne(bSpecification).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND, name)));
        return team.toDto(team);
    }

    public TeamDto get(Long id) {
        var team = teamRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID, responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, id.toString(), Constants.TEAM)));
        return team.toDto(team);
    }

    public List<TeamDto> getAll() {
        var teams = teamRepository.findAll();
        return ObjectUtils.isEmpty(teams) ? List.of() : teams.stream().map(teams.get(0)::toDto).collect(Collectors.toList());
    }

    public TeamDto update(TeamDto teamDto) {
        get(teamDto.getId());
        var pillarDto = pillarService.getByName(teamDto.getPillarName());
        teamDto.setPillarId(pillarDto.getId());
        Team team = teamDto.toDomain(teamDto);
        return team.toDto(teamRepository.save(team));
    }

    public String delete(Long id) {
        var teamDto = get(id);
        var team = teamDto.toDomain(teamDto);
        team.setIsDeleted(Boolean.TRUE);
        teamRepository.save(team);
        return responseMessage.getSuccessMessage(SuccessCode.DELETED, Constants.TEAM);
    }
}
