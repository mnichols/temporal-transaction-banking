package com.temporal.initiations.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for HTTP message handling.
 *
 * Configures content negotiation to properly handle XML content type
 * in requests and responses.
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    /**
     * Configures content negotiation for XML handling.
     *
     * Ensures that application/xml content type is properly recognized
     * and that string message converters can handle raw XML bodies.
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(false)
            .ignoreAcceptHeader(false)
            .useRegisteredExtensionsOnly(true)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("json", MediaType.APPLICATION_JSON);
    }
}
