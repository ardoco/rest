package io.github.ardoco.rest.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import org.eclipse.collections.api.list.ImmutableList;

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
    public String convertListOfSadCodeTraceLinksToJSONString(List<SadCodeTraceLink> sadCodeTraceLinks) throws JsonProcessingException {
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

    public String convertListOfSamCodeTraceLinksToJSONString(List<SamCodeTraceLink> samCodeTraceLinks) throws JsonProcessingException {
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (SamCodeTraceLink traceLink : samCodeTraceLinks) {
            ObjectNode traceLinkNode = objectMapper.createObjectNode();

            traceLinkNode.put("architectureElementUid", traceLink.getEndpointTuple().firstEndpoint().getId());
            traceLinkNode.put("architectureElementName", traceLink.getEndpointTuple().firstEndpoint().getName());
            traceLinkNode.put("codeCompilationUnitId", traceLink.getEndpointTuple().secondEndpoint().getId());
            traceLinkNode.put("codeCompilationUnitName", traceLink.getEndpointTuple().secondEndpoint().getName());
            arrayNode.add(traceLinkNode);
        }

        return objectMapper.writeValueAsString(arrayNode);
    }

    public String convertListOfSadSamTraceLinksToJSONString(List<SadSamTraceLink> sadSamTraceLinks) throws JsonProcessingException {

        // Create a JSON array node to hold the trace links
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (SadSamTraceLink traceLink : sadSamTraceLinks) {
            ObjectNode traceLinkNode = objectMapper.createObjectNode();
            traceLinkNode.put("sentenceNo", traceLink.getSentenceNumber());
            traceLinkNode.put("sentence", traceLink.getSentence().getText());
            traceLinkNode.put("modelElementUid", traceLink.getModelElementUid());
            traceLinkNode.put("confidence", traceLink.getConfidence());
            arrayNode.add(traceLinkNode);
        }

        return objectMapper.writeValueAsString(arrayNode);
    }
}