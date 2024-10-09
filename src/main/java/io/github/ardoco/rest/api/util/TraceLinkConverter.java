package io.github.ardoco.rest.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.*;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;

import java.util.List;

/**
 * Utility class for converting ArDoCo trace links into JSON string representations.
 * This class includes methods for handling various types of trace links such as
 * {@link SadCodeTraceLink}, {@link SamCodeTraceLink}, and {@link SadSamTraceLink}.
 */

public final class TraceLinkConverter {

    private TraceLinkConverter() {}

    /**
     * Converts a list of {@link SadCodeTraceLink} objects to a JSON string representation.
     * Each {@link SadCodeTraceLink} in the JSON array contains a sentence number and the
     * string representation of a {@link CodeCompilationUnit}.
     *
     * @param sadCodeTraceLinks the list of {@link SadCodeTraceLink} objects to convert
     * @return a JSON string representation of the list of {@link SadCodeTraceLink} objects
     * @throws JsonProcessingException if the conversion to JSON fails
     */
    public static String convertListOfSadCodeTraceLinksToJSONString(List<SadCodeTraceLink> sadCodeTraceLinks) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
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

    /**
     * Converts a list of {@link SamCodeTraceLink} objects to a JSON string representation.
     * Each {@link SamCodeTraceLink} in the JSON array contains information about the model
     * element and code element including their IDs and names.
     *
     * @param samCodeTraceLinks the list of {@link SamCodeTraceLink} objects to convert
     * @return a JSON string representation of the list of {@link SamCodeTraceLink} objects
     * @throws JsonProcessingException if the conversion to JSON fails
     */
    public static String convertListOfSamCodeTraceLinksToJSONString(List<SamCodeTraceLink> samCodeTraceLinks) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
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

    /**
     * Converts a list of {@link SadSamTraceLink} objects to a JSON string representation.
     * Each {@link SadSamTraceLink} in the JSON array includes the sentence number, model
     * element unique identifier, and confidence score.
     *
     * @param sadSamTraceLinks the list of {@link SadSamTraceLink} objects to convert
     * @return a JSON string representation of the list of {@link SadSamTraceLink} objects
     * @throws JsonProcessingException if the conversion to JSON fails
     */
    public static String convertListOfSadSamTraceLinksToJSONString(List<SadSamTraceLink> sadSamTraceLinks) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
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