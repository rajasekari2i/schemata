package com.opsbeach.analytics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.opsbeach.analytics.core.BaseRepositoryImpl;

/**
 * <p>
 * Analytics Service Main class.
 * </p>
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.opsbeach")
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class)
public class AnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApplication.class, args);
        log.info("Analytics Service has been started.");
    }
}
