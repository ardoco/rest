/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.TraceLinkConverter;

public class TraceLinkConverterTest {

    @Test
    void testConvertListOfTraceLinksToJSONString_withEmptyListSamSad() throws JsonProcessingException {
        // Create an empty Eclipse Collections ImmutableList
        ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks = Lists.immutable.empty();

        String jsonString = TraceLinkConverter.convertListOfSadCodeTraceLinksToJsonString(traceLinks);

        // Expected empty JSON array
        String expectedJson = "[]";

        // Assert the result
        assertEquals(expectedJson, jsonString);
    }
}
