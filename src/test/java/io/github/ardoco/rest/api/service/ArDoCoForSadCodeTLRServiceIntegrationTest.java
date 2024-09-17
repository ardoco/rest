package io.github.ardoco.rest.api.service;

import io.github.ardoco.rest.api.api_response.ResultBag;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import io.github.ardoco.rest.api.repository.RedisAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.slf4j.SLF4JLogger;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ArDoCoForSadCodeTLRServiceIntegrationTest {

    @Autowired
    private RedisAccessor redisAccessor;

    @Autowired
    private RunnerTLRService sadCodeTLRService;

//    private static GenericContainer<?> redis;
//
//    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForSadCodeTLRServiceIntegrationTest.class);
//
//    @BeforeAll
//    static void setUpContainer() {
//        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
//        redis = new GenericContainer<>(DockerImageName.parse("redis:7.4.0-alpine")).withExposedPorts(6379);
//        redis.start();
//
//        // Configure Redis properties for Spring
//        System.setProperty("spring.data.redis.host", redis.getHost());
//        System.setProperty("spring.data.redis.port", redis.getFirstMappedPort());
//        redis.followOutput(logConsumer);
//    }
//
//    @AfterAll
//    static void tearDownContainer() {
//        redis.stop();
//    }


    @Container
    @ServiceConnection
    static GenericContainer redis = new GenericContainer<>(DockerImageName.parse("redis:7.4.0-alpine")).withExposedPorts(6379);


    @Test
    void connectionEstablished() {
        assertThat(redis.isCreated()).isTrue();
        assertTrue(redis.isRunning());
    }


    @Test
    void testRunPipelineWithBigBlueButton_Success() throws Exception {
        ClassPathResource inputTextFile = new ClassPathResource("bigBlueButton/bigbluebutton.txt");
        ClassPathResource inputCodeFile = new ClassPathResource("bigBlueButton/codeModel.acm");

        MockMultipartFile inputText = createMockMultipartFile(inputTextFile);
        MockMultipartFile inputCode = createMockMultipartFile(inputCodeFile);

        String projectName = "bigBlueButton";
        SortedMap<String, String> additionalConfigs = new TreeMap<>();

        String pipelineId = sadCodeTLRService.runPipeline(projectName, inputText, inputCode, additionalConfigs);
        pollUntilResultIsAvailable(pipelineId);

        assertTrue(redisAccessor.keyExistsInDatabase(pipelineId));
        String result = redisAccessor.getResult(pipelineId);
        assertNotNull(result);
        System.out.println("Found Tracelinks: " + result);

        redisAccessor.deleteResult(pipelineId);
    }

    @Test
    void testRunPipeline_EmptyFile() throws Exception {
        ClassPathResource inputTextFile = new ClassPathResource("emptyFile.txt");
        ClassPathResource inputCodeFile = new ClassPathResource("bigBlueButton/codeModel.acm");

        MockMultipartFile inputText = createMockMultipartFile(inputTextFile);
        MockMultipartFile inputCode = createMockMultipartFile(inputCodeFile);

        String projectName = "emptyFileProject";
        SortedMap<String, String> additionalConfigs = new TreeMap<>();

        assertThrows(FileNotFoundException.class, () -> {
            sadCodeTLRService.runPipeline(projectName, inputText, inputCode, additionalConfigs);
        });
    }

    @Test
    void testRunPipelineAndWaitForResult_WithBBB_Success() throws Exception {
        ClassPathResource inputTextFile = new ClassPathResource("bigBlueButton/bigbluebutton.txt");
        ClassPathResource inputCodeFile = new ClassPathResource("bigBlueButton/codeModel.acm");

        MockMultipartFile inputText = createMockMultipartFile(inputTextFile);
        MockMultipartFile inputCode = createMockMultipartFile(inputCodeFile);

        String projectName = "bigBlueButtonProjectWait";
        SortedMap<String, String> additionalConfigs = new TreeMap<>();

        ResultBag result = sadCodeTLRService.runPipelineAndWaitForResult(projectName, inputText, inputCode, additionalConfigs);

        assertNotNull(result);
        assertNotNull(result.projectId());
        System.out.println("Found TraceLinks: " + result.traceLinks());

        Optional<String> traceLinks = sadCodeTLRService.getResult(result.projectId());
        assertTrue(traceLinks.isPresent());
        assertEquals(result.traceLinks(), traceLinks.get());

    }

    @Test
    void testRunPipelineAndWaitForResult_EmptyFile() throws Exception {
        ClassPathResource inputTextFile = new ClassPathResource("emptyFile.txt");
        ClassPathResource inputCodeFile = new ClassPathResource("bigBlueButton/codeModel.acm");

        MockMultipartFile inputText = createMockMultipartFile(inputTextFile);
        MockMultipartFile inputCode = createMockMultipartFile(inputCodeFile);

        String projectName = "emptyFileProject";
        SortedMap<String, String> additionalConfigs = new TreeMap<>();

        assertThrows(FileNotFoundException.class, () -> {
            sadCodeTLRService.runPipelineAndWaitForResult(projectName, inputText, inputCode, additionalConfigs);
        });
    }


    @Test
    void testGetResult() throws Exception {
        ClassPathResource inputTextFile = new ClassPathResource("bigBlueButton/bigbluebutton.txt");
        ClassPathResource inputCodeFile = new ClassPathResource("bigBlueButton/codeModel.acm");

        MockMultipartFile inputText = createMockMultipartFile(inputTextFile);
        MockMultipartFile inputCode = createMockMultipartFile(inputCodeFile);
        String projectName = "bigbluebutton";
        SortedMap<String, String> additionalConfigs = new TreeMap<>();

        String pipelineId = sadCodeTLRService.runPipeline(projectName, inputText, inputCode, additionalConfigs);
        Optional<String> result = sadCodeTLRService.getResult(pipelineId);
        assertTrue(result.isEmpty());

        boolean isResultAvailable = pollUntilResultIsAvailable(pipelineId);

        assertTrue(isResultAvailable);
        assertTrue(redisAccessor.keyExistsInDatabase(pipelineId));
        result = sadCodeTLRService.getResult(pipelineId);
        assertFalse(result.isEmpty());
        assertTrue(result.isPresent());
        System.out.println("Found TraceLinks: " + result.get());

        redisAccessor.deleteResult(pipelineId);
    }

    @Test
    void test_invalidKey() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {sadCodeTLRService.getResult("invalidKey");});
        assertThrows(IllegalArgumentException.class, () -> {sadCodeTLRService.waitForResult("invalidKey");});
    }

    @Test
    void testWaitForResult() throws Exception {
        ClassPathResource inputTextFile = new ClassPathResource("bigBlueButton/bigbluebutton.txt");
        ClassPathResource inputCodeFile = new ClassPathResource("bigBlueButton/codeModel.acm");

        MockMultipartFile inputText = createMockMultipartFile(inputTextFile);
        MockMultipartFile inputCode = createMockMultipartFile(inputCodeFile);
        String projectName = "bigBlueButton";
        SortedMap<String, String> additionalConfigs = new TreeMap<>();

        String pipelineId = sadCodeTLRService.runPipeline(projectName, inputText, inputCode, additionalConfigs);
        Optional<String> optionalResult = sadCodeTLRService.getResult(pipelineId);
        assertTrue(optionalResult.isEmpty());
        assertFalse(redisAccessor.keyExistsInDatabase(pipelineId));

        String finalResult = sadCodeTLRService.waitForResult(pipelineId);
        assertNotNull(finalResult);
        System.out.println("Found TraceLinks: " + finalResult);

        assertTrue(redisAccessor.keyExistsInDatabase(pipelineId));
        optionalResult = sadCodeTLRService.getResult(pipelineId);
        assertFalse(optionalResult.isEmpty());

        redisAccessor.deleteResult(pipelineId);
    }

    // Helper to create MockMultipartFile from ClassPathResource
    private MockMultipartFile createMockMultipartFile(ClassPathResource resource) throws IOException {
        File file = resource.getFile();
        String fileName = file.getName();
        String contentType = Files.probeContentType(file.toPath());
        byte[] content = Files.readAllBytes(file.toPath());
        return new MockMultipartFile(fileName, fileName, contentType, content);
    }

    // Helper to poll until Redis contains the result (returns true if the result is available
    private boolean pollUntilResultIsAvailable(String pipelineId) throws InterruptedException {
        int pollIntervalInSeconds = 1;
        int maxPolls = 30;
        for (int i = 0; i < maxPolls; i++) {
            String result = redisAccessor.getResult(pipelineId);
            if (result != null) {
                return true; // Result is available
            }
            TimeUnit.SECONDS.sleep(pollIntervalInSeconds);
        }
        return false;
    }
}

