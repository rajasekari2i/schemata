package com.opsbeach.connect.pagerduty.entity;

import com.opsbeach.connect.core.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "incident_log_entry")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentLogEntry extends BaseModel {
    @Column(name = "entry_id")
    private String entryId;
    @Column(name = "entry_created_at")
    private LocalDateTime entryCreatedAt;

    private String summary;

    private String description;

    private String type;
    @Column(name = "html_url")
    private String htmlUrl;
    @Column(name = "agent_id")
    private String agentId;
    @Column(name = "agent_type")
    private String agentType;
    @Column(name = "agent_summary")
    private String agentSummary;
    @Column(name = "channel_type")
    private String channelType;
    @Column(name = "incident_id")
    private String incidentId;
    @Column(name = "incident_type")
    private String incidentType;
    @Column(name = "incident_summary")
    private String incidentSummary;
    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "service_type")
    private String serviceType;
    @Column(name = "service_summary")
    private String serviceSummary;
    @Column(name = "row_created_at")
    private LocalDate rowCreatedAt;
    @Column(name = "row_updated_at")
    private LocalDate rowUpdatedAt;
    @Column(name = "row_deleted_at")
    private LocalDate rowDeletedAt;

}
