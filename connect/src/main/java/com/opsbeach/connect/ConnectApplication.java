package com.opsbeach.connect;

import com.opsbeach.connect.core.BaseRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * <p>
 * Main class.
 * </p>
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.opsbeach")
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class)
public class ConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConnectApplication.class, args);
        log.info("OpsBeach Connect has been started.");
    }
}