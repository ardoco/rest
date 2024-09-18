package io.github.ardoco.rest.api.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class ArdocoResultResponseDeserializer extends StdDeserializer<ArdocoResultResponse> {

    public ArdocoResultResponseDeserializer() {
        this(null);
    }

    public ArdocoResultResponseDeserializer(Class<?> vc) {
        super(vc);
    }


    @Override
    public ArdocoResultResponse deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);

        // Safely extract fields, using default values or null checks
        String projectId = node.has("projectId") && !node.get("projectId").isNull() ? node.get("projectId").asText() : null;
        String message = node.has("message") && !node.get("message").isNull() ? node.get("message").asText() : null;
        String statusString = node.has("status") && !node.get("status").isNull() ? node.get("status").asText() : null;
        HttpStatus status = statusString != null ? HttpStatus.valueOf(statusString) : null;

        // Treat `samSadTraceLinks` as raw JSON and store it as a string
        JsonNode samSadTraceLinksNode = node.get("samSadTraceLinks");
        String samSadTraceLinks = samSadTraceLinksNode != null ? samSadTraceLinksNode.toString() : null;

        // Create the ArdocoResultResponse object and return it
        ArdocoResultResponse resultResponse = new ArdocoResultResponse();
        resultResponse.setProjectId(projectId);
        resultResponse.setMessage(message);
        resultResponse.setStatus(status);
        resultResponse.setSamSadTraceLinks(samSadTraceLinks);

        return resultResponse;
    }

}

