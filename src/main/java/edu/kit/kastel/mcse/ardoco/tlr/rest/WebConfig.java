/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.ModelFormatTypeConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ModelFormatTypeConverter modelFormatTypeConverter;

    private final List<String> allowedOrigins;

    public WebConfig(ModelFormatTypeConverter modelFormatTypeConverter, @Value("${cors.allowed-origins:}") List<String> allowedOrigins) {
        this.modelFormatTypeConverter = modelFormatTypeConverter;
        this.allowedOrigins = allowedOrigins.stream().map(String::trim).filter(origin -> !origin.isEmpty()).toList();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins.isEmpty()) {
            return;
        }
        registry.addMapping("/api/**").allowedOriginPatterns(allowedOrigins.toArray(String[]::new)).allowedMethods("GET", "POST").allowCredentials(false);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(modelFormatTypeConverter);
    }
}
