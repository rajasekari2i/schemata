package com.opsbeach.user.service;

import com.opsbeach.sharedlib.dto.GenericResponseDto;
import com.opsbeach.sharedlib.dto.LoginDto;
import com.opsbeach.sharedlib.dto.RegistrationDto;
import com.opsbeach.sharedlib.dto.RoleDto;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.RecordNotFoundException;
import com.opsbeach.sharedlib.exception.UnAuthorizedException;
import com.opsbeach.sharedlib.exception.UserExistException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.response.SuccessCode;
import com.opsbeach.sharedlib.security.SecurityUtil;
import com.opsbeach.sharedlib.service.EmailService;
import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.sharedlib.utils.DateUtil;
import com.opsbeach.sharedlib.utils.FutureUtil;
import com.opsbeach.sharedlib.utils.OnboardStatus;
import com.opsbeach.sharedlib.utils.StringUtil;
import com.opsbeach.user.base.BaseService;
import com.opsbeach.user.base.specification.IdSpecifications;
import com.opsbeach.user.dto.ClientDto;
import com.opsbeach.user.dto.UserDetailDto;
import com.opsbeach.user.entity.User;
import com.opsbeach.user.entity.UserRole;
import com.opsbeach.user.mapper.UserMapper;
import com.opsbeach.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * <p>
 * Implements the CRUD operation of an user.
 * </p>
 */
@Slf4j
@Service
public class UserService extends BaseService<User, UserDetailDto> {

    private static final String LOGIN_FAILED_MESSAGE = "Login failed. Invalid username or password";
    private static final String PASSWORD_SHOULD_NOT_BE_EMPTY = "Password should not be empty";
    private static final String ACCOUNT_LOCKED_MESSAGE = "Your Account has been locked due to 3 failed attempts. It will be unlocked after 24 hours.";
    private final FutureUtil futureUtil;
    private final Executor fhirExecutor;
    private final RoleService roleService;
    private final ClientService clientService;
    private final UserRepository userRepository;
    private final ResponseMessage responseMessage;
    private final UserRoleService userRoleService;
    private final IdSpecifications<User> userIdSpecifications;
    private final UserMapper userMapper;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final EmailService emailService;

    public UserService(UserRepository userRepository, UserMapper userMapper, FutureUtil futureUtil, Executor fhirExecutor, IdSpecifications<User> userIdSpecifications, RoleService roleService, ResponseMessage responseMessage,
                       ClientService clientService, UserRoleService userRoleService, EmailService emailService) {
        super(userRepository, userMapper, userIdSpecifications);
        this.futureUtil = futureUtil;
        this.roleService = roleService;
        this.fhirExecutor = fhirExecutor;
        this.clientService = clientService;
        this.userRepository = userRepository;
        this.responseMessage = responseMessage;
        this.userRoleService = userRoleService;
        this.userIdSpecifications = userIdSpecifications;
        this.userMapper = userMapper;
        this.emailService = emailService;
    }

    @Override
    public void doPatch(User incomingUser, User toUpdateUser) {
        if (Objects.nonNull(incomingUser.getOnboardStatus())) {
            toUpdateUser.setOnboardStatus(incomingUser.getOnboardStatus());
        }
        if (Objects.nonNull(incomingUser.getFirstName())) {
            toUpdateUser.setFirstName(incomingUser.getFirstName());
        }
        if (Objects.nonNull(incomingUser.getDob())) {
            toUpdateUser.setDob(incomingUser.getDob());
        }
        if (Objects.nonNull(incomingUser.getEmailId())) {
            toUpdateUser.setEmailId(incomingUser.getEmailId());
        }

        if (Objects.nonNull(incomingUser.getType())) {
            toUpdateUser.setType(incomingUser.getType());
        }

        if (Objects.nonNull(incomingUser.getClientId())) {
            toUpdateUser.setClientId(incomingUser.getClientId());
        }

        continuePatch(incomingUser, toUpdateUser);
        toUpdateUser.setForgetPasswordKey(incomingUser.getForgetPasswordKey());
        toUpdateUser.setCreatedBy(incomingUser.getCreatedBy());
        toUpdateUser.setUpdatedBy(incomingUser.getUpdatedBy());
    }

