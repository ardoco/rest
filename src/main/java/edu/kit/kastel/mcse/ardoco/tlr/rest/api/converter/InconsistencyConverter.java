package edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.MissingTextForModelElementInconsistency;
import org.eclipse.collections.api.list.ImmutableList;

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
            if (inconsistency.getType().equals("MissingModelInstance")) {
                // cast inconsistency to MissingModelInstanceInconsistency#
                MissingModelInstanceInconsistency mmiInconsistency =
                        (MissingModelInstanceInconsistency) inconsistency;
                traceLinkNode.put("sentenceNumber", mmiInconsistency.getSentenceNumber());
            } else if (inconsistency.getType().equals("MissingTextForModelElement")) {
                // cast inconsistency to MissingTextForModelElementInconsistency
                MissingTextForModelElementInconsistency umeInconsistency =
                        (MissingTextForModelElementInconsistency) inconsistency;
                traceLinkNode.put("modelElementId", umeInconsistency.getModelInstanceUid());
            }
            arrayNode.add(traceLinkNode);
        }
        return objectMapper.writeValueAsString(arrayNode);
    }
}
