package com.opsbeach.connect.schemata.controller;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.opsbeach.connect.github.entity.ClientRepo.RepoType;
import com.opsbeach.connect.schemata.dto.TableDto;
import com.opsbeach.connect.schemata.dto.TableFilterOptionsDto;
import com.opsbeach.connect.schemata.dto.SchemaVisualizerDto;
import com.opsbeach.connect.schemata.service.TableService;
import com.opsbeach.sharedlib.response.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/table")
public class TableController {
    
    private final TableService tableService;

    @GetMapping("/graph/{id}")
    public SuccessResponse<SchemaVisualizerDto> getSchemaVisualizer(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(tableService.getSchemaVisualizer(id));
    }

    @GetMapping("/graph")
    public SuccessResponse<SchemaVisualizerDto> getSchemaVisualizerForAll() {
        return SuccessResponse.statusOk(tableService.getSchemaVisualizerForAll());
    }
    
    @GetMapping()
    public SuccessResponse<JsonNode> getAll(Pageable pageable, @RequestParam(name = "owner", required = false) List<String> owners,
                                                  @RequestParam(name = "domain", required = false) List<String> domains,
                                                  @RequestParam(name = "subscribers", required = false) List<String> subscribers) {
        return SuccessResponse.statusOk(tableService.getAll(owners, domains, subscribers, pageable));
    }

    @GetMapping("/{id}")
    public SuccessResponse<TableDto> getTable(@PathVariable("id") Long id) {
        return SuccessResponse.statusOk(tableService.get(id));
    }

    // @PatchMapping("/score/{rootNodeId}")
    // public SuccessResponse<Object> getScore(@PathVariable("rootNodeId") Long id, @RequestBody SchemaVisualizerDto uiDto) {
    //     return SuccessResponse.statusOk(tableService.computeScores(uiDto, id));
    // }

    // @PatchMapping("/validate/{rootNodeId}")
    // public SuccessResponse<Object> validateSchema(@PathVariable("rootNodeId") Long id, @RequestBody SchemaVisualizerDto uiDto) {
    //     return SuccessResponse.statusOk(tableService.validateSchema(uiDto, id));
    // }

    @GetMapping("/field/data-type")
    public SuccessResponse<Object> fieldDataTypes(@RequestParam(name = "repoType", required = false) RepoType repoType,
                                                  @RequestParam(name = "tableId", required = false) Long tableId) {
        if (Objects.nonNull(tableId)) {
            return SuccessResponse.statusOk(tableService.getFieldDataTypes(tableId));
        }
        return SuccessResponse.statusOk(tableService.fieldDataTypes(repoType));
    }

    @GetMapping("/filter-options")
    public SuccessResponse<TableFilterOptionsDto> getTableFilterOptions() {
        return SuccessResponse.statusOk(tableService.getTableFilterOptions());
    }

    @PostMapping("/upload-csv")
    public SuccessResponse<Object> uploadCsvFile(@RequestParam("files") List<MultipartFile> multipartFiles) throws IOException {
       return SuccessResponse.statusOk(tableService.uploadCsvToGit(multipartFiles));
    }
}
