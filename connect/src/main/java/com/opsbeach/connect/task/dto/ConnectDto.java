package com.opsbeach.connect.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.connect.core.BaseDto;
import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.task.entity.Connect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectDto extends BaseDto {
    
    @Setter
    private String headers;

    @JsonProperty("service_type")
    private ServiceType serviceType;

    @Setter
    @JsonProperty("auth_type")
    private AuthType authType;

    private String domain;

    @JsonProperty("user_email")
    private String userEmail;

    @Setter
    @JsonProperty("auth_token")
    private String authToken;

    @Setter
    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("project_key")
    private String projectKey;

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("repo_organization")
    private String repoOrganization;

    public Connect toDomin(ConnectDto connectDto)  {
        return Connect.builder().id(connectDto.getId())
                                .clientId(connectDto.getClientId())
                                .headers(connectDto.getHeaders())
                                .authType(connectDto.getAuthType())
                                .domain(connectDto.getDomain())
                                .userEmail(connectDto.getUserEmail())
                                .authToken(connectDto.getAuthToken())
                                .refreshToken(connectDto.getRefreshToken())
                                .serviceType(connectDto.getServiceType())
                                .projectKey(connectDto.getProjectKey())
                                .channelId(connectDto.getChannelId())
                                .userName(connectDto.getUserName())
                                .repoOrganization(connectDto.getRepoOrganization())
                                .build();
    }
}
