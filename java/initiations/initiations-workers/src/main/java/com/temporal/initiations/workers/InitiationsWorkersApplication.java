package com.temporal.initiations.workers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Initiations Workers service.
 *
 * This Spring Boot application runs Temporal workers that execute workflows
 * and activities for the payment file processing pipeline.
 *
 * The service registers:
 * - FileWorkflow implementation
 * - BatchWorkflow implementation
 * - All required activities
 *
 * It connects to the Temporal server and listens on the "initiations" task queue.
 */
@SpringBootApplication
public class InitiationsWorkersApplication {

    public static void main(String[] args) {
        SpringApplication.run(InitiationsWorkersApplication.class, args);
    }
}
