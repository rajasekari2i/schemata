package com.opsbeach.connect.task.entity;

import com.opsbeach.connect.core.BaseModel;
import com.opsbeach.connect.core.enums.AuthType;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.connect.task.dto.ConnectDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Table
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Connect extends BaseModel {
    
    private String headers;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type")
    private AuthType authType;

    @ColumnTransformer(
        read = "pgp_sym_decrypt(domain,'"+Constants.AES_KEY+"')",
        write = "pgp_sym_encrypt(?,'"+Constants.AES_KEY+"')"
        )
    private String domain;
    
    @ColumnTransformer(
        read = "pgp_sym_decrypt(user_email,'"+Constants.AES_KEY+"')",
        write = "pgp_sym_encrypt(?,'"+Constants.AES_KEY+"')"
        )
    @Column(name = "user_email")
    private String userEmail;
    
    @ColumnTransformer(
        read = "pgp_sym_decrypt(auth_token,'"+Constants.AES_KEY+"')",
        write = "pgp_sym_encrypt(?,'"+Constants.AES_KEY+"')"
        )
    @Column(name = "auth_token")
    private String authToken;

    @ColumnTransformer(
        read = "pgp_sym_decrypt(refresh_token,'"+Constants.AES_KEY+"')",
        write = "pgp_sym_encrypt(?,'"+Constants.AES_KEY+"')"
        )
    @Column(name = "refresh_token")
    private String refreshToken;

    @ColumnTransformer(
        read = "pgp_sym_decrypt(project_key,'"+Constants.AES_KEY+"')",
        write = "pgp_sym_encrypt(?,'"+Constants.AES_KEY+"')"
        )
    @Column(name = "project_key")
    private String projectKey;

    @ColumnTransformer(
        read = "pgp_sym_decrypt(channel_id,'"+Constants.AES_KEY+"')",
        write = "pgp_sym_encrypt(?,'"+Constants.AES_KEY+"')"
        )
    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "user_name")
    private String userName;

    @Setter
    @Column(name = "repo_organization")
    private String repoOrganization;

    public ConnectDto toDto(Connect connect)  {
        return ConnectDto.builder().id(connect.getId())
                                   .clientId(connect.getClientId())
                                   .headers(connect.getHeaders())
                                   .authType(connect.getAuthType())
                                   .domain(connect.getDomain())
                                   .userEmail(connect.getUserEmail())
                                   .authToken(connect.getAuthToken())
                                   .refreshToken(connect.getRefreshToken())
                                   .serviceType(connect.getServiceType())
                                   .projectKey(connect.getProjectKey())
                                   .channelId(connect.getChannelId())
                                   .userName(connect.getUserName())
                                   .repoOrganization(connect.getRepoOrganization())
                                   .build();
    }
}
