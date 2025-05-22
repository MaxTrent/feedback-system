package com.balancee.backendtask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendTaskApplication {
    private static final Logger logger = LoggerFactory.getLogger(BackendTaskApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Balancee-Backend-Task System");
        SpringApplication.run(BackendTaskApplication.class, args);
    }
}