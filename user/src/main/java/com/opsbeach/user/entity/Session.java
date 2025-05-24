package com.opsbeach.user.entity;

import com.opsbeach.user.base.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Holds session details of an user.
 * </p>
 */
@Entity
@Table
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Session extends BaseModel {
    @Column(name = "user_id")
    private long userId;
    private String uri;
    private String type;
    private String action;
    private String module;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "success_login")
    private Boolean successLogin;
}
