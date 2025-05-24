package com.opsbeach.connect.workday.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.opsbeach.connect.core.specification.IdSpecifications;
import com.opsbeach.connect.workday.dto.PillarDto;
import com.opsbeach.connect.workday.dto.TeamDto;
import com.opsbeach.connect.workday.entity.Team;
import com.opsbeach.connect.workday.repository.TeamRepository;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class TeamServiceTest {

    @InjectMocks
    private TeamService teamService;

    @Mock 
    private TeamRepository teamRepository;

    @Mock
    private PillarService pillarService;

    @Spy
    private IdSpecifications<Team> teamSpecifications;

    @Mock
    private ResponseMessage responseMessage;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private List<Team> getTeams() {
        List<Team> teams = new ArrayList<>();
        teams.add(Team.builder().id(1L).name("Streaming").description("data Streaming").build());
        teams.add(Team.builder().id(2L).name("Data Pipeling").description("Pipeline connection of date").build());
        return teams;
    }

    @Test
    public void getAllTest() {
        var teams = getTeams();
        var teamDtos = teams.stream().map(teams.get(0)::toDto).collect(Collectors.toList());
        when(teamRepository.findAll()).thenReturn(teams);
        var response = teamService.getAll();
        assertEquals(teamDtos.get(0).getId(), response.get(0).getId());
        assertEquals(teamDtos.get(1).getId(), response.get(1).getId());
        assertEquals(teamDtos.size(), response.size());
        when(teamRepository.findAll()).thenReturn(null);
        assertEquals(0, teamService.getAll().size());
    }

    @Test
    public void addTestPass() {
        var teamDto = TeamDto.builder().id(1L).name("Streaming").pillarId(1L).pillarName("analytics").description("data Streaming").build();
        var pillarDto = PillarDto.builder().id(1L).name("analytics").build();
        when(pillarService.getByName(teamDto.getPillarName())).thenReturn(pillarDto);
        Team team = Team.builder().id(1L).name("Streaming").pillarId(1L).description("data Streaming").build();
        when(teamRepository.save(ArgumentMatchers.<Team>any())).thenReturn(team);
        var response = teamService.add(teamDto);
        assertEquals(teamDto.getId(), response.getId());
        assertEquals(teamDto.getPillarId(), response.getPillarId());
    }

    @Test
    public void addTestFail() {
        var teamDto = TeamDto.builder().name("Streaming").pillarName("analytics").description("data Streaming").build();
        var teams = getTeams();
        when(teamRepository.findOne(ArgumentMatchers.<Specification<Team>>any())).thenReturn(Optional.of(teams.get(0)));
        assertThrows(IllegalArgumentException.class, () -> {
            teamService.add(teamDto);
        });
    }

    @Test
    public void getByNameTestFail() {
        assertThrows(RecordNotFoundException.class, () -> { teamService.getByName("Streaming"); });    
    }

    @Test
    public void getByNameTestPass() {
        var teamDto = TeamDto.builder().id(1L).name("Streaming").pillarId(1L).pillarName("analytics").description("data Streaming").build();
        var teams = getTeams();
        when(teamRepository.findOne(ArgumentMatchers.<Specification<Team>>any())).thenReturn(Optional.of(teams.get(0)));
        var response = teamService.getByName("Streaming");
        assertEquals(teamDto.getName(), response.getName());
    }

    @Test
    public void updateTest() {
        var teamDto = TeamDto.builder().id(1L).name("Streaming").pillarId(1L).pillarName("analytics").description("data Streaming").build();
        when(teamRepository.findById(teamDto.getId())).thenReturn(Optional.of(getTeams().get(0)));
        when(teamRepository.findAll(ArgumentMatchers.<Specification<Team>>any())).thenReturn(getTeams());
        var pillarDto = PillarDto.builder().id(1L).name("analytics").build();
        when(pillarService.getByName(teamDto.getPillarName())).thenReturn(pillarDto);
        Team team = Team.builder().id(1L).name("Streaming").pillarId(1L).description("data Streaming").build();
        when(teamRepository.save(Mockito.any(Team.class))).thenReturn(team);
        var response = teamService.update(teamDto);
        assertEquals(teamDto.getId(), response.getId());
        assertEquals(teamDto.getPillarId(), response.getPillarId());
    }

    @Test
    public void  deleteTestPass() {
        var team = getTeams().get(1);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        assertEquals(responseMessage.getSuccessMessage(SuccessCode.DELETED, "Team"), teamService.delete(team.getId()));
    }

    @Test
    public void deleteTestFail() {
        assertThrows(RecordNotFoundException.class, () -> {
            teamService.delete(1L);
        });
    }
}
