/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter;

import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.ModelEntityAbsentFromTextInconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.TextEntityAbsentFromModelInconsistency;

public final class InconsistencyConverter {

    private InconsistencyConverter() {
        // Prevent instantiation
    }

    public static String convertInconsistencyToJsonString(ImmutableList<Inconsistency> inconsistencies) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (Inconsistency inconsistency : inconsistencies) {
            ObjectNode traceLinkNode = objectMapper.createObjectNode();
            traceLinkNode.put("type", inconsistency.getType());
            traceLinkNode.put("reason", inconsistency.getReason());
            if (inconsistency.getType().equals("TextEntityAbsentFromModel")) {
                TextEntityAbsentFromModelInconsistency teamInconsistency = (TextEntityAbsentFromModelInconsistency) inconsistency;
                traceLinkNode.put("sentenceNumber", teamInconsistency.getSentenceNumber());
            } else if (inconsistency.getType().equals("ModelEntityAbsentFromText")) {
                ModelEntityAbsentFromTextInconsistency meatInconsistency = (ModelEntityAbsentFromTextInconsistency) inconsistency;
                traceLinkNode.put("modelElementId", meatInconsistency.getModelInstanceUid());
            }
            arrayNode.add(traceLinkNode);
        }
        return objectMapper.writeValueAsString(arrayNode);
    }
}