    private void continuePatch(User incomingUser, User toUpdateUser) {
        if (Objects.nonNull(incomingUser.getPassword())) {
            toUpdateUser.setPassword(incomingUser.getPassword());
        }
        if (Objects.nonNull(incomingUser.getVerificationTokenSentTime())) {
            toUpdateUser.setVerificationTokenSentTime(incomingUser.getVerificationTokenSentTime());
        }
        if (Objects.nonNull(incomingUser.getAccountLocked())) {
            toUpdateUser.setAccountLocked(incomingUser.getAccountLocked());
        }
        if (Objects.nonNull(incomingUser.getFailureAttempts())) {
            toUpdateUser.setFailureAttempts(incomingUser.getFailureAttempts());
        }
        if (Objects.nonNull(incomingUser.getLockTime())) {
            toUpdateUser.setLockTime(incomingUser.getLockTime());
        }
        if (Objects.nonNull(incomingUser.getPasswordChangedTime())) {
            toUpdateUser.setPasswordChangedTime(incomingUser.getPasswordChangedTime());
        }
        if (Objects.nonNull(incomingUser.getOldPassword())) {
            toUpdateUser.setOldPassword(incomingUser.getOldPassword());
        }
        if (Objects.nonNull(incomingUser.getOtp())) {
            toUpdateUser.setOtp(incomingUser.getOtp());
        }
        if (Objects.nonNull(incomingUser.getOtpSentTime())) {
            toUpdateUser.setOtpSentTime(incomingUser.getOtpSentTime());
        }
    }

    @Override
    public void validateAdd(UserDetailDto userDetailDto) {
        userDetailDto.setIsDeleted(Boolean.FALSE);
    }

    @Override
    public void validatePatch(UserDetailDto incomingDto) {
        incomingDto.setCreatedBy(incomingDto.getId());
        incomingDto.setUpdatedBy(incomingDto.getId());
    }

    public static String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public static Boolean isPasswordEqual(String password1, String password2) {
        return passwordEncoder.matches(password1, password2);
    }

    public GenericResponseDto registration(RegistrationDto registrationDto, String clientName, String role) {
        checkUserExists(registrationDto.getUsername());
        ClientDto clientDto = clientService.getClient(clientName);
        registrationDto.setClientId(clientDto.getId());
        // var encodedPassword = getEncodedPassword(registrationDto.getPassword());
        // registrationDto.setPassword(encodedPassword);
        // registrationDto.setOldPassword(encodedPassword);
        UserDetailDto userDetailDto = userMapper.registerToDto(registrationDto);
        userDetailDto.setAccountLocked(Boolean.FALSE);
        userDetailDto.setFailureAttempts(0);
        saveUserRole(add(userDetailDto), role);
        return GenericResponseDto.builder().status(Constants.SUCCESS).build();
    }

    public GenericResponseDto registerCompany(String companyName) {
        var clientDto = clientService.add(companyName);
        var userDetailDto = findById(SecurityUtil.getLoggedInUserDetail().getId());
        userDetailDto.setClientId(clientDto.getId());
        userDetailDto.setOnboardStatus(OnboardStatus.ONBOARDED);
        patch(userDetailDto);
        return GenericResponseDto.builder().status(Constants.SUCCESS).build();
    }

    public String sendOtp(String email) {
        var otp = Integer.toString(new Random().nextInt(100000, 1000000)); // generate 6 digit otp.
        var userDataDto = findByUsername(email);
        userDataDto.setOtp(otp);
        emailService.sendMail(email, "Schemata Labs login OTP", 
                     StringUtil.constructStringEmptySeparator("Your OTP for login is ", otp, " Valid for 15 minutes"));
        userDataDto.setOtpSentTime(DateUtil.currentDateTime());
        patch(userDataDto);
        return responseMessage.getSuccessMessage(SuccessCode.OTP_SENT_SUCCESSFULLY);
    }

