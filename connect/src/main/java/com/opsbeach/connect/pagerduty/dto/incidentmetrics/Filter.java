package com.opsbeach.connect.pagerduty.dto.incidentmetrics;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class Filter {

    @Setter
    @JsonProperty("created_at_start")
    private LocalDateTime createdAtStart;

    @JsonProperty("created_at_end")
    private LocalDateTime createdAtEnd;
}
