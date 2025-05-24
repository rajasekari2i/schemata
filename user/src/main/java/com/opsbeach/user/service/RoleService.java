package com.opsbeach.user.service;

import com.opsbeach.sharedlib.dto.RoleDto;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.utils.FutureUtil;
import com.opsbeach.user.base.specification.IdSpecifications;
import com.opsbeach.user.entity.Role;
import com.opsbeach.user.entity.UserRole;
import com.opsbeach.user.repository.RoleRepository;
import com.opsbeach.user.repository.UserRoleRepository;
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
 * Operations on Roles of an user.
 * </p>
 */
@Slf4j
@Service
public class RoleService {

    private final FutureUtil futureUtil;
    private final Executor fhirExecutor;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionService rolePermissionService;
    private final IdSpecifications<UserRole> userRoleIdSpecifications;
    private final IdSpecifications<Role> roleIdSpecifications;
    private final ResponseMessage responseMessage;

    public RoleService(FutureUtil futureUtil, Executor fhirExecutor, RoleRepository roleRepository, IdSpecifications<UserRole> userRoleIdSpecifications,
                       UserRoleRepository userRoleRepository, RolePermissionService rolePermissionService, IdSpecifications<Role> roleIdSpecifications,
                       ResponseMessage responseMessage) {
        this.futureUtil = futureUtil;
        this.fhirExecutor = fhirExecutor;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionService = rolePermissionService;
        this.userRoleIdSpecifications = userRoleIdSpecifications;
        this.roleIdSpecifications = roleIdSpecifications;
        this.responseMessage = responseMessage;
    }

    /**
     * Return the role of a user by role id.
     *
     * @param roleId - Role ID of a user.
     * @return Optional Role.
     */
    public Optional<Role> getRoleById(long roleId) {
        return roleRepository.findById(roleId);
    }

    /**
     * To get the roles by user ID.
     *
     * @param userId - User Id
     * @return roleDto - Roles and Permissions of a user.
     */
    public List<RoleDto> getRoleByUserId(long userId) {
        log.info("Fetching Roles by user Id - {}", userId);
        Specification<UserRole> userRoleSpecification = userRoleIdSpecifications.findByUserId(userId);
        List<UserRole> userRoles = userRoleRepository.findAll(userRoleSpecification);
        List<CompletableFuture<RoleDto>> roleFutures = userRoles.stream().map(this::getRoleCompletableFuture).collect(Collectors.toList());
        List<RoleDto> roleDtos;
        try {
            roleDtos = roleFutures.stream().map(futureUtil::safeGet).filter(Objects::nonNull).collect(Collectors.toList());
        } finally {
            roleFutures.forEach(futureUtil::tryCancelFuture);
        }
        return roleDtos;
    }

    private CompletableFuture<RoleDto> getRoleCompletableFuture(UserRole userRole) {
        return CompletableFuture.supplyAsync(() -> {
            if (Objects.nonNull(userRole)) {
                Optional<Role> roleOptional = getRoleById(userRole.getRoleId());
                if (roleOptional.isEmpty()) {
                    throw new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND_ID,
                            responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND_ID, "Role Id : " + userRole.getRoleId()));
                }
                Role role = roleOptional.get();
                return RoleDto.builder()
                        .id(role.getId()).name(role.getName())
                        .description(role.getDescription()).isDeleted(role.getIsDeleted())
                        .permissions(rolePermissionService.getPermissionsByRole(role.getId())).build();
            } else {
                return null;
            }
        }, fhirExecutor);
    }

    private CompletableFuture<RoleDto> getRoleCompletableFuture(Role role) {
        return CompletableFuture.supplyAsync(() -> {
            if (Objects.nonNull(role)) {
                return RoleDto.builder()
                        .id(role.getId()).name(role.getName())
                        .description(role.getDescription()).isDeleted(role.getIsDeleted())
                        .permissions(rolePermissionService.getPermissionsByRole(role.getId())).build();
            } else {
                return null;
            }
        }, fhirExecutor);
    }

    public List<RoleDto> getRoleByName(String name) {
        log.info("Fetching Roles by user Id - {}", name);
        Specification<Role> roleSpecification = roleIdSpecifications.findByName(name).and(roleIdSpecifications.notDeleted());
        List<Role> roles = roleRepository.findAll(roleSpecification);
        List<CompletableFuture<RoleDto>> roleFutures = roles.stream().map(this::getRoleCompletableFuture).collect(Collectors.toList());
        List<RoleDto> roleDtos;
        try {
            roleDtos = roleFutures.stream().map(futureUtil::safeGet).filter(Objects::nonNull).collect(Collectors.toList());
        } finally {
            roleFutures.forEach(futureUtil::tryCancelFuture);
        }
        return roleDtos;
    }
}