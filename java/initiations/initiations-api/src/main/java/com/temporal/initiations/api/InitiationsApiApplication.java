package com.temporal.initiations.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Initiations API service.
 *
 * This Spring Boot application provides REST API endpoints for submitting
 * payment files for processing. It uses the Temporal SDK client to start
 * File Workflow executions.
 *
 * Workers are disabled (start_workers=false) as this service is a client-only
 * service. Actual workflow execution happens in the workers service.
 */
@SpringBootApplication
public class InitiationsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(InitiationsApiApplication.class, args);
    }
}
