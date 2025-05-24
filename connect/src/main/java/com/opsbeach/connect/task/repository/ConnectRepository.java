package com.opsbeach.connect.task.repository;

import java.util.Optional;

import com.opsbeach.connect.core.BaseRepository;
import com.opsbeach.connect.core.enums.ServiceType;
import com.opsbeach.connect.task.entity.Connect;

public interface ConnectRepository extends BaseRepository<Connect> {
    
    Optional<Connect> findByServiceTypeAndClientId(ServiceType serviceType, Long clientId);
}
