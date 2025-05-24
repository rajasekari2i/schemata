package com.opsbeach.connect.workday.controller;

import java.util.List;

import javax.validation.Valid;

import com.opsbeach.connect.workday.dto.PillarDto;
import com.opsbeach.connect.workday.service.PillarService;
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
@RequestMapping("/v1/pillar")
@RequiredArgsConstructor
public class PillarController {

    private final PillarService pillarService;

    @Transactional
    @PostMapping
    public SuccessResponse<PillarDto> add(@RequestBody @Valid PillarDto pillarDto) {
        return SuccessResponse.statusCreated(pillarService.add(pillarDto));
    }
    
    @Transactional
    @GetMapping("/{id}")
    public SuccessResponse<PillarDto> get(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(pillarService.get(id));
    }

    @Transactional
    @GetMapping
    public SuccessResponse<List<PillarDto>> getAll() {
        return SuccessResponse.statusOk(pillarService.getAll());
    }

    @Transactional
    @GetMapping("/search")
    public SuccessResponse<PillarDto> getByName(@RequestParam("name") String name) {
        return SuccessResponse.statusOk(pillarService.getByName(name));
    }

    @Transactional
    @PutMapping
    public SuccessResponse<PillarDto> update(@RequestBody @Valid PillarDto pillarDto) {
        return SuccessResponse.statusOk(pillarService.update(pillarDto));
    }

    @Transactional
    @DeleteMapping("/{id}")
    public SuccessResponse<String> delete(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(pillarService.delete(id));
    }
}
