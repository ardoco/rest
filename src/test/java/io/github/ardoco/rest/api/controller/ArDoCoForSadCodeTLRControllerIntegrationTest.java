package io.github.ardoco.rest.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ardoco.rest.ArDoCoRestApplication;
import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import io.github.ardoco.rest.api.api_response.ErrorResponse;
import io.github.ardoco.rest.api.repository.RedisAccessor;
import io.github.ardoco.rest.api.util.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.web.client.TestRestTemplate;

@Testcontainers
@SpringBootTest(
        classes = ArDoCoRestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ArDoCoForSadCodeTLRControllerIntegrationTest {

    @Container
    @ServiceConnection
    static GenericContainer redis = new GenericContainer<>(DockerImageName.parse("redis:7.4.0-alpine")).withExposedPorts(6379);

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    private RedisAccessor redisAccessor;

    private static final Logger logger = LogManager.getLogger(ArDoCoForSadCodeTLRControllerIntegrationTest.class);

    /*
     * ******************************
     *
     * test runPipeLine()
     *
     * ******************************
     */
    @Test
    void shouldGetId() throws JsonProcessingException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/api/sad-code/start", HttpMethod.POST, requestEntity, String.class
        );

        assertNotNull(responseEntity.getBody());
        ArdocoResultResponse response = parseResponseEntityToArdocoResponse(responseEntity);


        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(response.getProjectId());
        assertEquals(response.getMessage(), Messages.RESULT_IS_BEING_PROCESSED, "Expected: " + Messages.RESULT_IS_BEING_PROCESSED + ", but was: " + response.getMessage());
        assertEquals(responseEntity.getStatusCode(), response.getStatus());
        assertNull(response.getTraceLinks());
        redisAccessor.deleteResult(response.getProjectId());
    }

    @Test
    void shouldHandleFileNotFoundException() {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("projectName", "nonexistentProject");
        parameters.add("inputText", new ClassPathResource("emptyFile.txt"));
        parameters.add("inputCode", new ClassPathResource("bigBlueButton/codeModel.acm"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        ResponseEntity<ErrorResponse> responseEntity = restTemplate.exchange(
                "/api/sad-code/start", HttpMethod.POST, requestEntity, ErrorResponse.class
        );

        assertEquals(responseEntity.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY); // 422 for file not found
        assertNotNull(responseEntity.getBody());

        ErrorResponse response = responseEntity.getBody();
        assertEquals(response.getMessage(), Messages.FILE_NOT_FOUND, "Expected: " + Messages.RESULT_IS_BEING_PROCESSED + ", but was: " + response.getMessage());
        assertEquals(responseEntity.getStatusCode(), response.getStatus());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        File tempFile = File.createTempFile("TestFile-", ".txt");
        tempFile.deleteOnExit();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("projectName", "emptyFileProject");
        parameters.add("inputText", new FileSystemResource(tempFile));
        parameters.add("inputCode", new ClassPathResource("bigBlueButton/codeModel.acm"));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        ResponseEntity<ErrorResponse> responseEntity = restTemplate.exchange(
                "/api/sad-code/start", HttpMethod.POST, requestEntity, ErrorResponse.class
        );

        assertEquals(responseEntity.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY); // 422 for file not found
        assertNotNull(responseEntity.getBody());

        ErrorResponse response = responseEntity.getBody();
        assertEquals(response.getMessage(), Messages.FILE_NOT_FOUND, "Expected: " + Messages.RESULT_IS_BEING_PROCESSED + ", but was: " + response.getMessage());
        assertEquals(responseEntity.getStatusCode(), response.getStatus());
        assertNotNull(response.getTimestamp());
    }

    /*
     * ******************************
     *
     * test getResult()
     *
     * ******************************
     */

    @Test
    void shouldGetResultNotReady() throws IOException, InterruptedException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/api/sad-code/start", HttpMethod.POST, requestEntity, String.class
        );

        assertNotNull(responseEntity.getBody());
        ArdocoResultResponse response = parseResponseEntityToArdocoResponse(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        String projectId = response.getProjectId();

        ResponseEntity<String> resultResponseEntity;
        do {
            TimeUnit.SECONDS.sleep(1);
            resultResponseEntity = restTemplate.getForEntity("/api/sad-code/{id}", String.class, projectId);
            ArdocoResultResponse waitingResult = parseResponseEntityToArdocoResponse(resultResponseEntity);

            if (HttpStatus.ACCEPTED == resultResponseEntity.getStatusCode()) {
                assertSame( HttpStatus.ACCEPTED, resultResponseEntity.getStatusCode());
                assertNotNull(waitingResult);
                assertNull(waitingResult.getTraceLinks());
                assertEquals(Messages.RESULT_NOT_READY, waitingResult.getMessage());
                assertEquals(waitingResult.getStatus(), resultResponseEntity.getStatusCode());
                assertEquals(projectId, waitingResult.getProjectId());
                assertNotNull(response.getProjectId());

            } else if (HttpStatus.OK == resultResponseEntity.getStatusCode()) {
                assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                assertNotNull(waitingResult.getStatus());  // Should not be null at this point
                assertEquals(Messages.RESULT_IS_READY, waitingResult.getMessage());
                assertEquals(waitingResult.getStatus(), responseEntity.getStatusCode());
                assertEquals(projectId, waitingResult.getProjectId());

                // Test to retrieve already ready result again:
                resultResponseEntity = restTemplate.getForEntity("/api/sad-code/{id}", String.class, projectId);
                waitingResult = parseResponseEntityToArdocoResponse(resultResponseEntity);
                testReadyResult(waitingResult, responseEntity);
                assertEquals(projectId, waitingResult.getProjectId());

                assertTrue(redisAccessor.deleteResult(waitingResult.getProjectId()));
            } else {
                fail();
            }

            TimeUnit.SECONDS.sleep(1);

        } while (HttpStatus.ACCEPTED == resultResponseEntity.getStatusCode());
    }

    @Test
    void shouldImediatlyReturnResultWhenRunPipeline() throws IOException, InterruptedException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/api/sad-code/start", HttpMethod.POST, requestEntity, String.class
        );

        assertNotNull(responseEntity.getBody());
        ArdocoResultResponse response = parseResponseEntityToArdocoResponse(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        String projectId = response.getProjectId();

        ResponseEntity<String> resultResponseEntity;
        do {
            TimeUnit.SECONDS.sleep(1);
            resultResponseEntity = restTemplate.getForEntity("/api/sad-code/{id}", String.class, projectId);
            ArdocoResultResponse waitingResult = parseResponseEntityToArdocoResponse(resultResponseEntity);

            if (HttpStatus.ACCEPTED == resultResponseEntity.getStatusCode()) {
                assertSame( HttpStatus.ACCEPTED, resultResponseEntity.getStatusCode());
                assertNotNull(waitingResult);
                assertNull(waitingResult.getTraceLinks());
                assertEquals(Messages.RESULT_NOT_READY, waitingResult.getMessage());
                assertEquals(waitingResult.getStatus(), resultResponseEntity.getStatusCode());
                assertEquals(projectId, waitingResult.getProjectId());
                assertNotNull(response.getProjectId());

            } else if (HttpStatus.OK == resultResponseEntity.getStatusCode()) {
                assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                assertNotNull(waitingResult.getStatus());  // Should not be null at this point
                assertEquals(Messages.RESULT_IS_READY, waitingResult.getMessage());
                assertEquals(waitingResult.getStatus(), responseEntity.getStatusCode());
                assertEquals(projectId, waitingResult.getProjectId());

                // Test to retrieve already ready result again:
                resultResponseEntity = restTemplate.exchange(
                        "/api/sad-code/start", HttpMethod.POST, requestEntity, String.class
                );
                waitingResult = parseResponseEntityToArdocoResponse(resultResponseEntity);
                testReadyResult(waitingResult, responseEntity);
                assertEquals(projectId, waitingResult.getProjectId());

                assertTrue(redisAccessor.deleteResult(waitingResult.getProjectId()));
            } else {
                fail();
            }

            TimeUnit.SECONDS.sleep(1);

        } while (HttpStatus.ACCEPTED == resultResponseEntity.getStatusCode());
    }

    @Test
    void shouldReturnResultNotFoundForInvalidId() {
        String invalidId = "invalid-project-id";
        ResponseEntity<ErrorResponse> responseEntity = restTemplate.getForEntity("/api/sad-code/{id}", ErrorResponse.class, invalidId);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals(errorResponse.getStatus(), responseEntity.getStatusCode());
        String expected = "No result with key " + invalidId + " found.";
        assertEquals(expected, errorResponse.getMessage(), "Expected: " + expected + ", but was: " + errorResponse.getMessage());
    }

    /*
     * ******************************
     *
     * test waitForResult()
     *
     * ******************************
     */

    @Test
    void shouldReturnResultWhenReady() throws InterruptedException, JsonProcessingException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB();

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "/api/sad-code/start", HttpMethod.POST, requestEntity, String.class
        );

        assertNotNull(responseEntity.getBody());
        ArdocoResultResponse response = parseResponseEntityToArdocoResponse(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        String projectId = response.getProjectId();

        ResponseEntity<String> resultResponseEntity;
        do {
            resultResponseEntity = restTemplate.getForEntity("/api/sad-code/wait/{id}", String.class, projectId);
            ArdocoResultResponse waitingResult = parseResponseEntityToArdocoResponse(resultResponseEntity);

            if (HttpStatus.ACCEPTED == resultResponseEntity.getStatusCode()) {
                assertSame( HttpStatus.ACCEPTED, resultResponseEntity.getStatusCode());
                assertNotNull(waitingResult);
                assertNull(waitingResult.getTraceLinks());
                assertEquals(Messages.REQUEST_TIMED_OUT, waitingResult.getMessage());
                assertEquals(waitingResult.getStatus(), resultResponseEntity.getStatusCode());
                assertEquals(projectId, waitingResult.getProjectId());

            } else if (HttpStatus.OK == resultResponseEntity.getStatusCode()) {
                testReadyResult(waitingResult, resultResponseEntity);

                assertEquals(projectId, waitingResult.getProjectId());

                // try to get the result right away again since it is ready
                resultResponseEntity = restTemplate.getForEntity("/api/sad-code/wait/{id}", String.class, projectId);
                waitingResult = parseResponseEntityToArdocoResponse(resultResponseEntity);
                testReadyResult(waitingResult, resultResponseEntity);
                assertEquals(projectId, waitingResult.getProjectId());
                assertTrue(redisAccessor.deleteResult(waitingResult.getProjectId()));

            } else {
                fail();
            }

        } while (HttpStatus.ACCEPTED == resultResponseEntity.getStatusCode());
    }


    /*
     * ******************************
     *
     * test waitForResult()
     *
     * ******************************
     */

    @Test
    void shouldStartPipelineAndReturnResultWhenReady() throws IOException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB();
        ResponseEntity<String> responseEntity;

        do {
            responseEntity = restTemplate.exchange("/api/sad-code/start-and-wait", HttpMethod.POST, requestEntity, String.class);
            assertNotNull(responseEntity.getBody());

            ArdocoResultResponse response = parseResponseEntityToArdocoResponse(responseEntity);

            if (HttpStatus.ACCEPTED == responseEntity.getStatusCode()) {
                assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());
                assertNull(response.getTraceLinks());  // Should be null in this case
                assertEquals(Messages.REQUEST_TIMED_OUT_START_AND_WAIT, response.getMessage());
                assertEquals(response.getStatus(), responseEntity.getStatusCode());
                assertNotNull(response.getProjectId());

            } else {
                testReadyResult(response, responseEntity);
                assertTrue(redisAccessor.deleteResult(response.getProjectId()));
            }

        } while (HttpStatus.ACCEPTED == responseEntity.getStatusCode());
    }



    private HttpEntity<MultiValueMap<String, Object>> setUpRequestParamToStartPipelineBBB() {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("projectName", "bigBlueButton");
        parameters.add("inputText", new ClassPathResource("bigBlueButton/bigbluebutton.txt"));
        parameters.add("inputCode", new ClassPathResource("bigBlueButton/codeModel.acm"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(parameters, headers);
    }

    private ArdocoResultResponse parseResponseEntityToArdocoResponse(ResponseEntity<String> responseEntity) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse the response body into a JsonNode
        JsonNode rootNode = objectMapper.readTree(responseEntity.getBody());

        // extract fields
        String projectId = rootNode.has("projectId") ? rootNode.get("projectId").asText() : null;
        String message = rootNode.has("message") ? rootNode.get("message").asText() : null;
        String statusString = rootNode.has("status") ? rootNode.get("status").asText() : null;
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

    private void testReadyResult(ArdocoResultResponse response, ResponseEntity<String> responseEntity) {
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(response.getTraceLinks());  // Should not be null at this point
        assertEquals(Messages.RESULT_IS_READY, response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
        assertNotNull(response.getProjectId());
    }

}