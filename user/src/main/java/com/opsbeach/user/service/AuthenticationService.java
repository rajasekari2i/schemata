package com.opsbeach.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import com.opsbeach.sharedlib.dto.AuthenticationResponseDto;
import com.opsbeach.sharedlib.dto.GenericResponseDto;
import com.opsbeach.sharedlib.dto.JweDto;
import com.opsbeach.sharedlib.dto.JwtDto;
import com.opsbeach.sharedlib.dto.KeyStoreDto;
import com.opsbeach.sharedlib.dto.LoginDto;
import com.opsbeach.sharedlib.dto.RefreshTokenDto;
import com.opsbeach.sharedlib.dto.RegisterClientDto;
import com.opsbeach.sharedlib.dto.RegistrationDto;
import com.opsbeach.sharedlib.dto.RoleDto;
import com.opsbeach.sharedlib.dto.SessionDto;
import com.opsbeach.sharedlib.exception.ErrorCode;
import com.opsbeach.sharedlib.exception.InvalidDataException;
import com.opsbeach.sharedlib.exception.UnAuthorizedException;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.security.HMac;
import com.opsbeach.sharedlib.security.JweLibrary;
import com.opsbeach.sharedlib.security.RSAKeyGen;
import com.opsbeach.sharedlib.security.RSAMechanism;
import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.sharedlib.utils.DateUtil;
import com.opsbeach.sharedlib.utils.OnboardStatus;
import com.opsbeach.sharedlib.utils.StringUtil;
import com.opsbeach.user.dto.ClientDto;
import com.opsbeach.user.dto.UserDetailDto;
import com.opsbeach.user.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AuthenticationService {

    private final HMac hMac;
    private final JweLibrary jweLibrary;
    private final ClientService clientService;
    private final UserService userService;
    private final JwtService jwtService;
    private final RSAMechanism rsaMechanism;
    private final ResponseMessage responseMessage;
    private final RoleService roleService;
    private final SessionService sessionService;

    public AuthenticationService(RSAMechanism rsaMechanism,
                                 RoleService roleService, ResponseMessage responseMessage, HMac hMac,
                                 JweLibrary jweLibrary, JwtService jwtService, UserService userService,
                                 ClientService clientService, SessionService sessionService) {
        this.hMac = hMac;
        this.jweLibrary = jweLibrary;
        this.rsaMechanism = rsaMechanism;
        this.responseMessage = responseMessage;
        this.roleService = roleService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.clientService = clientService;
        this.sessionService = sessionService;
    }

    /*public JweDto decryptToken(String authToken) {
        log.info("Decrypting the token");
        try {
            //var jwtTokenDto = (JwtDto) cacheService.get(authToken, Constants.TOKEN);
            var jwtTokenDto = jwtService.getByAccessToken(authToken);
            return jweLibrary.decrypt(authToken, RSAKeyGen.getStringToKeys(jwtTokenDto.getPublicKey(), jwtTokenDto.getPrivateKey()));
        } catch (JOSEException | JsonProcessingException | ParseException jsonProcessingException) {
            throw new InvalidDataException(ErrorCode.INVALID_JSON_PARSE, responseMessage.getErrorMessage(ErrorCode.INVALID_JSON_PARSE));
        }
    }

    public String decryptClient(String encryptedClient) {
        log.info("Decrypting the tenant");
        try {
            return rsaMechanism.decrypt(encryptedClient);
        } catch (JOSEException | ParseException e) {
            log.error("Error in decrypting the Tenant - {}", e.getMessage());
            return Constants.EMPTY;
        }
    }*/

    /*private TenantDto validateTenant(String tenant) {
        log.info("Validating the incoming Tenant");
        if (StringUtil.isBlank(tenant) || StringUtil.isEmpty(tenant)) {
            throw new TenantEmptyException(ErrorCode.EMPTY_TENANT, responseMessage.getErrorMessage(ErrorCode.EMPTY_TENANT));
        }
        String tenantDetailsUrl = applicationConfig.getUserService().get(Constants.BASE_URL) + TENANT;
        return app2AppService.httpGet(tenantDetailsUrl, app2AppService.setHeaders(App2AppService.formTenantHeader(tenant), null), TenantDto.class);
    }*/

    private UserDetailDto getUser(String username) {
        log.info("Fetching the Details of the User [{}]", username);

        return userService.findByUsername(username);
        /*String getUserDetailsUrl = StringUtil.constructStringEmptySeparator(getUserServiceBaseUrl(), applicationConfig.getUser().get(USER), "?username=", username);
        return app2AppService.httpGet(getUserDetailsUrl, app2AppService.setHeaders(null, null), UserDto.class);*/
    }

    private ClientDto getClient(long clientId) {
        log.info("Fetching the Details of the Client [{}]", clientId);
        return clientService.findById(clientId);
        /*String getUserDetailsUrl = StringUtil.constructStringEmptySeparator(getUserServiceBaseUrl(), applicationConfig.getUser().get(CLIENT), "/", String.valueOf(clientId));
        return app2AppService.httpGet(getUserDetailsUrl, app2AppService.setHeaders(new HashMap<>(), null), ClientDto.class);*/
    }

    public ClientDto registerClient(RegisterClientDto registerClientDto) {
        log.info("Client Registration name: [{}] ", registerClientDto.getName());
        var clientDto =  ClientDto.builder().name(registerClientDto.getName()).description(registerClientDto.getDescription()).build();
        return clientService.add(clientDto);
    }

    public ClientDto registerClient(String workEmail) {
        var clientName = workEmail.substring(workEmail.indexOf("@")+1, workEmail.lastIndexOf("."));
        log.info("Client Registration name: [{}] ", clientName);
        return clientService.add(clientName);
    }

    public GenericResponseDto registerUser(RegistrationDto registrationDto, String client) {
        log.info("User registration username:  [{}] ", registrationDto.getUsername());
        return userService.registration(registrationDto, client, "USER");
        /*return app2AppService.httpPost(StringUtil.constructStringEmptySeparator(getUserServiceBaseUrl(),
                applicationConfig.getUser().get(USER_REGISTER)), app2AppService.setHeaders(App2AppService.formClientHeader(client), registrationDto), Object.class);*/
    }

    public GenericResponseDto registerUser(RegistrationDto registrationDto) {
        if (registrationDto.getOnboardStatus().equals(OnboardStatus.DEMO_USER)) {
            registrationDto.setCompanyName(com.opsbeach.user.base.Constants.ADMIN_CLIENT_NAME);
        }
        clientService.add(registrationDto.getCompanyName());
        log.info("Client Registration name: [{}] ", registrationDto.getCompanyName());
        return registerUser(registrationDto, registrationDto.getCompanyName());
    }

    public String sendOtp(String email) {
        log.info("Creating OTP for user [{}]", email);
        return userService.sendOtp(email);
    }
    public AuthenticationResponseDto login(LoginDto loginDto) {
        log.info("Customer [{}] log in with OTP", loginDto.getUsername());
        /*var userDto =  app2AppService.httpPost(StringUtil.constructStringEmptySeparator(getUserServiceBaseUrl(),
                applicationConfig.getUser().get(USER_LOGIN)), app2AppService.setHeaders(new HashMap<>(), loginDto), UserDto.class);*/
        var userDetailDto = userService.login(loginDto);
        try {
            return authenticationResponse(userDetailDto, Boolean.FALSE);
        } catch (JOSEException | JsonProcessingException jsonProcessingException) {
            throw new InvalidDataException(ErrorCode.INVALID_JSON_PARSE, responseMessage.getErrorMessage(ErrorCode.INVALID_JSON_PARSE));
        }
    }

    public AuthenticationResponseDto githubAuthentication() {
        var userDetailDto = getUser(com.opsbeach.user.base.Constants.ADMIN_USER_NAME);
        try {
            return authenticationResponse(userDetailDto, Boolean.FALSE);
        } catch (JOSEException | JsonProcessingException jsonProcessingException) {
            throw new InvalidDataException(ErrorCode.INVALID_JSON_PARSE, responseMessage.getErrorMessage(ErrorCode.INVALID_JSON_PARSE));
        }
    }

    public AuthenticationResponseDto authenticationResponse(UserDetailDto userDto, Boolean isRefreshToken) throws JOSEException, JsonProcessingException {
        log.info("Get Authentication response for the User [{}]", userDto.getUsername());
        var clientDto = getClient(userDto.getClientId());
        var jweDto = JweDto.builder().userId(userDto.getId()).isRefresh(Boolean.FALSE).client(clientDto.getName()).username(userDto.getUsername()).build();
        var keyStoreDto = RSAKeyGen.getPublicAndPrivateKey();
        String accessToken = jweLibrary.encrypt(jweDto, keyStoreDto.getPublicKey());
        var token = AuthenticationResponseDto.Token.builder().accessToken(accessToken).refreshToken(refreshResponse(userDto, clientDto, keyStoreDto.getPublicKey())).expiresIn(2592000).tokenType(Constants.BEARER).build();
        if (isRefreshToken.equals(Boolean.FALSE)) {
            var jwtTokenDto = formJwtTokenDto(token, keyStoreDto);
            jwtService.add(jwtTokenDto);
            //cacheService.save(token.getAccessToken(), Constants.TOKEN, jwtTokenDto);
        }
        return AuthenticationResponseDto.builder().token(token).isOnboarded(clientDto.isOnboarded()).build();
    }

    private String refreshResponse(UserDetailDto userDto, ClientDto clientDto, RSAPublicKey publicKey) throws JsonProcessingException, JOSEException {
        log.info("Get Authentication response for the User [{}]", userDto.getUsername());
        //ClientDto clientDto = getClient(userDto.getClientId());
        var jweDto = JweDto.builder().userId(userDto.getId()).isRefresh(Boolean.TRUE).client(clientDto.getName()).username(userDto.getUsername()).build();
        return jweLibrary.encrypt(jweDto, publicKey);
    }

    private JwtDto formJwtTokenDto(AuthenticationResponseDto.Token token, KeyStoreDto keyStoreDto) {
        Base64.Encoder encoder = Base64.getEncoder();
        return JwtDto.builder().accessToken(token.getAccessToken()).refreshToken(token.getRefreshToken())
                .publicKey(encoder.encodeToString(keyStoreDto.getPublicKey().getEncoded()))
                .privateKey(encoder.encodeToString(keyStoreDto.getPrivateKey().getEncoded()))
                .expireAt(DateUtil.plusDays(DateUtil.currentDateTime(), 30))
                .build();
    }

    public AuthenticationResponseDto verifyRefreshToken(RefreshTokenDto refreshTokenDto) throws ParseException, JOSEException, JsonProcessingException {
        //var jwtDto = (JwtDto) cacheService.get(SecurityUtil.getHashKey(), Constants.TOKEN);
        var jwtDto = jwtService.getByRefreshToken(refreshTokenDto.getRefreshToken());

        var refreshJweDto = jweLibrary.decrypt(refreshTokenDto.getRefreshToken(), RSAKeyGen.getStringToKeys(jwtDto.getPublicKey(), jwtDto.getPrivateKey()));
        if (refreshJweDto.getIsRefresh().equals(Boolean.FALSE)) {
            throw new UnAuthorizedException(ErrorCode.INVALID_REFRESH_TOKEN, responseMessage.getErrorMessage(ErrorCode.INVALID_REFRESH_TOKEN));
        }
        var userDto = getUser(refreshJweDto.getUsername());
        if (Objects.isNull(userDto)) {
            throw new UnAuthorizedException(ErrorCode.INVALID_USER, responseMessage.getErrorMessage(ErrorCode.INVALID_USER));
        }
        var hoursBetweenTwoTime = DateUtil.hoursBetweenTime(jwtDto.getExpireAt(), DateUtil.currentDateTime());
        if (hoursBetweenTwoTime > 24) {
            throw new UnAuthorizedException(ErrorCode.INVALID_REFRESH_TOKEN, responseMessage.getErrorMessage(ErrorCode.INVALID_REFRESH_TOKEN));
        }
        if (userDto.getIsDeleted().equals(Boolean.TRUE)) {
            throw new UnAuthorizedException(ErrorCode.INVALID_USER, responseMessage.getErrorMessage(ErrorCode.INVALID_USER));
        }
        var accessJweDto = jweLibrary.decrypt(refreshTokenDto.getAccessToken(), RSAKeyGen.getStringToKeys(jwtDto.getPublicKey(), jwtDto.getPrivateKey()));
        if (!accessJweDto.getUsername().equals(refreshJweDto.getUsername())) {
            throw new UnAuthorizedException(ErrorCode.INVALID_USER, responseMessage.getErrorMessage(ErrorCode.INVALID_USER));
        }
        if (!jwtDto.getRefreshToken().equals(refreshTokenDto.getRefreshToken())) {
            throw new UnAuthorizedException(ErrorCode.INVALID_REFRESH_TOKEN, responseMessage.getErrorMessage(ErrorCode.INVALID_REFRESH_TOKEN));
        }
        //userDto.setSecret(CacheUtil.getSecret());
        var authenticationResponseDto = authenticationResponse(userDto, Boolean.TRUE);
        authenticationResponseDto.getToken().setRefreshToken(refreshTokenDto.getRefreshToken());
        jwtDto.setAccessToken(authenticationResponseDto.getToken().getAccessToken());
        //cacheService.save(userDto.getSecret(), Constants.TOKEN, jwtDto);
        jwtService.add(jwtDto);
        return authenticationResponseDto;
    }

    public AuthenticationResponseDto refresh(RefreshTokenDto refreshTokenDto) {
        log.info("Generating refresh token");
        try {
            return verifyRefreshToken(refreshTokenDto);
        } catch (ParseException | JOSEException | JsonProcessingException exception) {
            throw new UnAuthorizedException(ErrorCode.INVALID_REFRESH_TOKEN, responseMessage.getErrorMessage(ErrorCode.INVALID_REFRESH_TOKEN));
        }
    }


    public Boolean logout(String authToken) {
        log.info("Logging out the user from the system");
        if (!authToken.startsWith(Constants.PREFIX) || StringUtil.isEmpty(authToken).equals(Boolean.TRUE)) {
            throw new UnAuthorizedException(ErrorCode.UNABLE_VERIFY_ACCESS_TOKEN, responseMessage.getErrorMessage(ErrorCode.UNABLE_VERIFY_ACCESS_TOKEN));
        }
        var accessToken = authToken.substring(Constants.PREFIX.length());
        //var jweDto = decryptToken(accessToken);
        //var hashKey = SecurityUtil.getHashKey();
        /*if (CacheUtil.checkAlreadyLoggedIn(hashKey).equals(Boolean.TRUE)) {
            //cacheService.save(hashKey, Constants.IS_ALREADY_LOGGED_IN, Boolean.FALSE);

            throw new LoggedOutException(ErrorCode.ALREADY_LOGGED_OUT_ERROR, responseMessage.getErrorMessage(ErrorCode.ALREADY_LOGGED_OUT_ERROR));
        }*/
        return jwtService.delete(accessToken);
    }

    public UserDto findByUsername(String username) {
        var userDetailDto = userService.findByUsername(username);
        List<RoleDto> roleDtos = roleService.getRoleByUserId(userDetailDto.getId());
        UserDto userDto = mapperUser(userDetailDto);
        userDto.setRoles(roleDtos);
        return userDto;
    }
    private UserDto mapperUser(UserDetailDto userDetailDto) {
        return UserDto.builder()
                .id(userDetailDto.getId())
                .userType(userDetailDto.getType())
                .clientId(userDetailDto.getClientId())
                .email(userDetailDto.getEmailId())
                .mobile(userDetailDto.getMobile())
                .isDeleted(userDetailDto.getIsDeleted())
                .onboardStatus(userDetailDto.getOnboardStatus().name())
                .username(userDetailDto.getUsername())
                .timeZone(userDetailDto.getTimeZone())
                .build();
    }

    public JwtDto addJwt(JwtDto jwtDto) {
        return jwtService.add(jwtDto);
    }

    public JwtDto getByAccessToken(String authenticationToken) {
        return jwtService.getByAccessToken(authenticationToken);
    }

    public JwtDto getByRefreshToken(String refreshToken) {
        return jwtService.getByRefreshToken(refreshToken);
    }

    public Boolean deleteByAccessToken(String accessToken) {
        return jwtService.delete(accessToken);
    }

    public SessionDto addSession(SessionDto sessionDto) {
        return sessionService.add(sessionDto);
    }
}
