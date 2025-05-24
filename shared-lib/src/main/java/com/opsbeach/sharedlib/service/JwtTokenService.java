package com.opsbeach.sharedlib.service;

import com.opsbeach.sharedlib.dto.JwtDto;
import com.opsbeach.sharedlib.security.ApplicationConfig;
import com.opsbeach.sharedlib.utils.Constants;
import com.opsbeach.sharedlib.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@Service
public class JwtTokenService {

    private static final String JWT_ACCESS = "jwt-access";

    private final App2AppService app2AppService;
    private final ApplicationConfig applicationConfig;

    public JwtTokenService(App2AppService app2AppService, ApplicationConfig applicationConfig) {
        this.app2AppService = app2AppService;
        this.applicationConfig = applicationConfig;
    }

    private String getUserServiceBaseUrl() {
        return applicationConfig.getUser().get(Constants.BASE_URL);
    }

    public JwtDto getByAccessToken(String authenticationToken) {
        JwtDto requestJwtDto = new JwtDto();
        requestJwtDto.setAccessToken(authenticationToken);

        return app2AppService.httpPost(StringUtil.constructStringEmptySeparator(getUserServiceBaseUrl(),
                applicationConfig.getUser().get(JWT_ACCESS)), app2AppService.setHeaders(new HashMap<>(), requestJwtDto), JwtDto.class);
        /*return app2AppService.httpGet(StringUtil.constructStringEmptySeparator(getUserServiceBaseUrl(),
                applicationConfig.getUser().get(JWT_ADD),"/",authenticationToken,"/access"), app2AppService.setHeaders(new HashMap<>(), null), JwtDto.class);*/
    }
}
