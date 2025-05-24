package com.opsbeach.connect.core;

import com.opsbeach.connect.core.utils.Constants;
import com.opsbeach.sharedlib.utils.YamlPropertySourceFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Fetches value from Application-url file.
 * </p>
 */
@Configuration
@EnableConfigurationProperties
@Getter
@Setter
@ConfigurationProperties(prefix = "application")
@PropertySource(value = "classpath:application-url.yml", factory = YamlPropertySourceFactory.class)
public class App2AppConfig {
    private Map<String, String> connect = new HashMap<>();
    private Map<String, String> analytics = new HashMap<>();

    public String getIntegrationBaseUrl() {
        return this.getConnect().get(Constants.BASE_URL);
    }

    public String getTransactionBaseUrl() {
        return this.getAnalytics().get(Constants.BASE_URL);
    }
}