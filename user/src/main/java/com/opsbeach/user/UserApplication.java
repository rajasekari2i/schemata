package com.opsbeach.user;

import com.opsbeach.user.base.BaseRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * <p>
 * user Service Main class.
 * </p>
 */
@Slf4j
@SpringBootApplication(scanBasePackages = "com.opsbeach")
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class)
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        log.info("User Service has been started.");
    }
}
