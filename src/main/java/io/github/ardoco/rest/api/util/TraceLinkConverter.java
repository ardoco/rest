package io.github.ardoco.rest.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;

import java.util.List;
import java.util.stream.Collectors;

public class TraceLinkConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String convertListOfTraceLinksToJSONString(List<SadCodeTraceLink> sadCodeTraceLinks) throws JsonProcessingException {
        try {
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
        } catch (JsonProcessingException e) {
            // Handle or log the exception
            System.err.println("Error converting trace links to JSON: " + e.getMessage());
            throw e;  // Re-throw the exception if necessary
        }
    }
}