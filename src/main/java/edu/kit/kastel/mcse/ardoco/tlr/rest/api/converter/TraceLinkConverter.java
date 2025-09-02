/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter;

import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;

/**
 * Utility class for converting ArDoCo trace links into JSON string representations.
 * This class includes methods for handling various types of trace links {@link TraceLink}.
 */
public final class TraceLinkConverter {

    private TraceLinkConverter() {
    }

    /**
     * Converts a list of {@link TraceLink} objects to a JSON string representation.
     * Each {@link TraceLink} in the JSON array contains a sentence number and a Model Entity.
     *
     * @param sadCodeTraceLinks the list of {@link TraceLink} objects to convert
     * @return a JSON string representation of the list of {@link TraceLink} objects
     * @throws JsonProcessingException if the conversion to JSON fails
     */
    public static String convertListOfSadCodeTraceLinksToJsonString(ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> sadCodeTraceLinks)
            throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (TraceLink<SentenceEntity, ? extends ModelEntity> traceLink : sadCodeTraceLinks) {
            var sentenceEntity = traceLink.getFirstEndpoint();
            ObjectNode traceLinkNode = objectMapper.createObjectNode();
            traceLinkNode.put("sentenceNumber", sentenceEntity.getId());
            traceLinkNode.put("codeElementId", traceLink.getSecondEndpoint().getId());
            traceLinkNode.put("codeElementName", traceLink.getSecondEndpoint().getName());

            if (traceLink instanceof TransitiveTraceLink<?, ?> transitive) {
                var first = transitive.getFirstTraceLink().getSecondEndpoint();
                if (first instanceof ArchitectureEntity architectureEntity) {
                    traceLinkNode.put("modelElementId", architectureEntity.getId());
                    if (architectureEntity.getType().isPresent()) {
                        traceLinkNode.put("modelElementName",architectureEntity.getName() + " (" + architectureEntity.getType().get() + ")");
                    }
                    else {
                        traceLinkNode.put("modelElementName", architectureEntity.getName());
                    }

                }
            }
            arrayNode.add(traceLinkNode);
        }
        return objectMapper.writeValueAsString(arrayNode);
    }

    /**
     * Converts a list of {@link TraceLink} objects to a JSON string representation.
     * Each {@link TraceLink} in the JSON array contains information about the model
     * element and code element including their IDs and names.
     *
     * @param samCodeTraceLinks the list of {@link TraceLink} objects to convert
     * @return a JSON string representation of the list of {@link TraceLink} objects
     * @throws JsonProcessingException if the conversion to JSON fails
     */
    public static String convertListOfSamCodeTraceLinksToJsonString(
            ImmutableList<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> samCodeTraceLinks) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (TraceLink<? extends ArchitectureEntity, ? extends ModelEntity> traceLink : samCodeTraceLinks) {
            ObjectNode traceLinkNode = objectMapper.createObjectNode();

            traceLinkNode.put("modelElementId", traceLink.getFirstEndpoint().getId());
            if (traceLink.getFirstEndpoint().getType().isPresent()) {
                traceLinkNode.put("modelElementName",traceLink.getFirstEndpoint().getName() + " (" + traceLink.getFirstEndpoint().getType().get() + ")");
            }
            else {
                traceLinkNode.put("modelElementName", traceLink.getFirstEndpoint().getName());
            }
            traceLinkNode.put("codeElementId", traceLink.getSecondEndpoint().getId());
            traceLinkNode.put("codeElementName", traceLink.getSecondEndpoint().getName());
            arrayNode.add(traceLinkNode);
        }
        return objectMapper.writeValueAsString(arrayNode);
    }

    /**
     * Converts a list of {@link TraceLink} objects to a JSON string representation.
     * Each {@link TraceLink} in the JSON array includes the sentence number, model
     * element unique identifier, and confidence score.
     *
     * @param sadSamTraceLinks the list of {@link TraceLink} objects to convert
     * @return a JSON string representation of the list of {@link TraceLink} objects
     * @throws JsonProcessingException if the conversion to JSON fails
     */
    public static String convertListOfSadSamTraceLinksToJsonString(ImmutableList<TraceLink<SentenceEntity, ModelEntity>> sadSamTraceLinks)
            throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (TraceLink<SentenceEntity, ModelEntity> traceLink : sadSamTraceLinks) {
            ObjectNode traceLinkNode = objectMapper.createObjectNode();
            traceLinkNode.put("sentenceNumber", traceLink.getFirstEndpoint().getId());
            if (traceLink.getSecondEndpoint().getType().isPresent()) {
                traceLinkNode.put("modelElementName",traceLink.getSecondEndpoint().getName() + " (" + traceLink.getSecondEndpoint().getType().get() + ")");
            }
            else {
                traceLinkNode.put("modelElementName", traceLink.getSecondEndpoint().getName());
            }
            traceLinkNode.put("modelElementId", traceLink.getSecondEndpoint().getId());
            arrayNode.add(traceLinkNode);
        }
        return objectMapper.writeValueAsString(arrayNode);
    }
}
