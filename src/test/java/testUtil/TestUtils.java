package testUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.util.Messages;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class TestUtils {

    private TestUtils() {} // prevent instantiation

    public static HttpEntity<MultiValueMap<String, Object>> setUpRequestParamToStartPipelineBBB() {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("projectName", "bigBlueButton");
        parameters.add("inputText", new ClassPathResource("bigBlueButton/bigbluebutton.txt"));
        parameters.add("inputCode", new ClassPathResource("bigBlueButton/codeModel.acm"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(parameters, headers);
    }

    public static ArdocoResultResponse parseResponseEntityToArdocoResponse(ResponseEntity<String> responseEntity) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse the response body into a JsonNode
        JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());

        // extract fields
        String projectId = rootNode.has("projectId") ? rootNode.get("projectId").asText() : null;
        String message = rootNode.has("message") ? rootNode.get("message").asText() : null;
        String statusString = rootNode.has("status") ? rootNode.get("status").asText() : null;
        String traceLinkType = rootNode.has("traceLinkType") ? rootNode.get("traceLinkType").asText() : null;
        HttpStatus status = statusString != null ? HttpStatus.valueOf(statusString) : null;

        JsonNode samSadTraceLinksNode = rootNode.get("samSadTraceLinks");
        String samSadTraceLinks;
        if (samSadTraceLinksNode == null || samSadTraceLinksNode.isNull()) {
            samSadTraceLinks = null;
        } else {
            samSadTraceLinks = samSadTraceLinksNode.asText();
        }

        return  new ArdocoResultResponse(projectId, status, samSadTraceLinks, message);
    }

    public static void testReadyResult(ArdocoResultResponse response, ResponseEntity<String> responseEntity) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(response.getTraceLinks());  // Should not be null at this point
        assertEquals(Messages.RESULT_IS_READY, response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertNotNull(response.getRequestId());
    }
}
