package com.opsbeach.user.dto;

import com.opsbeach.sharedlib.utils.OnboardStatus;
import com.opsbeach.user.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * Basic Details of an User.
 * </p>
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDto extends BaseDto {
    private Long clientId;
    private LocalDate dob;
    private String emailId;
    private String mobile;
    private String username;
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;
    private String forgetPasswordKey;
    private String gender;
    private String type;
    private String timeZone;
    private OnboardStatus onboardStatus;
    private Integer failureAttempts;
    private LocalDateTime lockTime;
    private Boolean accountLocked;
    private LocalDateTime verificationTokenSentTime;
    private LocalDateTime passwordChangedTime;
    private String oldPassword;
    private String otp;
    private LocalDateTime otpSentTime;
}