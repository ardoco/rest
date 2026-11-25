/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;

/**
 * Service for providing the default configuration of all configurable classes in the ArDoCo project.
 */
@Service("configurationProviderService")
public class ConfigurationProviderService {

    /**
     * Constructs a new {@code ConfigurationProviderService}.
     */
    public ConfigurationProviderService() {
        // Default constructor
    }

    /**
     * Retrieves the default configuration for all configurable classes in the ArDoCo project.
     *
     * @return a map containing the configuration keys and their default values
     * @see ConfigurationHelper#getDefaultConfigurationOptions()
     */
    public Map<String, String> getDefaultConfiguration() {
        return ConfigurationHelper.getDefaultConfigurationOptions().castToSortedMap();
    }
}
