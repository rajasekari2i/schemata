package com.opsbeach.connect.pagerduty.dto;

import java.util.Date;
import java.util.List;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.LogEntryDto.FieldDto;
import com.opsbeach.connect.pagerduty.entity.Incident;
import com.opsbeach.connect.pagerduty.entity.IncidentLogEntry;
import com.opsbeach.connect.pagerduty.entity.IncidentMetrics;
import com.opsbeach.connect.pagerduty.entity.PagerDutyService;
import com.opsbeach.connect.pagerduty.enums.IncidentStatus;
import com.opsbeach.connect.pagerduty.enums.ServiceStatus;
import com.opsbeach.sharedlib.utils.DateUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagerdutyResponseDto {

    private int limit;

    private int offset;

    private Boolean more;

    private int total;

    private List<IncidentMetricsDto> data;

    private List<IncidentDto> incidents;

    private List<ServiceDto> services;

    @JsonProperty("log_entries")
    private List<LogEntryDto> logEntries;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IncidentDto{
        
        @Setter
        private String id;

        @Setter
        private Long dbId;

        @Setter
        private Long clientId;

        private String summary;

        private String type;

        private String self;

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("incident_number")
        private int incidentNumber;

        @JsonProperty("created_at")
        private Date createdAt;

        private IncidentStatus status;

        private String title;

        @JsonProperty("pending_actions")
        private Object pendingActions;

        @JsonProperty("incident_key")
        private String incidentKey;

        private FieldDto service;

        private Object assignments;

        @JsonProperty("assigned_via")
        private String assignedVia;

        private Object acknowledgements;

        @JsonProperty("last_status_change_at")
        private Date lastStatusChangeAt;

        @JsonProperty("last_status_change_by")
        private Object lastStatusChangeBy;

        @JsonProperty("first_trigger_log_entry")
        private FieldDto firstTriggerLogEntry;

        @JsonProperty("escalation_policy")
        private FieldDto escalationPolicy;

        private List<FieldDto> teams;

        private FieldDto priority;

        private String urgency;

        @JsonProperty("resolve_reason")
        private ResolveReason resolveReason;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ResolveReason {
            
            @Builder.Default
            private String type = "merge_resolve_reason";

            private FieldDto incident;
        }

        @JsonProperty("alert_counts")
        private AlertCount alertCounts;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AlertCount {

            private int all;

            private int resolved;

            private int triggered;
            
        }

        @JsonProperty("conference_bridge")
        private Object conferenceBridge;

        private Object body;

        private Object occurrence;

        @JsonProperty("incidents_responders")
        private Object incidentsResponders;

        @JsonProperty("responder_requests")
        private Object responderRequests;   

        @JsonProperty("basic_alert_grouping")
        private Object basicAlertGrouping;

        @JsonProperty("alert_grouping")
        private Object alertGrouping;

        private String description;

        @JsonProperty("is_mergeable")
        private Boolean isMergeable;

        private Date rowCreatedAt;

        private Date rowUpdatedAt;

        private Date rowDeletedAt;

        public Incident toDomain(IncidentDto incidentDto) {
            return Incident.builder().id(incidentDto.getDbId())
                                     .clientId(incidentDto.getClientId())
                                     .incidentId(incidentDto.getId())
                                     .key(incidentDto.getIncidentKey())
                                     .number(incidentDto.getIncidentNumber())
                                     .incidentCreatedAt(DateUtil.convertDatetoLocalDateTimeUTC(incidentDto.getCreatedAt()))
                                     .lastStatusChangeAt(ObjectUtils.isEmpty(incidentDto.getLastStatusChangeAt()) ? null : DateUtil.convertDatetoLocalDateTimeUTC(incidentDto.getLastStatusChangeAt()))
                                     .status(incidentDto.getStatus().name())
                                     .type(incidentDto.getType())
                                     .priority(ObjectUtils.isEmpty(incidentDto.getPriority()) ? null : incidentDto.getPriority().getType())
                                     .urgency(incidentDto.getUrgency())
                                     .title(incidentDto.getTitle())
                                     .description(incidentDto.getDescription())
                                     .summary(incidentDto.getSummary())
                                     .resolveReason(ObjectUtils.isEmpty(incidentDto.getResolveReason()) ? null : incidentDto.getResolveReason().getType())
                                     .htmlUrl(incidentDto.getHtmlUrl())
                                     .serviceId(incidentDto.getService().getId())
                                     .serviceSummary(incidentDto.getService().getSummary())
                                     .serviceType(incidentDto.getService().getType())
                                     .escalationPolicyId(incidentDto.getEscalationPolicy().getId())
                                     .escalationPolicySummary(incidentDto.getEscalationPolicy().getSummary())
                                     .escalationPolicyType(incidentDto.getEscalationPolicy().getType())
                                     .triggerId(incidentDto.getFirstTriggerLogEntry().getId())
                                     .triggerSummary(incidentDto.getFirstTriggerLogEntry().getSummary())
                                     .triggerType(incidentDto.getFirstTriggerLogEntry().getType())
                                     .alertCountResolved(incidentDto.getAlertCounts().getResolved())
                                     .alertCountTriggered(incidentDto.getAlertCounts().getTriggered())
                                     .alertTotalCount(incidentDto.getAlertCounts().getAll())
                                     .rowCreatedAt(ObjectUtils.isEmpty(incidentDto.getRowCreatedAt()) ? null : DateUtil.convertToLocalDate(DateUtil.convertDatetoLocalDateTimeUTC(incidentDto.getRowCreatedAt())))
                                     .rowUpdatedAt(ObjectUtils.isEmpty(incidentDto.getRowUpdatedAt()) ? null : DateUtil.convertToLocalDate(DateUtil.convertDatetoLocalDateTimeUTC(incidentDto.getRowUpdatedAt())))
                                     .rowDeletedAt(ObjectUtils.isEmpty(incidentDto.getRowDeletedAt()) ? null : DateUtil.convertToLocalDate(DateUtil.convertDatetoLocalDateTimeUTC(incidentDto.getRowDeletedAt())))
                                     .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiceDto {
        
        private String id;
    
        @Setter
        private Long dbId;
    
        @Setter
        private Long clientId;
    
        private String teamId;
    
        private String summary;
    
        @Builder.Default
        private String type = "service";
    
        private String self;
    
        @JsonProperty("html_url")
        private String htmlUrl;
    
        private String name;
    
        private String description;
    
        @JsonProperty("auto_resolve_timeout")
        @Builder.Default
        private Integer autoResolveTimeout = 14400;
    
        @JsonProperty("acknowledgement_timeout")
        @Builder.Default
        private Integer acknowledgementTimeout = 1800;
    
        @JsonProperty("created_at")
        private Date createdAt;
    
        @Builder.Default
        private ServiceStatus status = ServiceStatus.ACTIVE;
    
        @JsonProperty("last_incident_timestamp")
        private String lastIncidentTimestamp;
    
        @JsonProperty("escalation_policy")
        private FieldDto escalationPolicy;
    
        @JsonProperty("response_play")
        private FieldDto responsePlay;
    
        private Object teams;
    
        private Object integrations;
    
        @JsonProperty("incident_urgency_rule")
        private Object incidentUrgencyRule;
    
        @JsonProperty("support_hours")
        private Object supportHours;
    
        @JsonProperty("scheduled_actions")
        private Object scheduledActions;
    
        private Object addons;
    
        @JsonProperty("alert_creation")
        private String alertCreation;
    
        @JsonProperty("alert_grouping_parameters")
        private Object alertGroupingParameters;

        public PagerDutyService toDomain(ServiceDto serviceDto) {
            return PagerDutyService.builder().id(serviceDto.getDbId())
                                      .clientId(serviceDto.getClientId())
                                      .serviceId(serviceDto.getId())
                                      .name(serviceDto.getName())
                                      .description(serviceDto.getDescription())
                                      .status(serviceDto.getStatus().name())
                                      .build();
        }
    }
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IncidentMetricsDto {

        private String id;

        @Setter
        private Long dbId;

        @Setter
        private Long clientId;

        @JsonProperty("team_id")
        private String teamId;

        @JsonProperty("service_id")
        private String serviceId;

        @JsonProperty("created_at")
        private Date createdAt;

        @JsonProperty("resolved_at")
        private Date resolvedAt;

        private String urgency;

        private Boolean major;

        @JsonProperty("priority_id")
        private String priorityId;

        @JsonProperty("priority_name")
        private String priorityName;

        @JsonProperty("priority_order")
        private int priorityOrder;

        @JsonProperty("seconds_to_resolve")
        private int secondsToResolve;

        @JsonProperty("seconds_to_first_ack")
        private int secondsToFirstAck;

        @JsonProperty("seconds_to_engage")
        private int secondsToEngage;

        @JsonProperty("seconds_to_mobilize")
        private int secondsToMobilize;

        @JsonProperty("engaged_seconds")
        private int engagedSeconds;

        @JsonProperty("engaged_user_count")
        private int engagedUserCount;

        @JsonProperty("escalation_count")
        private int escalationCount;

        @JsonProperty("assignment_count")
        private int assignmentCount;

        @JsonProperty("business_hour_interruptions")
        private int businessHourInterruptions;

        @JsonProperty("sleep_hour_interruptions")
        private int sleepHourInterruptions;

        @JsonProperty("off_hour_interruptions")
        private int offHourInterruptions;

        @JsonProperty("snoozed_seconds")
        private int snoozedSeconds;

        private String description;

        @JsonProperty("incident_number")
        private int incidentNumber;

        @JsonProperty("service_name")
        private String serviceName;

        @JsonProperty("user_defined_effort_seconds")
        private Object userDefinedEfforSeconds;

        @JsonProperty("team_name")
        private Object teamName;

        public IncidentMetrics toDomain(IncidentMetricsDto incidentMetricsDto) {
        
            return IncidentMetrics.builder().id(incidentMetricsDto.getDbId())
                                            .incidentId(incidentMetricsDto.getId())
                                            .clientId(incidentMetricsDto.getClientId())
                                            .teamId(incidentMetricsDto.getTeamId())
                                            .serviceId(incidentMetricsDto.getServiceId())
                                            .metricsCreatedAt(DateUtil.convertDatetoLocalDateTimeUTC(incidentMetricsDto.getCreatedAt()))
                                            .resolvedAt(ObjectUtils.isEmpty(incidentMetricsDto.getResolvedAt()) ? null : DateUtil.convertDatetoLocalDateTimeUTC(incidentMetricsDto.getResolvedAt()))
                                            .urgency(incidentMetricsDto.getUrgency())
                                            .major(incidentMetricsDto.getMajor())
                                            .priorityName(incidentMetricsDto.getPriorityName())
                                            .priorityOrder(incidentMetricsDto.getPriorityOrder())
                                            .secondsToResolve(incidentMetricsDto.getSecondsToResolve())
                                            .secondsToFirstAck(incidentMetricsDto.getSecondsToFirstAck())
                                            .secondsToEngage(incidentMetricsDto.getSecondsToEngage())
                                            .secondsToMobilize(incidentMetricsDto.getSecondsToMobilize())
                                            .engagedSeconds(incidentMetricsDto.getEngagedSeconds())
                                            .engagedUserCount(incidentMetricsDto.getEngagedUserCount())
                                            .escalationCount(incidentMetricsDto.getEscalationCount())
                                            .assignmentCount(incidentMetricsDto.getAssignmentCount())
                                            .businessHourInterruptions(incidentMetricsDto.getBusinessHourInterruptions())
                                            .sleepHourInterruptions(incidentMetricsDto.getSleepHourInterruptions())
                                            .offHourInterruptions(incidentMetricsDto.getOffHourInterruptions())
                                            .snoozedSeconds(incidentMetricsDto.getSnoozedSeconds())
                                            .build();
        }
    }
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LogEntryDto {
        
        private String id;

        @Setter
        private Long clientId;
        
        private String summary;

        private String type;

        private String self;

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("created_at")
        private Date createdAt;

        private FieldDto channel;

        private FieldDto agent;

        private String note;

        private Object contexts;

        private FieldDto service;

        private FieldDto user;

        private FieldDto incident;

        private List<FieldDto> teams;

        @JsonProperty("event_details")
        private Object eventDetails;

        private Object assignees;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class FieldDto {

            private String id;

            private String type;

            private String summary;

            private String self;

            @JsonProperty("html_url")
            private String htmlUrl;       
        }

        public IncidentLogEntry toDomin(LogEntryDto logEntryDto) {
            return IncidentLogEntry.builder()
                                   .clientId(logEntryDto.getClientId())
                                   .entryId(logEntryDto.getId())
                                   .entryCreatedAt(DateUtil.convertDatetoLocalDateTimeUTC(logEntryDto.getCreatedAt()))
                                   .summary(logEntryDto.getSummary())
                                   .type(logEntryDto.getType())
                                   .htmlUrl(logEntryDto.getHtmlUrl())
                                   .agentId(ObjectUtils.isEmpty(logEntryDto.getAgent()) ? null : logEntryDto.getAgent().getId())
                                   .agentSummary(ObjectUtils.isEmpty(logEntryDto.getAgent()) ? null : logEntryDto.getAgent().getSummary())
                                   .agentType(ObjectUtils.isEmpty(logEntryDto.getAgent()) ? null : logEntryDto.getAgent().getType())
                                   .channelType(ObjectUtils.isEmpty(logEntryDto.getChannel()) ? null : logEntryDto.getChannel().getType())
                                   .incidentId(ObjectUtils.isEmpty(logEntryDto.getIncident()) ? null : logEntryDto.getIncident().getId())
                                   .incidentSummary(ObjectUtils.isEmpty(logEntryDto.getIncident()) ? null : logEntryDto.getIncident().getSummary())
                                   .incidentType(ObjectUtils.isEmpty(logEntryDto.getIncident()) ? null : logEntryDto.getIncident().getType())
                                   .serviceId(ObjectUtils.isEmpty(logEntryDto.getService()) ? null : logEntryDto.getService().getId())
                                   .serviceType(ObjectUtils.isEmpty(logEntryDto.getService()) ? null : logEntryDto.getService().getType())
                                   .serviceSummary(ObjectUtils.isEmpty(logEntryDto.getService()) ? null : logEntryDto.getService().getSummary())
                                   .build();
        }
    }
}
