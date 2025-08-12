/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;

/**
 * This class is needed to be able to convert the input from the API Endpoint straight into type Architecture model.
 */

@Component
public class ModelFormatTypeConverter implements Converter<String, ModelFormat> {

    @Override
    public ModelFormat convert(String source) {
        try {
            return ModelFormat.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid architecture model type: " + source, e);
        }
    }
}
