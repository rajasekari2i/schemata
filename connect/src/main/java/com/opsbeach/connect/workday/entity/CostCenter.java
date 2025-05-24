package com.opsbeach.connect.workday.entity;

import com.opsbeach.connect.core.BaseModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * Pillar table
 * </p>
 */
@Entity
@Table(name = "cost_center")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CostCenter extends BaseModel {
        
    private String name;

    private String description;

    private String status;
}