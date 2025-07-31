package edu.kit.kastel.mcse.ardoco.tlr.rest;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.ModelFormatTypeConverter;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ModelFormatTypeConverter modelFormatTypeConverter;

    public WebConfig(ModelFormatTypeConverter modelFormatTypeConverter) {
        this.modelFormatTypeConverter = modelFormatTypeConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(modelFormatTypeConverter);
    }
}
