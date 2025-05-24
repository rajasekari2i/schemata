package com.opsbeach.connect.github.dto;

import java.util.List;

import com.opsbeach.connect.github.entity.Workflow;
import com.opsbeach.connect.schemata.entity.Field;
import com.opsbeach.connect.schemata.entity.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDto {
  
    Long id;
    Long domainId;
    Workflow.Status status;
    String schemaName;
    Long nodeId;
    List<FieldDto> fields;
    TableDto table;
    String stackHolders;
    String purpose;
    String creator;
    String additionalReference;
    float rank;
    String title;

    public Workflow toDomain(WorkflowDto workflowDto) {
        return Workflow.builder().id(workflowDto.id)
                                 .domainId(workflowDto.domainId)
                                 .nodeId(workflowDto.nodeId)
                                 .schemaName(workflowDto.schemaName)
                                 .stackHolders(workflowDto.stackHolders)
                                 .purpose(workflowDto.purpose)
                                 .creator(workflowDto.creator)
                                 .additionalReference(workflowDto.additionalReference)
                                 .status(workflowDto.status)
                                 .rank(workflowDto.rank)
                                 .title(workflowDto.title)
                                 .build();
    }

    /*
     * This Dto is to Accept the incomming changes done in UI.
     */
    public record  TableDto(Long id, String nameSpace, String name, String type, String description, String owner, String domain,
                         String email, String complianceOwner, String channel, String[] subscribers, String status, List<FieldDto> fields,
                         String qualityRuleBase, String qualityRuleSql, String qualityRuleCel) {
        public Table toDomain(TableDto tableDto) {
            return Table.builder().id(tableDto.id)
                                  .nameSpace(tableDto.nameSpace)
                                  .name(tableDto.name)
                                  .type(tableDto.type)
                                  .description(tableDto.description)
                                  .owner(tableDto.owner)
                                  .domain(tableDto.domain)
                                  .email(tableDto.email)
                                  .complianceOwner(tableDto.complianceOwner)
                                  .channel(tableDto.channel)
                                  .subscribers(tableDto.subscribers)
                                  .status(tableDto.status)
                                  .qualityRuleBase(tableDto.qualityRuleBase)
                                  .qualityRuleSql(tableDto.qualityRuleSql)
                                  .qualityRuleCel(tableDto.qualityRuleCel)
                                  .fields(tableDto.fields().stream().map(tableDto.fields().get(0)::toDomain).toList())
                                  .build();
        }
    }
    
    /*
     * This Dto is to Accept the incomming changes done in UI.
     */
    public record FieldDto(Long id, String name, String dataType, String description, Boolean deprecated, Boolean isPii, Boolean isClassified, Long referenceFieldId) {
        public Field toDomain(FieldDto fieldDto) {
            return Field.builder().id(fieldDto.id)
                                  .name(fieldDto.name)
                                  .dataType(fieldDto.dataType)
                                  .description(fieldDto.description)
                                  .isPii(fieldDto.isPii)
                                  .isClassified(fieldDto.isClassified)
                                  .deprecated(fieldDto.deprecated)
                                  .build();
        }
    }
}
