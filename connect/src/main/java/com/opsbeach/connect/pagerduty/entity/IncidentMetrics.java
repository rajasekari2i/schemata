package com.opsbeach.connect.pagerduty.entity;

import com.opsbeach.connect.core.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_metrics")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentMetrics extends BaseModel {

    @Column(name = "row_updated_at")
    private String incidentId;

    @Column(name = "team_id")
    private String teamId;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "metrics_created_at")
    private LocalDateTime metricsCreatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    private String urgency;

    private Boolean major;

    @Column(name = "priority_name")
    private String priorityName;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Column(name = "seconds_to_resolve")
    private Integer secondsToResolve;

    @Column(name = "seconds_to_first_ack")
    private Integer secondsToFirstAck;

    @Column(name = "seconds_to_engage")
    private Integer secondsToEngage;

    @Column(name = "seconds_to_mobilize")
    private Integer secondsToMobilize;

    @Column(name = "engaged_seconds")
    private Integer engagedSeconds;

    @Column(name = "engaged_user_count")
    private Integer engagedUserCount;

    @Column(name = "escalation_count")
    private Integer escalationCount;

    @Column(name = "assignment_count")
    private Integer assignmentCount;

    @Column(name = "business_hour_interruptions")
    private Integer businessHourInterruptions;

    @Column(name = "sleep_hour_interruptions")
    private Integer sleepHourInterruptions;

    @Column(name = "off_hour_interruptions")
    private Integer offHourInterruptions;

    @Column(name = "snoozed_seconds")
    private Integer snoozedSeconds;
}
