package com.temporal.initiations.api.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Temporal client and workflow execution.
 *
 * This configuration provides beans for WorkflowClient and WorkflowServiceStubs
 * if they are not already auto-configured by the temporal-spring-boot-starter.
 */
@Configuration
public class TemporalConfiguration {

    /**
     * Provides WorkflowClient bean if not auto-configured.
     *
     * The temporal-spring-boot-starter typically auto-configures this bean,
     * but this fallback ensures one is available if needed.
     */
    @Bean
    @ConditionalOnMissingBean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(
            serviceStubs,
            WorkflowClientOptions.newBuilder()
                .setNamespace("initiations")
                .build()
        );
    }

    /**
     * Provides WorkflowServiceStubs if not auto-configured.
     *
     * Configures connection to the Temporal server with sensible defaults.
     * Server address should be provided via application.yaml properties.
     */
    @Bean
    @ConditionalOnMissingBean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newLocalServiceStubs();
    }
}
