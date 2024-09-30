package io.github.ardoco.rest;

import io.github.ardoco.rest.api.util.ArchitectureModelTypeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ArchitectureModelTypeConverter architectureModelTypeConverter;

    public WebConfig(ArchitectureModelTypeConverter architectureModelTypeConverter) {
        this.architectureModelTypeConverter = architectureModelTypeConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(architectureModelTypeConverter);
    }
}
