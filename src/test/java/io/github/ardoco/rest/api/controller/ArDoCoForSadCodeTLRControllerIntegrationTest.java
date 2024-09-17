package io.github.ardoco.rest.api.controller;

import io.github.ardoco.rest.ArDoCoRestApplication;
import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import io.github.ardoco.rest.api.api_response.ErrorResponse;
import io.github.ardoco.rest.api.repository.RedisAccessor;
import io.github.ardoco.rest.api.util.Messages;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

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
    void shouldGetId() {
        // "/api/sad-code/start"
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
        parameters.add("projectName", "bigBlueButton");
        parameters.add("inputText", new org.springframework.core.io.ClassPathResource("bigBlueButton/bigbluebutton.txt"));
        parameters.add("inputCode", new org.springframework.core.io.ClassPathResource("bigBlueButton/codeModel.acm"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        ResponseEntity<ArdocoResultResponse> responseEntity = restTemplate.exchange(
                "/api/sad-code/start",
                HttpMethod.POST,
                requestEntity,
                ArdocoResultResponse.class
        );

        assertNotNull(responseEntity.getBody());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);

        ArdocoResultResponse response = responseEntity.getBody();
        assertNotNull(response.getProjectId());
        assertEquals(response.getMessage(), Messages.RESULT_IS_BEING_PROCESSED, "Expected: " + Messages.RESULT_IS_BEING_PROCESSED + ", but was: " + response.getMessage());
        assertEquals(responseEntity.getStatusCode(), response.getStatus());
        assertNull(response.getSamSadTraceLinks());
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
                "/api/sad-code/start",
                HttpMethod.POST,
                requestEntity,
                ErrorResponse.class
        );

        assertEquals(responseEntity.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY); // 422 for file not found
        assertNotNull(responseEntity.getBody());

        ErrorResponse response = responseEntity.getBody();
        assertEquals(response.getMessage(), Messages.FILE_NOT_FOUND, "Expected: " + Messages.RESULT_IS_BEING_PROCESSED + ", but was: " + response.getMessage());
        assertEquals(responseEntity.getStatusCode(), response.getStatus());
        assertNotNull(response.getTimestamp());
        System.out.println(response.getDebugMessage());
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
                "/api/sad-code/start",
                HttpMethod.POST,
                requestEntity,
                ErrorResponse.class
        );

        assertEquals(responseEntity.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY); // 422 for file not found
        assertNotNull(responseEntity.getBody());

        ErrorResponse response = responseEntity.getBody();
        assertEquals(response.getMessage(), Messages.FILE_NOT_FOUND, "Expected: " + Messages.RESULT_IS_BEING_PROCESSED + ", but was: " + response.getMessage());
        assertEquals(responseEntity.getStatusCode(), response.getStatus());
        assertNotNull(response.getTimestamp());
        System.out.println(response.getDebugMessage());
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
        // Step 1: Start the pipeline and get the id
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("projectName", "bigBlueButton");
        parameters.add("inputText", new org.springframework.core.io.ClassPathResource("bigBlueButton/bigbluebutton.txt"));
        parameters.add("inputCode", new org.springframework.core.io.ClassPathResource("bigBlueButton/codeModel.acm"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        ResponseEntity<ArdocoResultResponse> responseEntity = restTemplate.exchange(
                "/api/sad-code/start",
                HttpMethod.POST,
                requestEntity,
                ArdocoResultResponse.class
        );

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ArdocoResultResponse response = responseEntity.getBody();
        assertNotNull(response.getProjectId());
        String id = response.getProjectId();

        // Step 2: Polling the result (expected to be not ready initially)
        int maxRetries = 1000;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < maxRetries; i++) {
            responseEntity = restTemplate.getForEntity("/api/sad-code/{id}", ArdocoResultResponse.class, id);

            if (responseEntity.getStatusCode() == HttpStatus.ACCEPTED) {
                ArdocoResultResponse resultResponse = responseEntity.getBody();
                assertNotNull(resultResponse);
                assertEquals(id, resultResponse.getProjectId());
                assertEquals(Messages.RESULT_NOT_READY, resultResponse.getMessage());
                assertNull(resultResponse.getSamSadTraceLinks());
            } else if (responseEntity.getStatusCode() == HttpStatus.OK) {
                long estimatedTime = System.currentTimeMillis() - startTime;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(estimatedTime);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(estimatedTime) - TimeUnit.MINUTES.toSeconds(minutes);
                logger.log(Level.INFO, "Time passed to retrieve result: " + minutes + " min " + seconds + " s.");

                assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                ArdocoResultResponse finalResponse = responseEntity.getBody();
                assertNotNull(finalResponse);
                assertNotNull(finalResponse.getSamSadTraceLinks());
                assertEquals(Messages.RESULT_IS_READY, finalResponse.getMessage());
                assertEquals(finalResponse.getStatus(), responseEntity.getStatusCode());

                // Test to retrieve already ready result again:
                responseEntity = restTemplate.getForEntity("/api/sad-code/{id}", ArdocoResultResponse.class, id);
                assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                finalResponse = responseEntity.getBody();
                assertNotNull(finalResponse);
                assertNotNull(finalResponse.getSamSadTraceLinks());
                assertEquals(Messages.RESULT_IS_READY, finalResponse.getMessage());
                assertEquals(finalResponse.getStatus(), responseEntity.getStatusCode());
                return;
            }

            TimeUnit.SECONDS.sleep(1);  // Wait for 1 second before retrying
        }
        fail("The result was not ready after " + maxRetries + " retries.");
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
    void shouldReturnResultWhenReady() throws InterruptedException {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("projectName", "bigBlueButton");
        parameters.add("inputText", new ClassPathResource("bigBlueButton/bigbluebutton.txt"));
        parameters.add("inputCode", new ClassPathResource("bigBlueButton/codeModel.acm"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        ResponseEntity<ArdocoResultResponse> startResponse = restTemplate.exchange(
                "/api/sad-code/start",
                HttpMethod.POST,
                requestEntity,
                ArdocoResultResponse.class
        );

        assertNotNull(startResponse.getBody());
        assertEquals(HttpStatus.OK, startResponse.getStatusCode());

        String id = startResponse.getBody().getProjectId();

        // Polling the result and waiting until it's ready
        ResponseEntity<ArdocoResultResponse> resultResponseEntity;
        int maxRetries = 1000;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < maxRetries; i++) {
            resultResponseEntity = restTemplate.getForEntity("/api/sad-code/wait/{id}", ArdocoResultResponse.class, id);

            if (resultResponseEntity.getStatusCode() == HttpStatus.OK) {
                // Result is ready
                long estimatedTime = System.currentTimeMillis() - startTime;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(estimatedTime);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(estimatedTime) - TimeUnit.MINUTES.toSeconds(minutes);
                logger.log(Level.INFO, "Time passed to retrieve result: " + minutes + " min" + seconds + " s.");

                ArdocoResultResponse resultResponse = resultResponseEntity.getBody();
                assertNotNull(resultResponse);
                assertNotNull(resultResponse.getSamSadTraceLinks());
                assertEquals(Messages.RESULT_IS_READY, resultResponse.getMessage());
                assertEquals(resultResponse.getStatus(), resultResponseEntity.getStatusCode());
                return;
            }
            TimeUnit.SECONDS.sleep(1);  // Wait and retry until result is ready
        }
        fail("The result was not ready after 10 attempts");
    }


    /*
     * ******************************
     *
     * test waitForResult()
     *
     * ******************************
     */

    @Test
    void shouldStartPipelineAndReturnResultWhenReady() {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("projectName", "bigBlueButton");
        parameters.add("inputText", new ClassPathResource("bigBlueButton/bigbluebutton.txt"));
        parameters.add("inputCode", new ClassPathResource("bigBlueButton/codeModel.acm"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        ResponseEntity<ArdocoResultResponse> responseEntity = restTemplate.exchange(
                "/api/sad-code/start-and-wait",
                HttpMethod.POST,
                requestEntity,
                ArdocoResultResponse.class
        );

        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ArdocoResultResponse response = responseEntity.getBody();
        assertNotNull(response.getSamSadTraceLinks());
        assertEquals(Messages.RESULT_IS_READY, response.getMessage());
        assertEquals(response.getStatus(), responseEntity.getStatusCode());
    }
}