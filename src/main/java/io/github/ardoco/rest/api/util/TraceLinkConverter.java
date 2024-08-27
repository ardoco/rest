package io.github.ardoco.rest.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;

import java.util.List;
import java.util.stream.Collectors;

public class TraceLinkConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a list of SadCodeTraceLink objects to a JSON string representation.
     *
     * @param sadCodeTraceLinks the list of SadCodeTraceLink objects to convert
     * @return a JSON string representation of the list of SadCodeTraceLink objects, or null if conversion fails
     */
    public String convertListOfTraceLinksToJSONString(List<SadCodeTraceLink> sadCodeTraceLinks) throws JsonProcessingException {
        // Convert each trace link to a list of its endpoint names
        List<List<String>> traceLinksData = sadCodeTraceLinks.stream()
                .map(traceLink -> {
                    EndpointTuple endpointTuple = traceLink.getEndpointTuple();
                    String firstEndpointName = endpointTuple.firstEndpoint().getName();
                    String secondEndpointName = endpointTuple.secondEndpoint().getName();
                    return List.of(firstEndpointName, secondEndpointName);
                })
                .collect(Collectors.toList());

        // Convert the list of lists to a JSON string
        return objectMapper.writeValueAsString(traceLinksData);
    }
}