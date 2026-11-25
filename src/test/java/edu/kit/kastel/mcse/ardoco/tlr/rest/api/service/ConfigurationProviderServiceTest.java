/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ConfigurationProviderServiceTest {

    @Test
    void listAllDefaultConfigurations() {
        Map<String, String> configuration = assertDoesNotThrow(() -> new ConfigurationProviderService().getDefaultConfiguration(),
                "Default configuration cannot be returned");
        configuration.forEach((key, value) -> System.out.println(key + " = " + value));
        assertFalse(configuration.isEmpty(), "Default configuration shall not be empty");
    }
}
