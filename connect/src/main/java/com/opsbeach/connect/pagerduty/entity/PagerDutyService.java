package com.opsbeach.connect.pagerduty.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.pagerduty.dto.PagerdutyResponseDto.ServiceDto;
import com.opsbeach.connect.pagerduty.enums.ServiceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "pager_duty_service")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PagerDutyService extends BaseModel {

    @Column(name = "service_id")
    private String serviceId;

    private String name;

    private String description;

    @Column(name = "data_tier")
    private String dataTier;

    @Column(name = "service_tier")
    private String serviceTier;

    @Column(name = "alert_channel")
    private String alertChannel;

    private Long teamId;

    private String runbook;

    @Column(name = "deploy_runbook")
    private String deployRunbook;

    private String status;

    public ServiceDto toDto(PagerDutyService service) {
        return ServiceDto.builder().clientId(service.getClientId())
                                   .id(service.getServiceId())
                                   .dbId(service.getId())
                                   .name(service.getName())
                                   .description(service.getDescription())
                                   .status(ServiceStatus.valueOf(service.getStatus()))
                                   .build();
    }
}
