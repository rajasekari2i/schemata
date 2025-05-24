package com.opsbeach.connect.schemata.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.opsbeach.connect.schemata.dto.TableDto;
import com.opsbeach.connect.schemata.enums.EventType;
import com.opsbeach.connect.schemata.enums.ModelType;
import com.opsbeach.connect.schemata.enums.SchemaType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Node
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Table {
    
    @Id
    @GeneratedValue
    private Long id;

    private Long clientId;

    private String jsonSchemaId;

    @Builder.Default
    private Boolean isDeleted = Boolean.FALSE;

    @Builder.Default
    private Boolean isUserChanged = Boolean.FALSE;

    private Long prId;  // primary key of pull_request table postgres.

    private String name;

    private String nameSpace;

    private String type;

    private String description; //editable

    private String owner;

    private String domain;  //editable

    private String email;   //editable

    private String complianceOwner;   //editable

    private String channel;  //editable

    private String[] subscribers;  //editable

    private String qualityRuleBase;

    private String qualityRuleSql;

    private String qualityRuleCel;

    @Builder.Default
    private String status = "Active";  //editable

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private SchemaType schemaType = SchemaType.UNKNOWN;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private EventType eventType = EventType.NONE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ModelType modelType = ModelType.NONE;
    
    private String[] requiredFields;  // Json Schema Prop

    @Builder.Default
    @Setter
    @Relationship(value = "PROPERTIES")
    private List<Field> fields = new ArrayList<>();

    @Relationship(value = "MODIFIED_TO")
    private Table modifiedTable;

    public TableDto toDto(Table table) {
        return TableDto.builder().id(table.getId())
                                 .name(table.getName())
                                 .isDeleted(table.getIsDeleted())
                                 .isUserChanged(table.getIsUserChanged())
                                 .prId(table.getPrId())
                                 .nameSpace(table.getNameSpace())
                                 .type(table.getType())
                                 .description(table.getDescription())
                                 .channel(table.getChannel())
                                 .subscribers(table.getSubscribers())
                                 .owner(table.getOwner())
                                 .qualityRuleBase(table.getQualityRuleBase())
                                 .qualityRuleSql(table.getQualityRuleSql())
                                 .qualityRuleCel(table.getQualityRuleCel())
                                 .domain(table.getDomain())
                                 .status(table.getStatus())
                                 .schemaType(table.getSchemaType())
                                 .eventType(table.getEventType())
                                 .modelType(table.getModelType())
                                 .email(table.getEmail())
                                 .complianceOwner(table.getComplianceOwner())
                                 .requiredFields(table.getRequiredFields())
                                 .modifiedTable(Objects.nonNull(table.getModifiedTable()) ? toDto(table.getModifiedTable()) : null)
                                 .fields(ObjectUtils.isEmpty(table.getFields()) ? List.of() : table.getFields().stream().map(table.getFields().get(0)::toDto).toList())
                                 .build();
    }

    public static final class Prop {

        // AVRO
        public static final String DESC = "desc";    
        public static final String DESCRIPTION = "description";
        public static final String COMMENT = "comment";
        public static final String SEE_ALSO = "see_also";
        public static final String REFERENCE = "reference";
        public static final String OWNER = "owner";
        public static final String DOMAIN = "domain";
        public static final String STATUS = "status";
        public static final String SCHEMA_TYPE = "schema_type";
        public static final String EVENT_TYPE = "event_type";
        public static final String MODEL_TYPE = "model_type";
        public static final String EMAIL = "email";
        public static final String TEAM_CHANNEL = "team_channel";
        public static final String ALERT_CHANNEL = "alert_channel";
        public static final String COMPLIANCE_OWNER = "compliance_owner";
        public static final String COMPLIANCE_CHANNEL = "compliance_channel";
        public static final String SUBSCRIBERS = "subscribers";
        public static final String CHANNEL = "channel";
        public static final String QUALITY_RULE_BASE = "quality_rule_base";
        public static final String QUALITY_RULE_SQL = "quality_rule_sql";
        public static final String QUALITY_RULE_CEL = "quality_rule_cel";

        // JSON
        public static final String PROPERTIES = "properties";
        public static final String JSON_SCHEMA_ID = "$id";
        public static final String DEFENITIONS = "definitions";
        public static final String TYPE = "type";
        public static final String TITLE = "title";
        public static final String $SCHEMA = "$schema";
        public static final String REQUIRED = "required";
        public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
        public static final String MIN_PROPERTIES = "minProperties";
        public static final String MAX_PROPERTIES = "maxProperties";
    }
}
