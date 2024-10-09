package io.github.ardoco.rest.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TraceLinkConverterTest { //TODO

    @Test
    void testConvertListOfTraceLinksToJSONString_withValidData() throws JsonProcessingException { //TODO outdated
        // Mocking Entity objects
        Entity entity1 = mock(Entity.class);
        Entity entity2 = mock(Entity.class);
        when(entity1.getName()).thenReturn("Entity1");
        when(entity2.getName()).thenReturn("Entity2");

        // Mocking EndpointTuple object
        EndpointTuple endpointTuple = mock(EndpointTuple.class);
        when(endpointTuple.firstEndpoint()).thenReturn(entity1);
        when(endpointTuple.secondEndpoint()).thenReturn(entity2);

        // Mocking SadCodeTraceLink object
        SadCodeTraceLink traceLink = new SadCodeTraceLink(endpointTuple);

        // Test the conversion
        List<SadCodeTraceLink> traceLinks = Collections.singletonList(traceLink);
        String jsonString = TraceLinkConverter.convertListOfSadCodeTraceLinksToJSONString(traceLinks);

        // Expected JSON format
        String expectedJson = "[[\"Entity1\",\"Entity2\"]]";

        // Assert the conversion result
        assertEquals(expectedJson, jsonString);
    }

    @Test
    void testConvertListOfSadCodeTraceLinksToJSONString_withMultipleLinks() throws JsonProcessingException { //TODO outdated
        // Mocking entities and endpoint tuples for multiple links
        Entity entityA1 = mock(Entity.class);
        Entity entityA2 = mock(Entity.class);
        when(entityA1.getName()).thenReturn("A1");
        when(entityA2.getName()).thenReturn("A2");

        EndpointTuple tuple1 = mock(EndpointTuple.class);
        when(tuple1.firstEndpoint()).thenReturn(entityA1);
        when(tuple1.secondEndpoint()).thenReturn(entityA2);

        Entity entityB1 = mock(Entity.class);
        Entity entityB2 = mock(Entity.class);
        when(entityB1.getName()).thenReturn("B1");
        when(entityB2.getName()).thenReturn("B2");

        EndpointTuple tuple2 = mock(EndpointTuple.class);
        when(tuple2.firstEndpoint()).thenReturn(entityB1);
        when(tuple2.secondEndpoint()).thenReturn(entityB2);

        // Creating SadCodeTraceLinks
        SadCodeTraceLink traceLink1 = new SadCodeTraceLink(tuple1);
        SadCodeTraceLink traceLink2 = new SadCodeTraceLink(tuple2);

        // Test the conversion
        List<SadCodeTraceLink> traceLinks = Arrays.asList(traceLink1, traceLink2);
        String jsonString = TraceLinkConverter.convertListOfSadCodeTraceLinksToJSONString(traceLinks);

        // Expected JSON format
        String expectedJson = "[[\"A1\",\"A2\"],[\"B1\",\"B2\"]]";

        // Assert the conversion result
        assertEquals(expectedJson, jsonString);
    }

    @Test
    void testConvertListOfTraceLinksToJSONString_withEmptyListSamSad() throws JsonProcessingException {
        // Test the conversion with an empty list
        List<SadCodeTraceLink> traceLinks = Collections.emptyList();
        String jsonString = TraceLinkConverter.convertListOfSadCodeTraceLinksToJSONString(traceLinks);

        // Expected empty JSON array
        String expectedJson = "[]";

        // Assert the result
        assertEquals(expectedJson, jsonString);
    }
}
