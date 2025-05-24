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
 * Holds details of Tenant
 * </p>
 */
@Entity
@Table
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Client extends BaseModel {
    private String name;
    private String description;
    @Column(name = "is_onboarded")
    private boolean isOnboarded;
}