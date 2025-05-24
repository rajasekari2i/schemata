package com.opsbeach.user.entity;

import com.opsbeach.sharedlib.utils.OnboardStatus;
import com.opsbeach.user.base.BaseModel;
import com.opsbeach.user.utils.Constants;
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
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * Holds details of an user.
 * </p>
 */
@Entity
@Table(name = Constants.TABLE_USER)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FilterDef(name = "clientFilter", parameters = {@ParamDef(name = "clientId", type = Long.class)})
@Filter(name = "clientFilter", condition = ":clientId = client_id")
public class User extends BaseModel {
    @Column(name = "client_id")
    private Long clientId;
    private LocalDate dob;
    @Column(name = "email_id")
    private String emailId;
    private String username;
    @Column(unique = true)
    private String mobile;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "middle_name")
    private String middleName;
    @Column(name = "last_name")
    private String lastName;
    @NotNull
    private String password;
    @Column(name = "forget_password_key")
    private String forgetPasswordKey;
    private String gender;
    private String type;
    @Column(name = "time_zone")
    private String timeZone;
    @Enumerated(EnumType.STRING)
    @Column(name = "onboard_status")
    private OnboardStatus onboardStatus;
    @Column(name = "failure_attempts")
    private Integer failureAttempts;
    @Column(name = "lock_time")
    private LocalDateTime lockTime;
    @Column(name = "account_locked")
    private Boolean accountLocked;
    @Column(name = "verification_token_sent_time")
    private LocalDateTime verificationTokenSentTime;
    @Column(name = "password_changed_time")
    private LocalDateTime passwordChangedTime;
    @Column(name = "old_password")
    private String oldPassword;
    private String otp;
    @Column(name = "otp_sent_time")
    private LocalDateTime otpSentTime;
}
