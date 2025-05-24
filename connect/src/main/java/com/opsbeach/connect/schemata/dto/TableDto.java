package com.opsbeach.connect.schemata.dto;

import java.util.List;
import java.util.Objects;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.opsbeach.connect.schemata.entity.Table;
import com.opsbeach.connect.schemata.enums.EventType;
import com.opsbeach.connect.schemata.enums.ModelType;
import com.opsbeach.connect.schemata.enums.SchemaType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@JsonInclude(Include.NON_NULL)
public class TableDto {
    
    @Setter
    private Long id;

    private String name;

    private Long clientId;

    @Setter
    private Long clientRepoId;

    @Setter
    @Builder.Default
    private Boolean isDeleted = Boolean.FALSE;

    @Setter
    @Builder.Default
    private Boolean isUserChanged = Boolean.FALSE;

    private Long prId;

    private String nameSpace;

    private String type;

    private String description;   //editable

    private String owner;

    private String domain;  //editable

    private String email;    //editable

    private String complianceOwner;  //editable

    private String channel;  //editable

    private String[] subscribers;  //editable

    private String qualityRuleBase;

    private String qualityRuleSql;

    private String qualityRuleCel;

    @Builder.Default
    private String status = "Active";  //editable

    @Builder.Default
    private SchemaType schemaType = SchemaType.UNKNOWN;

    @JsonIgnore
    @Builder.Default
    private EventType eventType = EventType.NONE;

    @JsonIgnore
    @Builder.Default
    private ModelType modelType = ModelType.NONE;
    
    private String[] requiredFields;  // Json Schema Prop

    private List<FieldDto> fields;

    private TableDto modifiedTable;

    @Setter
    private List<String> dataTypes;   /// this field to send datatypes for this table.

    @Setter
    @Builder.Default
    private Boolean isFieldChanged = Boolean.FALSE;

    public Table toDomain(TableDto tableDto) {
        return Table.builder().id(tableDto.getId())
                              .name(tableDto.getName())
                              .isDeleted(tableDto.getIsDeleted())
                              .isUserChanged(tableDto.getIsUserChanged())
                              .prId(tableDto.getPrId())
                              .nameSpace(tableDto.getNameSpace())
                              .type(tableDto.getType())
                              .description(tableDto.getDescription())
                              .channel(tableDto.getChannel())
                              .subscribers(tableDto.getSubscribers())
                              .owner(tableDto.getOwner())
                              .domain(tableDto.getDomain())
                              .status(tableDto.getStatus())
                              .schemaType(tableDto.getSchemaType())
                              .eventType(tableDto.getEventType())
                              .modelType(tableDto.getModelType())
                              .qualityRuleBase(tableDto.getQualityRuleBase())
                              .qualityRuleSql(tableDto.getQualityRuleSql())
                              .qualityRuleCel(tableDto.getQualityRuleCel())
                              .email(tableDto.getEmail())
                              .complianceOwner(tableDto.getComplianceOwner())
                              .requiredFields(tableDto.getRequiredFields())
                              .modifiedTable(Objects.nonNull(tableDto.getModifiedTable()) ? toDomain(tableDto.getModifiedTable()) : null)
                              .fields(ObjectUtils.isEmpty(tableDto.getFields()) ? List.of() : tableDto.getFields().stream().map(tableDto.getFields().get(0)::toDomin).toList())
                              .build();
    }
}
