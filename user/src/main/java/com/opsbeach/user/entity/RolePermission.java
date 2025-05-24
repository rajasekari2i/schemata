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
 * Join table for Role and Permission.
 * </p>
 */
@Entity
@Table(name = "role_permission")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission extends BaseModel {
    @Column(name = "role_id")
    private long roleId;
    @Column(name = "permission_id")
    private long permissionId;
}