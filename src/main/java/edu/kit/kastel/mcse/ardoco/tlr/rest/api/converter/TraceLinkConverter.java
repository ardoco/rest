package edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;

import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;

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
    public static String convertListOfSadCodeTraceLinksToJsonString(List<TraceLink<SentenceEntity, CodeCompilationUnit>> sadCodeTraceLinks) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();

        System.out.println("hi from sadcodeTraceLinkConverter");

        for (TraceLink<SentenceEntity, CodeCompilationUnit> traceLink : sadCodeTraceLinks) {
            var sentenceEntity = traceLink.getFirstEndpoint();
            ObjectNode traceLinkNode = objectMapper.createObjectNode();
            traceLinkNode.put("sentenceNumber", sentenceEntity.getId());
            traceLinkNode.put("codeCompilationUnit", traceLink.getSecondEndpoint().getId());

            if (traceLink instanceof TransitiveTraceLink<?, ?, ?> transitive) {
                System.out.println("transitive trace link found");
                var first = transitive.getFirstTraceLink().getSecondEndpoint();
                if (first instanceof ArchitectureEntity architectureEntity) {
                    traceLinkNode.put("modelElementId", architectureEntity.getId());
                }
            }
            arrayNode.add(traceLinkNode);
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
    public static String convertListOfSamCodeTraceLinksToJsonString(List<TraceLink<ArchitectureEntity, CodeCompilationUnit>> samCodeTraceLinks) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();


        for (TraceLink<ArchitectureEntity, CodeCompilationUnit> traceLink : samCodeTraceLinks) {
            ObjectNode traceLinkNode = objectMapper.createObjectNode();

            traceLinkNode.put("modelElementId", traceLink.getSecondEndpoint().getId());
            //traceLinkNode.put("modelElementName", traceLink.getSecondEndpoint().getName());
            traceLinkNode.put("codeElementId", traceLink.getFirstEndpoint().getId()); // Assuming the first endpoint is the code element
            //traceLinkNode.put("codeElementName", traceLink.getFirstEndpoint().toString());
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
    public static String convertListOfSadSamTraceLinksToJsonString(List<TraceLink<SentenceEntity, ArchitectureEntity>> sadSamTraceLinks) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (TraceLink<SentenceEntity, ArchitectureEntity> traceLink : sadSamTraceLinks) {
            ObjectNode traceLinkNode = objectMapper.createObjectNode();
            traceLinkNode.put("sentenceNumber", traceLink.getFirstEndpoint().getId());
            traceLinkNode.put("modelElementUid", traceLink.getSecondEndpoint().getId());
            //traceLinkNode.put("confidence", traceLink.getConfidence());
            arrayNode.add(traceLinkNode);
        }
        return objectMapper.writeValueAsString(arrayNode);
    }
}