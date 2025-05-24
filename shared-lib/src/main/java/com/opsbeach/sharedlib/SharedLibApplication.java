package com.opsbeach.sharedlib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SharedLibApplication {

    public static void main(String[] args) {
        new SpringApplication(SharedLibApplication.class).run();
    }
}