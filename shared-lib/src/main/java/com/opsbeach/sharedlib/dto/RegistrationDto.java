package com.opsbeach.sharedlib.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opsbeach.sharedlib.utils.OnboardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDto {
    private String username;
    private String password;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    private String Gender;
    private String mobile;
    @JsonProperty("onboard_status")
    private OnboardStatus onboardStatus;
    private long clientId;
    private String oldPassword;
    @JsonProperty("company_name")
    private String companyName;
}
