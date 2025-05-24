package com.opsbeach.connect.pagerduty.dto.incidentmetrics;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class RequestBody {

    @Setter
    private Filter filters;

    private Integer limit;

    private String order;

    @JsonProperty("order_by")
    private String orderBy;
    
}
