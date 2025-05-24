package com.opsbeach.analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface SlaMeterDto {
    
    @JsonProperty(value = "time_to_acknowledge")
    long getTimeToAcknowledge();

    @JsonProperty(value = "first_reply_time")
    long getFirstReplyTime();

    @JsonProperty(value = "time_to_resolve")
    long getTimeToResolve();

    @JsonProperty(value = "ticket_sla_meter")
    long getTicketSlaMeter();
}