    public UserDetailDto login(LoginDto loginDto) {
        log.info("User login with username");
        UserDetailDto userDataDto = findByUsername(loginDto.getUsername());

        if (userDataDto.getAccountLocked().equals(Boolean.TRUE)) {
            if (DateUtil.currentDateTime().isAfter(DateUtil.plusDays(userDataDto.getLockTime(), 1))) {
                userDataDto.setFailureAttempts(0);
                userDataDto.setAccountLocked(Boolean.FALSE);
                userDataDto.setLockTime(DateUtil.currentDateTime());
                patch(userDataDto);
            } else {
                throw new UnAuthorizedException(ErrorCode.INVALID_PASSWORD, ACCOUNT_LOCKED_MESSAGE);
            }
        }

        if (Objects.isNull(loginDto.getPassword())) {
            throw new UnAuthorizedException(ErrorCode.INVALID_PASSWORD, PASSWORD_SHOULD_NOT_BE_EMPTY);
        }

        // if (isPasswordEqual(loginDto.getPassword(), userDataDto.getPassword()).equals(Boolean.FALSE)) {
        if (Objects.isNull(userDataDto.getOtp()) || Boolean.FALSE.equals(loginDto.getPassword().equals(userDataDto.getOtp()))) {
            userDataDto.setFailureAttempts(userDataDto.getFailureAttempts() + 1);
            if (userDataDto.getFailureAttempts() >= 3) {
                userDataDto.setAccountLocked(Boolean.TRUE);
                userDataDto.setLockTime(DateUtil.currentDateTime());
                patch(userDataDto);
                throw new UnAuthorizedException(ErrorCode.INVALID_PASSWORD, ACCOUNT_LOCKED_MESSAGE);
            }
            patch(userDataDto);
            throw new UnAuthorizedException(ErrorCode.INVALID_PASSWORD, LOGIN_FAILED_MESSAGE);
        }

        if (DateUtil.currentDateTime().isAfter(DateUtil.plusMinutes(userDataDto.getOtpSentTime(), 15))) {
            throw new UnAuthorizedException(ErrorCode.OTP_EXPIRED, responseMessage.getErrorMessage(ErrorCode.OTP_EXPIRED));
        }

        userDataDto.setFailureAttempts(0);
        userDataDto.setAccountLocked(Boolean.FALSE);
        patch(userDataDto);
        return userDataDto;
    }

    public void saveUserRole(UserDetailDto userDetailDto, String role) {
        List<RoleDto> roles = roleService.getRoleByName(role);
        List<CompletableFuture<Void>> userRoleServiceFutures =
                roles.stream().map(roleDto -> getUserRoleServiceFuture(userDetailDto, roleDto.getId())).collect(Collectors.toList());
        try {
            userRoleServiceFutures.forEach(futureUtil::safeGet);
        } finally {
            userRoleServiceFutures.forEach(futureUtil::tryCancelFuture);
        }
        log.info("User Role has been saved in the table");
    }

    private CompletableFuture<Void> getUserRoleServiceFuture(UserDetailDto userDetailDto, Long roleId) {
        return CompletableFuture.runAsync(() -> {
            var userRole = UserRole.builder().userId(userDetailDto.getId()).roleId(roleId).isDeleted(Boolean.FALSE).createdBy(userDetailDto.getId()).updatedBy(userDetailDto.getId()).build();
            userRoleService.save(userRole);
        }, fhirExecutor);
    }

    public UserDetailDto findByUsername(String username) {
        Specification<User> baseSpecification = userIdSpecifications.findByUsername(username);
        Optional<User> entity = userRepository.findOne(baseSpecification);
        if (entity.isEmpty()) {
            throw new RecordNotFoundException(ErrorCode.RECORD_NOT_FOUND,
                    responseMessage.getErrorMessage(ErrorCode.RECORD_NOT_FOUND,  String.format("Username: %s ", username)));
        }
        return userMapper.domainToDto(entity.get());
    }

