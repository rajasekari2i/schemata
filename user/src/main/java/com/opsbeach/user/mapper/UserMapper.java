package com.opsbeach.user.mapper;

import com.opsbeach.sharedlib.dto.RegistrationDto;
import com.opsbeach.user.base.BaseMapper;
import com.opsbeach.user.dto.UserDetailDto;
import com.opsbeach.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * <p>
 * Converts UserDto to User Entity and vice versa.
 * </p>
 */
@Component
public class UserMapper implements BaseMapper<User, UserDetailDto> {

    @Override
    public UserDetailDto domainToDto(User user) {
        return UserDetailDto.builder().clientId(user.getClientId())
                .isDeleted(user.getIsDeleted())
                .firstName(user.getFirstName())
                .onboardStatus(user.getOnboardStatus())
                .username(user.getUsername())
                .emailId(user.getEmailId())
                .accountLocked(user.getAccountLocked())
                .lockTime(user.getLockTime())
                .mobile(user.getMobile())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .password(user.getPassword())
                .id(user.getId())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .otp(user.getOtp())
                .otpSentTime(user.getOtpSentTime())
                .failureAttempts(user.getFailureAttempts())
                .build();
    }

    @Override
    public User dtoToDomain(UserDetailDto userDetailDto) {
        return User.builder().clientId(userDetailDto.getClientId())
                .isDeleted(Boolean.FALSE)
                .firstName(userDetailDto.getFirstName())
                .lastName(userDetailDto.getLastName())
                .onboardStatus(userDetailDto.getOnboardStatus())
                .username(userDetailDto.getUsername())
                .emailId(userDetailDto.getEmailId())
                .password(userDetailDto.getPassword())
                .accountLocked(userDetailDto.getAccountLocked())
                .lockTime(userDetailDto.getLockTime())
                .mobile(userDetailDto.getMobile())
                .otp(userDetailDto.getOtp())
                .otpSentTime(userDetailDto.getOtpSentTime())
                .failureAttempts(userDetailDto.getFailureAttempts())
                .build();
    }

    public UserDetailDto registerToDto(RegistrationDto registrationDto) {
        return UserDetailDto.builder()
                .clientId(registrationDto.getClientId())
                .isDeleted(Boolean.FALSE)
                .accountLocked(Boolean.FALSE)
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .onboardStatus(registrationDto.getOnboardStatus())
                .username(registrationDto.getUsername())
                .emailId(registrationDto.getUsername())
                .password(registrationDto.getPassword())
                .mobile(registrationDto.getMobile())
                .oldPassword(registrationDto.getOldPassword())
                .build();
    }

}
