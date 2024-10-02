package io.github.ardoco.rest.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.*;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.common.util.TraceLinkUtilities;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

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
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (SadCodeTraceLink traceLink : sadCodeTraceLinks) {
            var codeElement = (CodeCompilationUnit) traceLink.getEndpointTuple().secondEndpoint();

            if (traceLink instanceof TransitiveTraceLink transitiveTraceLink) {
                ObjectNode traceLinkNode = objectMapper.createObjectNode();
                traceLinkNode.put("sentenceNumber", ((SadSamTraceLink) transitiveTraceLink.getFirstTraceLink()).getSentenceNumber() + 1);
                traceLinkNode.put("codeCompilationUnit", codeElement.toString());
                arrayNode.add(traceLinkNode);

            } else if (traceLink.getEndpointTuple().firstEndpoint() instanceof RecommendedInstance recommendedInstance) {
                ImmutableSortedSet<Integer> sentenceNumbers = recommendedInstance.getSentenceNumbers();
                for (var sentence : sentenceNumbers) {
                    ObjectNode traceLinkNode = objectMapper.createObjectNode();
                    traceLinkNode.put("sentenceNumber", (sentence + 1));
                    traceLinkNode.put("codeCompilationUnit", codeElement.toString());
                    arrayNode.add(traceLinkNode);
                }
            }
        }
        return objectMapper.writeValueAsString(arrayNode);
    }

    public String convertListOfSamCodeTraceLinksToJSONString(List<SamCodeTraceLink> samCodeTraceLinks) throws JsonProcessingException {
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (SamCodeTraceLink traceLink : samCodeTraceLinks) {
            ObjectNode traceLinkNode = objectMapper.createObjectNode();
            EndpointTuple endpointTuple = traceLink.getEndpointTuple();
            var modelElement = endpointTuple.firstEndpoint();
            var codeElement = (CodeCompilationUnit) endpointTuple.secondEndpoint();

            traceLinkNode.put("modelElementId", modelElement.getId());
            traceLinkNode.put("modelElementName", modelElement.getName());
            traceLinkNode.put("codeElementId", codeElement.getId());
            traceLinkNode.put("codeElementName", codeElement.toString());
            arrayNode.add(traceLinkNode);
        }
        return objectMapper.writeValueAsString(arrayNode);
    }

    public String convertListOfSadSamTraceLinksToJSONString(List<SadSamTraceLink> sadSamTraceLinks) throws JsonProcessingException {
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (SadSamTraceLink traceLink : sadSamTraceLinks) {
            ObjectNode traceLinkNode = objectMapper.createObjectNode();
            traceLinkNode.put("sentenceNumber", traceLink.getSentenceNumber());
            traceLinkNode.put("modelElementUid", traceLink.getModelElementUid());
            traceLinkNode.put("confidence", traceLink.getConfidence());
            arrayNode.add(traceLinkNode);
        }
        return objectMapper.writeValueAsString(arrayNode);
    }
}