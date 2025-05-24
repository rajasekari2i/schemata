package com.opsbeach.user.service;

import com.opsbeach.sharedlib.dto.PermissionDto;
import com.opsbeach.sharedlib.utils.FutureUtil;
import com.opsbeach.user.base.specification.IdSpecifications;
import com.opsbeach.user.entity.Permission;
import com.opsbeach.user.entity.RolePermission;
import com.opsbeach.user.repository.PermissionRepository;
import com.opsbeach.user.repository.RolePermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * <p>
 * Operations on Permissions of an user.
 * </p>
 */
@Slf4j
@Service
public class RolePermissionService {

    private final FutureUtil futureUtil;
    private final Executor fhirExecutor;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final IdSpecifications<Permission> permissionIdSpecifications;
    private final IdSpecifications<RolePermission> rolePermissionIdSpecifications;

    public RolePermissionService(FutureUtil futureUtil, Executor fhirExecutor, RolePermissionRepository rolePermissionRepository, IdSpecifications<RolePermission> rolePermissionIdSpecifications, IdSpecifications<Permission> permissionIdSpecifications, PermissionRepository permissionRepository) {
        this.futureUtil = futureUtil;
        this.fhirExecutor = fhirExecutor;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionIdSpecifications = permissionIdSpecifications;
        this.rolePermissionIdSpecifications = rolePermissionIdSpecifications;
    }

    /**
     * Returns the list of permissions of a user based on the Role.
     *
     * @param roleId - Role ID of a user.
     * @return permissions - List of Permissions.
     */
    public List<PermissionDto> getPermissionsByRole(long roleId) {
        log.info("Fetching Permissions based for the roleId - {}", roleId);
        Specification<RolePermission> roleSpecification = rolePermissionIdSpecifications.getPermissionsByRoleId(roleId).and(rolePermissionIdSpecifications.notDeleted());
        List<RolePermission> rolePermissions = rolePermissionRepository.findAll(roleSpecification);
        List<CompletableFuture<PermissionDto>> permissionFutures = rolePermissions.stream().map(this::getRolePermissionCompletableFuture).collect(Collectors.toList());
        List<PermissionDto> permissions;
        try {
            permissions = permissionFutures.stream().map(futureUtil::safeGet).filter(Objects::nonNull).collect(Collectors.toList());
        } finally {
            permissionFutures.forEach(futureUtil::tryCancelFuture);
        }
        return permissions;
    }

    private CompletableFuture<PermissionDto> getRolePermissionCompletableFuture(RolePermission rolePermission) {
        return CompletableFuture.supplyAsync(() -> {
            Specification<Permission> permissionSpecification = permissionIdSpecifications.findById(rolePermission.getPermissionId()).and(permissionIdSpecifications.notDeleted());
            Optional<Permission> permission = permissionRepository.findOne(permissionSpecification);
            return permission.map(value -> PermissionDto.builder().id(value.getId())
                    .operation(value.getOperation()).isDeleted(value.getIsDeleted()).build()).orElse(null);
        }, fhirExecutor);
    }
}