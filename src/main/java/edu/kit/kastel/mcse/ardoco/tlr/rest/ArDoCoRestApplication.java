/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * Main application class for the ArDoCo Trace Link Recovery REST API.
 */
@OpenAPIDefinition(info = @Info(title = "ArDoCo: Trace Link Recovery", description = "provides functionality to run ArDoCoTLR and provide results"))
@EnableAsync
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ArDoCoRestApplication {

    /** Default constructor for the ArDoCoRestApplication. */
    public ArDoCoRestApplication() {
        // Default constructor
    }

    /**
     * Main method to run the ArDoCo Trace Link Recovery REST API application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ArDoCoRestApplication.class, args);

    }
}
