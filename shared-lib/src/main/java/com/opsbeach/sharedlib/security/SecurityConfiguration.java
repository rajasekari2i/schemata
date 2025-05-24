package com.opsbeach.sharedlib.security;

import com.opsbeach.sharedlib.exception.ExceptionResponseCreator;
import com.opsbeach.sharedlib.response.ResponseMessage;
import com.opsbeach.sharedlib.service.App2AppService;
import com.opsbeach.sharedlib.service.AuthService;
import com.opsbeach.sharedlib.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfiguration {

    private static final String[] AUTH_WHITELIST = {
            "/v1/auth/**",            
            "/user/actuator/health",
            "/connect/actuator/health",
            "/virima/actuator/health",
            "/actuator/health",
            "/v1/github/signin/callback",
            "/v1/github/market-place-events",
            "/v1/schema/organization"
    };

    private final App2AppService app2AppService;
    private final ResponseMessage responseMessage;
    private final ApplicationConfig applicationConfig;
    private final AuthService authService;
    private final AuthenticationProvider authenticationProvider;
    private final ExceptionResponseCreator exceptionResponseCreator;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    public SecurityConfiguration(AuthenticationProvider authenticationProvider, ExceptionResponseCreator exceptionResponseCreator,
                                 ResponseMessage responseMessage, App2AppService app2AppService, ApplicationConfig applicationConfig,
                                 AuthService authService, AuthenticationConfiguration authenticationConfiguration) {
        this.app2AppService = app2AppService;
        this.responseMessage = responseMessage;
        this.applicationConfig = applicationConfig;
        this.authService = authService;
        this.authenticationProvider = authenticationProvider;
        this.exceptionResponseCreator = exceptionResponseCreator;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        //return authenticationConfiguration.getAuthenticationManager();
        return authenticationConfiguration.getAuthenticationManager();
    }

    /*protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }*/

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var jwtAuthenticationFilter = new JwtAuthenticationFilter(exceptionResponseCreator, responseMessage, app2AppService, applicationConfig, authService);
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManagerBean());
        http.headers().frameOptions().disable(); // Added to enable viewing H2 database in EC2 console
        http.cors()
            .and()
            .csrf()
            .disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            //.authorizeHttpRequests((authz) -> authz.anyRequest().authenticated())
            .authorizeRequests()
            .requestMatchers(AUTH_WHITELIST).permitAll()
            //.antMatchers(AUTH_WHITELIST).permitAll()
            .anyRequest().authenticated();
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        //web.ignoring().antMatchers("/v1/auth/logout");
        return (web) -> web.ignoring().requestMatchers("/v1/auth/logout");
    }

    @Bean
    public CorsFilter corsFilter() {
        final var source = new UrlBasedCorsConfigurationSource();
        final var config = new CorsConfiguration();
        config.addAllowedMethod(HttpMethod.GET);
        config.addAllowedMethod(HttpMethod.PUT);
        config.setAllowCredentials(Boolean.TRUE);
        config.addAllowedMethod(HttpMethod.HEAD);
        config.addAllowedMethod(HttpMethod.POST);
        config.addAllowedMethod(HttpMethod.PATCH);
        config.addAllowedMethod(HttpMethod.DELETE);
        config.addAllowedMethod(HttpMethod.OPTIONS);
        config.addAllowedHeader(Constants.ASTERISK_SYMBOL);
        config.setAllowedOriginPatterns(Collections.singletonList(Constants.ASTERISK_SYMBOL));
        source.registerCorsConfiguration(Constants.FORWARD_SLASH + Constants.ASTERISK_SYMBOL + Constants.ASTERISK_SYMBOL, config);
        return new CorsFilter(source);
    }
}
