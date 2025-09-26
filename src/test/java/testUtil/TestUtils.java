/* Licensed under MIT 2025. */
package testUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ErrorResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.messages.ResultMessages;

public final class TestUtils {

    private TestUtils() {
    } // prevent instantiation

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
        JsonNode result = rootNode.get("result");
        JsonNode traceLinkNode = (result != null && result.has("traceLinks")) ? result.get("traceLinks") : null;
        JsonNode inconsistenciesNode = (result != null && result.has("inconsistencies")) ? result.get("inconsistencies") : null;

        String projectId = rootNode.has("requestId") ? rootNode.get("requestId").asText() : null;
        String message = rootNode.has("message") ? rootNode.get("message").asText() : null;
        String status = rootNode.has("status") ? rootNode.get("status").asText() : null;
        String traceLinkType = rootNode.has("traceLinkType") ? rootNode.get("traceLinkType").asText() : null;

        assert projectId != null;
        assert message != null;
        assert status != null;
        assert traceLinkType != null;

        // Treat traceLinks as raw JSON and store it as a string
        String traceLinks = (traceLinkNode == null || traceLinkNode.isNull()) ? "[]" : traceLinkNode.toString();
        String inconsistencies = (inconsistenciesNode == null || inconsistenciesNode.isNull()) ? "[]" : inconsistenciesNode.toString();

        // Create the ArdocoResultResponse object and return it
        ArdocoResultResponse resultResponse = new ArdocoResultResponse();
        resultResponse.setRequestId(projectId);
        resultResponse.setMessage(message);
        resultResponse.setStatus(HttpStatus.valueOf(status));
        resultResponse.setTraceLinkType(TraceLinkType.valueOf(traceLinkType));

        if (result != null && !result.isNull()) {
            resultResponse.setResult(new ArDoCoApiResult(traceLinks, inconsistencies));
        }
        return resultResponse;
    }

    /*
    Tests for when starting the pipeline for the first time, when no results from previous runs exist in the database yet.
     */
    public static void testStartPipeline_new(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(ResultMessages.RESULT_IS_BEING_PROCESSED, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNull(response.getResult());
    }

    /*
    Tests for when starting the pipeline for the first time, when no results from previous runs exist in the database yet.
     */
    public static void testStartPipeline_resultIsInDatabase(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(ResultMessages.RESULT_IS_READY, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNotNull(response.getResult());
    }

    /*
    Tests when trying to get the result, but the result is not ready yet
     */
    public static void testGetResult_notReady(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(ResultMessages.RESULT_NOT_READY, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNull(response.getResult());
    }

    /*
    Tests when trying to get the result, but the result is not ready yet
    */
    public static void testGetResult_ready(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(ResultMessages.RESULT_IS_READY, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNotNull(response.getResult());
    }

    /*
    Tests when waiting for the result, but the result is not ready yet after the waiting period
     */
    public static void testWaitForResult_notReady(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(ResultMessages.REQUEST_TIMED_OUT, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNull(response.getResult());
    }

    /*
    Tests when waiting for the result, and the result is ready
     */
    public static void testWaitForResult_ready(ArdocoResultResponse response, ResponseEntity<String> responseEntity, TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(ResultMessages.RESULT_IS_READY, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNotNull(response.getResult());
    }

    public static void testRunPipelineAndWaitForResult_notReady(ArdocoResultResponse response, ResponseEntity<String> responseEntity,
            TraceLinkType traceLinkType) {
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode(), "message: " + response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertEquals(ResultMessages.REQUEST_TIMED_OUT_START_AND_WAIT, response.getMessage(), "message: " + response.getMessage());
        assertEquals(traceLinkType, response.getTraceLinkType());
        assertNotNull(response.getRequestId());
        assertNull(response.getResult());
    }

    public static void testInvalidRequestId(ResponseEntity<ErrorResponse> responseEntity, String invalidId) {
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals(errorResponse.getStatus(), responseEntity.getStatusCode());
    }

    public static void testsForHandelingEmptyFiles(ResponseEntity<ErrorResponse> responseEntity) {
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        ErrorResponse response = responseEntity.getBody();
        assertEquals(responseEntity.getStatusCode(), response.getStatus());
        assertNotNull(response.getTimestamp());
    }

}
