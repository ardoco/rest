/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.ConfigurationProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * This controller exposes an endpoint to retrieve the default configuration.
 */
@Tag(name = "Configuration Provider")
@RestController
@RequestMapping("/api")
public class ConfigurationProviderController {

    @Autowired
    private ConfigurationProviderService configurationProviderService;

    /**
     * Constructs a new {@code ConfigurationProviderController} with the specified service.
     */
    public ConfigurationProviderController() {
        // Default constructor
    }

    /**
     * Retrieves the default configuration.
     *
     * @return a ResponseEntity containing the default configuration as a Map
     * @throws InvocationTargetException if an exception occurs during method invocation
     * @throws InstantiationException    if an error occurs while instantiating a class
     * @throws IllegalAccessException    if access to a class or its constructor is denied
     */
    @Operation(summary = "Get Default Configuration", description = "Retrieves the default configuration of ardoco.")
    @GetMapping("/configuration")
    public ResponseEntity<Map<String, String>> getConfiguration() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, String> config = configurationProviderService.getDefaultConfiguration();
        return ResponseEntity.ok(config);
    }

}
