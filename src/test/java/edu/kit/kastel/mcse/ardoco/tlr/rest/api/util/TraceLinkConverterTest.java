/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.TraceLinkConverter;

class TraceLinkConverterTest { //TODO

    @Test
    void testConvertListOfTraceLinksToJSONString_withEmptyListSamSad() throws JsonProcessingException {
        // Test the conversion with an empty list
        List<SadCodeTraceLink> traceLinks = Collections.emptyList();
        String jsonString = TraceLinkConverter.convertListOfSadCodeTraceLinksToJsonString(traceLinks);

        // Expected empty JSON array
        String expectedJson = "[]";

        // Assert the result
        assertEquals(expectedJson, jsonString);
    }
}
