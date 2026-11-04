/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat connector configuration.
 */
@Configuration
public class TomcatConfig {

    /**
     * Configures the maximum number of locales Tomcat parses from an Accept-Language header.
     *
     * @param maxLocaleCount the configured maximum number of locales
     * @return a web server customizer
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> tomcatCustomizer(
            @Value("${server.tomcat.max-locale-count:100}") int maxLocaleCount) {
        if (maxLocaleCount < 1) {
            throw new IllegalArgumentException("server.tomcat.max-locale-count must be greater than zero");
        }

        return factory -> factory.addConnectorCustomizers((Connector connector) ->
                connector.setMaxLocaleCount(maxLocaleCount));
    }
}
