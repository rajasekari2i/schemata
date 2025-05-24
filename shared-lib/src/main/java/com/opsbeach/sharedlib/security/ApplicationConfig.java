package com.opsbeach.sharedlib.security;

import com.opsbeach.sharedlib.utils.YamlPropertySourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Getter
@Setter
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "spring")
@PropertySource(value = "classpath:application.yaml", factory = YamlPropertySourceFactory.class)
public class ApplicationConfig {
    private Integer threadingFutureTimeout;
    //private Map<String, String> redis = new HashMap<>();
    private Map<String, String> user = new HashMap<>();

    private Map<String, String> github = new HashMap<>();

    private Map<String, String> gcloud = new HashMap<>();

    private Map<String, String> smtp = new HashMap<>();
}