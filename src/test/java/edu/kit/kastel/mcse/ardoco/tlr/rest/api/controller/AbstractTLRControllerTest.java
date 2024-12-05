/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import edu.kit.kastel.mcse.ardoco.tlr.rest.ArDoCoRestApplication;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ErrorResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository.RedisAccessor;
import testUtil.TestUtils;

@Testcontainers
@SpringBootTest(classes = ArDoCoRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractTLRControllerTest {

    private static final String GET_RESULT_ENDPOINT = "/api/get-result/{id}";
    private static final String WAIT_FOR_RESULT_ENDPOINT = "/api/wait-for-result/{id}";

    private final String runPipelineEndpoint;
    private final String runPipelineAndWaitEndpoint;
    private final TraceLinkType traceLinkType;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected RedisAccessor redisAccessor;

    private static GenericContainer<?> redis;
    private static final String REDIS_IMAGE_NAME = "redis:7.0-alpine";
    private static final int REDIS_PORT = 6379;

    @BeforeAll
    static void beforeAll() {
        redis = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE_NAME)).withExposedPorts(REDIS_PORT);
        redis.start();
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getMappedPort(REDIS_PORT).toString());
        System.out.println(redis.getHost() + ":" + redis.getMappedPort(REDIS_PORT));
    }

    // Redis container shared across all subclasses
    /*@Container
    @ServiceConnection
    public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }*/

    public AbstractTLRControllerTest(TraceLinkType traceLinkType) {
        this.traceLinkType = traceLinkType;
        String endpointName = traceLinkType.getEndpointName();
        this.runPipelineEndpoint = String.format("/api/%s/start", endpointName);
        this.runPipelineAndWaitEndpoint = String.format("/api/%s/start-and-wait", endpointName);
    }

    // Common test method for starting pipeline and getting results
    @Timeout(value = 6, unit = TimeUnit.MINUTES)
    protected void runPipeline_start_and_getResult(HttpEntity<MultiValueMap<String, Object>> requestEntity) throws IOException {
        // Start the pipeline
        ArdocoResultResponse response = startNewPipeline_test(requestEntity);
        String projectId = response.getRequestId();

        tryGetResultWhenNotReady_test(projectId);
        waitForResultUntilReady_test(projectId);

        // Now the result should be ready
        getResult_hasResult_test(projectId);
        runPipeLineDirectlyHasResult_test(requestEntity);
        runPipeLineAndWaitDirectlyHasResult_test(requestEntity);

        // Clean up
        redisAccessor.deleteResult(response.getRequestId());
    }

    @Test
    void testRetrievingResultForInvalidId() {
        String invalidId = "invalid-project-id";

        // testGetResult
        ResponseEntity<ErrorResponse> responseEntity = restTemplate.getForEntity(GET_RESULT_ENDPOINT, ErrorResponse.class, invalidId);
        TestUtils.testInvalidRequestId(responseEntity, invalidId);

        // testWaitForResult
        responseEntity = restTemplate.getForEntity(WAIT_FOR_RESULT_ENDPOINT, ErrorResponse.class, invalidId);
        TestUtils.testInvalidRequestId(responseEntity, invalidId);
    }

    @Test
    void testHandleEmptyFile() {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = prepareRequestEntityForEmptyFileTest("emptyFileProject");

        ResponseEntity<ErrorResponse> responseEntity = restTemplate.exchange(runPipelineEndpoint, HttpMethod.POST, requestEntity, ErrorResponse.class);
        TestUtils.testsForHandelingEmptyFiles(responseEntity);

        responseEntity = restTemplate.exchange(runPipelineAndWaitEndpoint, HttpMethod.POST, requestEntity, ErrorResponse.class);
        TestUtils.testsForHandelingEmptyFiles(responseEntity);
    }

    protected abstract HttpEntity<MultiValueMap<String, Object>> prepareRequestEntityForEmptyFileTest(String projectName);

    protected void test_runPipelineAndWaitForResult_helper(HttpEntity<MultiValueMap<String, Object>> requestEntity) throws IOException {
        // test whether runPipeLineAndWait() directly has the result
        ResponseEntity<String> responseEntity = restTemplate.exchange(runPipelineAndWaitEndpoint, HttpMethod.POST, requestEntity, String.class);
        assertNotNull(responseEntity.getBody());
        ArdocoResultResponse response = TestUtils.parseResponseEntityToArdocoResponse(responseEntity);

        String projectId = response.getRequestId();

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            // result is ready
            TestUtils.testWaitForResult_ready(response, responseEntity, traceLinkType);
            resultIsInDatabase(projectId);
        } else if (responseEntity.getStatusCode() == HttpStatus.ACCEPTED) {
            // continue waiting
            TestUtils.testRunPipelineAndWaitForResult_notReady(response, responseEntity, traceLinkType);
            waitForResultUntilReady_test(projectId);
        } else {
            fail();
        }

        // now the result should be ready: test getResult() when result is ready
        getResult_hasResult_test(projectId);
        runPipeLineDirectlyHasResult_test(requestEntity);
        runPipeLineAndWaitDirectlyHasResult_test(requestEntity);

        // clean up after test
        redisAccessor.deleteResult(projectId);
    }

    // Helper methods for common test logic
    protected ArdocoResultResponse startNewPipeline_test(HttpEntity<MultiValueMap<String, Object>> requestEntity) throws IOException {
        ResponseEntity<String> responseEntity = restTemplate.exchange(runPipelineEndpoint, HttpMethod.POST, requestEntity, String.class);
        assertNotNull(responseEntity.getBody());
        return TestUtils.parseResponseEntityToArdocoResponse(responseEntity);
    }

    protected void tryGetResultWhenNotReady_test(String projectId) throws IOException {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(GET_RESULT_ENDPOINT, String.class, projectId);
        ArdocoResultResponse response = TestUtils.parseResponseEntityToArdocoResponse(responseEntity);
        TestUtils.testGetResult_notReady(response, responseEntity, traceLinkType);
        resultIsNotInDatabase(response.getRequestId());
    }

    protected void waitForResultUntilReady_test(String projectId) throws IOException {
        ResponseEntity<String> waitingEntity;
        do {
            waitingEntity = restTemplate.getForEntity(WAIT_FOR_RESULT_ENDPOINT, String.class, projectId);
            ArdocoResultResponse waitingResponse = TestUtils.parseResponseEntityToArdocoResponse(waitingEntity);

            if (waitingEntity.getStatusCode() == HttpStatus.ACCEPTED) {
                resultIsNotInDatabase(projectId);
                TestUtils.testWaitForResult_notReady(waitingResponse, waitingEntity, traceLinkType);
            } else if (waitingEntity.getStatusCode() == HttpStatus.OK) {
                TestUtils.testWaitForResult_ready(waitingResponse, waitingEntity, traceLinkType);
                resultIsInDatabase(projectId);
            } else {
                fail();
            }

        } while (waitingEntity.getStatusCode() == HttpStatus.ACCEPTED);
    }

    protected void getResult_hasResult_test(String projectId) throws IOException {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(GET_RESULT_ENDPOINT, String.class, projectId);
        ArdocoResultResponse response = TestUtils.parseResponseEntityToArdocoResponse(responseEntity);
        TestUtils.testGetResult_ready(response, responseEntity, traceLinkType);
        resultIsInDatabase(response.getRequestId());
    }

    protected void runPipeLineDirectlyHasResult_test(HttpEntity<MultiValueMap<String, Object>> requestEntity) throws IOException {
        ResponseEntity<String> responseEntity = restTemplate.exchange(runPipelineEndpoint, HttpMethod.POST, requestEntity, String.class);
        assertNotNull(responseEntity.getBody());
        ArdocoResultResponse response = TestUtils.parseResponseEntityToArdocoResponse(responseEntity);
        TestUtils.testStartPipeline_resultIsInDatabase(response, responseEntity, traceLinkType);
        resultIsInDatabase(response.getRequestId());
    }

    protected void runPipeLineAndWaitDirectlyHasResult_test(HttpEntity<MultiValueMap<String, Object>> requestEntity) throws IOException {
        ResponseEntity<String> responseEntity = restTemplate.exchange(runPipelineAndWaitEndpoint, HttpMethod.POST, requestEntity, String.class);
        assertNotNull(responseEntity.getBody());
        ArdocoResultResponse response = TestUtils.parseResponseEntityToArdocoResponse(responseEntity);
        TestUtils.testStartPipeline_resultIsInDatabase(response, responseEntity, traceLinkType);
        resultIsInDatabase(response.getRequestId());
    }

    // Utility methods for checking database state
    protected void resultIsNotInDatabase(String requestID) {
        assertFalse(redisAccessor.keyExistsInDatabase(requestID));
    }

    protected void resultIsInDatabase(String requestID) {
        assertTrue(redisAccessor.keyExistsInDatabase(requestID));
    }

}
