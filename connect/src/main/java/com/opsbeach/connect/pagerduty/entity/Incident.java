package com.opsbeach.connect.pagerduty.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.IncidentDto;
import com.opsbeach.connect.pagerduty.enums.IncidentStatus;
import com.opsbeach.sharedlib.utils.DateUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * Incident table
 * </p>
 */
@Entity
@Table
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Incident extends BaseModel {
    @Column(name = "incident_id")
    private String incidentId;
    private String key;
    private int number;
    @Column(name = "incident_created_at")
    private LocalDateTime incidentCreatedAt;
    @Column(name = "last_status_change_at")
    private LocalDateTime lastStatusChangeAt;

    private String status;

    private String type;

    private String priority;        

    private String urgency;

    private String title;

    private String description;

    private String summary;
    @Column(name = "resolve_reason")
    private String resolveReason;
    @Column(name = "html_url")
    private String htmlUrl;
    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "service_type")
    private String serviceType;
    @Column(name = "service_summary")
    private String serviceSummary;
    @Column(name = "escalation_policy_id")
    private String escalationPolicyId;
    @Column(name = "escalation_policy_type")
    private String escalationPolicyType;
    @Column(name = "escalation_policy_summary")
    private String escalationPolicySummary;
    @Column(name = "trigger_id")
    private String triggerId;
    @Column(name = "trigger_type")
    private String triggerType;
    @Column(name = "trigger_summary")
    private String triggerSummary;
    @Column(name = "alert_count_triggered")
    private int alertCountTriggered;
    @Column(name = "alert_count_resolved")
    private int alertCountResolved;
    @Column(name = "alert_total_count")
    private int alertTotalCount;
    @Column(name = "row_created_at")
    private LocalDate rowCreatedAt;
    @Column(name = "row_updated_at")
    private LocalDate rowUpdatedAt;
    @Column(name = "row_deleted_at")
    private LocalDate rowDeletedAt;

    public IncidentDto toDto(Incident incident) {
        return IncidentDto.builder().clientId(incident.getClientId())
                                    .dbId(incident.getId())
                                    .id(incident.getIncidentId())
                                    .incidentKey(incident.getKey())
                                    .incidentNumber(incident.getNumber())
                                    .createdAt(DateUtil.convertLocalDateTimeToDateUTC(incident.getIncidentCreatedAt()))
                                    .status(IncidentStatus.valueOf(incident.getStatus()))
                                    .build();
    }
}
