package com.opsbeach.virima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.opsbeach.virima.core.BaseRepositoryImpl;

@SpringBootApplication(scanBasePackages = "com.opsbeach")
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class)
public class VirimaApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirimaApplication.class, args);
	}

}