    /**
     * Prepares UserDto based on UserDataDto for the Authorization.
     *
     * @return userDto - Details required for Authorization.
     */
    public UserDetailDto getUserDto(UserDetailDto userDetailDto) {

        /*log.info("Get User Details and save values in Cache");
        var userSessionDto = userSessionService.findByOpenSession(userDataDto.getId(), httpServletRequest, userDataDto.getUserType());
        var hashKey = userSessionDto.getSecret();
        var isAlreadyLoggedIn = cacheService.delete(hashKey);
        if (StringUtils.isEmpty(sessionId).equals(Boolean.FALSE)) {
            cacheService.save(hashKey, Constants.SESSION_ID, sessionId);
        }
        saveCacheValues(userDataDto, userSessionDto, hashKey, isAlreadyLoggedIn);
        return userService.getUserDto(userDataDto, Boolean.FALSE, userSessionDto);*/


        return userDetailDto;
    }

    /**
     * Updates User Details of a User with the details received from OneMoney.
     *
     * @param userId  - The respective User ID.
     * @param holders - Details of the User received from OneMoney.
     * @return successResponseDto - Success message if the user details has been updated properly.
     */
    /*public GenericResponseDto updateUserDetails(long userId, List<HoldersDto> holders) {
        log.info("Updating the User table");
        var holder = holders.get(0).getHolder();
        var userDto = findById(userId);
        userDto.setDob(holder.getDob());
        userDto.setGender("MALE"); // change once original value received from OneMoney. As of now Gender is not received from OneMoney.
        userDto.setPan(holder.getPan());
        if (Objects.nonNull(holder.getDob())) {
            userDto.setAge(Period.between(holder.getDob(), DateUtil.currentTimeZoneDate()).getYears());
        }
        userDto.setEmail(holder.getEmail());
        if (Objects.nonNull(holder.getCkycCompliance())) {
            userDto.setIsKycVerified(holder.getCkycCompliance().equalsIgnoreCase(Constants.TRUE_VALUE));
        }
        userDto.setType(holders.get(0).getType());
        patch(userDto);
        log.info("User Details has been updated in the Database.");
        return GenericResponseDto.builder().status(SUCCESS).build();
    }*/

    /**
     * Returns details of a user.
     *
     * @return Object - Details of user.
     */
    /*public UserDetailDto getUserDetails() {
        log.info("Fetching User Details");
        return findById(CacheUtil.getUserId());
    }*/

    /**
     * Returns details of a user by mobile number.
     *
     * @param searchValue - Mobile number of a user.
     * @return Object - Details of user.
     */
    public UserDetailDto getUserDetails(String searchValue, String type) {
        log.info("Fetching User Details");
        Specification<User> baseSpecification = userIdSpecifications.findByBatchUser();
        var userDto = findOne(baseSpecification);
        return getUserDto(userDto);
    }

    public Optional<User> userExists(String searchValue) {
        Specification<User> baseSpecification = userIdSpecifications.findByEmail(searchValue);
        return userRepository.findOne(baseSpecification);
    }

    /**
     * Check whether the user already exists in database during registration.
     *
     * @param searchValue - Mobile number or email of user.
     * @return Boolean - True (or) false.
     */
    public Object checkUserExists(String searchValue) {
        log.info("Validating whether the user is already registered");
        Optional<User> user = userExists(searchValue);
        if (user.isPresent()) {
            throw new UserExistException(ErrorCode.USER_ALREADY_EXISTS, responseMessage.getErrorMessage(ErrorCode.USER_ALREADY_EXISTS));
        }
        return Boolean.FALSE;
    }

    /*public GenericResponseDto updateUserOnboardStatus(int status) {
        log.info("Updating the User On-board status into table");
        var userDataDto = findById(CacheUtil.getUserId());
        userDataDto.setOnboardStatus(OnboardStatus.valueOf(status));
        patch(userDataDto);
        return GenericResponseDto.builder().status(Constants.SUCCESS).build();
    }*/
}
