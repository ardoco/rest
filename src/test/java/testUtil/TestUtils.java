package testUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import io.github.ardoco.rest.api.api_response.ErrorResponse;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.util.Messages;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

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
        // Parse the response body into a JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());
        JsonNode traceLinkNode = rootNode.get("traceLinks");

        String projectId = rootNode.has("requestId") ? rootNode.get("requestId").asText() : null;
        String message = rootNode.has("message") ? rootNode.get("message").asText() : null;
        String status = rootNode.has("status") ? rootNode.get("status").asText() : null;
        String traceLinkType = rootNode.has("traceLinkType") ? rootNode.get("traceLinkType").asText() : null;

        assert projectId != null;
        assert message != null;
        assert status != null;
        assert traceLinkType != null;

        // Treat traceLinks as raw JSON and store it as a string
        String traceLinks = (traceLinkNode == null || traceLinkNode.isNull()) ? null : traceLinkNode.toString();

        // Create the ArdocoResultResponse object and return it
        ArdocoResultResponse resultResponse = new ArdocoResultResponse();
        resultResponse.setRequestId(projectId);
        resultResponse.setMessage(message);
        resultResponse.setStatus(HttpStatus.valueOf(status));
        resultResponse.setTraceLinkType(TraceLinkType.valueOf(traceLinkType));

        if (traceLinkNode != null) {
            resultResponse.setTraceLinks(traceLinks);
        }
        return resultResponse;
    }

    /*
    Tests for when starting the pipeline for the first time, when no results from previous runs exist in the database yet.
     */
    public static void testStartPipeline_new(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(Messages.RESULT_IS_BEING_PROCESSED, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNull(response.getTraceLinks());
    }

    /*
    Tests for when starting the pipeline for the first time, when no results from previous runs exist in the database yet.
     */
    public static void testStartPipeline_resultIsInDatabase(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(Messages.RESULT_IS_READY, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNotNull(response.getTraceLinks());
    }


    /*
    Tests when trying to get the result, but the result is not ready yet
     */
    public static void testGetResult_notReady(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(Messages.RESULT_NOT_READY, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNull(response.getTraceLinks());
    }

    /*
Tests when trying to get the result, but the result is not ready yet
 */
    public static void testGetResult_ready(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(Messages.RESULT_IS_READY, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNotNull(response.getTraceLinks());
    }

    /*
    Tests when waiting for the result, but the result is not ready yet after the waiting period
     */
    public static void testWaitForResult_notReady(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(Messages.REQUEST_TIMED_OUT, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNull(response.getTraceLinks());
    }

    /*
    Tests when waiting for the result, and the result is ready
     */
    public static void testWaitForResult_ready(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(Messages.RESULT_IS_READY, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNotNull(response.getTraceLinks());
    }

    public static void testRunPipelineAndWaitForResult_notReady(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(Messages.REQUEST_TIMED_OUT_START_AND_WAIT, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNull(response.getTraceLinks());
    }

    public static void testInvalidRequestId(ResponseEntity<ErrorResponse> responseEntity, String invalidId) {
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals(errorResponse.getStatus(), responseEntity.getStatusCode());
        assertEquals(Messages.noResultForKey(invalidId), errorResponse.getMessage());
    }

    public static void testsForHandelingEmptyFiles(ResponseEntity<ErrorResponse> responseEntity) {
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        ErrorResponse response = responseEntity.getBody();
        assertEquals(response.getMessage(), Messages.FILE_NOT_FOUND);
        assertEquals(responseEntity.getStatusCode(), response.getStatus());
        assertNotNull(response.getTimestamp());
    }

}
