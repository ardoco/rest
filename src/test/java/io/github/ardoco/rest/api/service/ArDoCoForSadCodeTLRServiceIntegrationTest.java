//''package io.github.ardoco.rest.api.service;
//
//import io.github.ardoco.rest.api.api_response.ResultBag;
//import io.github.ardoco.rest.api.exception.FileNotFoundException;
//import io.github.ardoco.rest.api.exception.TimeoutException;
//import io.github.ardoco.rest.api.repository.RedisAccessor;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.slf4j.SLF4JLogger;
//import org.junit.jupiter.api.BeforeEach;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.web.multipart.MultipartFile;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.containers.output.OutputFrame;
//import org.testcontainers.containers.output.Slf4jLogConsumer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//import org.mockito.Mockito;
//import io.github.ardoco.rest.api.api_response.ResultBag;
//import io.github.ardoco.rest.api.exception.ArdocoException;
//import io.github.ardoco.rest.api.exception.TimeoutException;
//import io.github.ardoco.rest.api.repository.DatabaseAccessor;
//import io.github.ardoco.rest.api.util.FileConverter;
//import io.github.ardoco.rest.api.util.Messages;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.nio.file.Files;
//import java.util.Optional;
//import java.util.SortedMap;
//import java.util.TreeMap;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@Testcontainers
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public class ArDoCoForSadCodeTLRServiceIntegrationTest {
//
//    @Autowired
//    private RedisAccessor redisAccessor;
//
//    @Autowired
//    @Qualifier("sadCodeTLRService")
//    private AbstractRunnerTLRService service;
//
////    private static GenericContainer<?> redis;
////
////    private static final Logger logger = LoggerFactory.getLogger(ArDoCoForSadCodeTLRServiceIntegrationTest.class);
////
////    @BeforeAll
////    static void setUpContainer() {
////        Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
////        redis = new GenericContainer<>(DockerImageName.parse("redis:7.4.0-alpine")).withExposedPorts(6379);
////        redis.start();
////
////        // Configure Redis properties for Spring
////        System.setProperty("spring.data.redis.host", redis.getHost());
////        System.setProperty("spring.data.redis.port", redis.getFirstMappedPort());
////        redis.followOutput(logConsumer);
////    }
////
////    @AfterAll
////    static void tearDownContainer() {
////        redis.stop();
////    }
//
//    private DatabaseAccessor databaseAccessor;
//    private SortedMap<String, String> additionalConfigs;
//    ConcurrentHashMap<String, CompletableFuture<String>> asyncTasks;
//
//    @BeforeEach
//    void setup() throws IllegalAccessException, NoSuchFieldException {
//        databaseAccessor = mock(DatabaseAccessor.class);
//        service = new ArDoCoForSadCodeTLRService(databaseAccessor);
//
//        additionalConfigs = new TreeMap<>();
//
//
//        // Set up asyncTasks using reflection to access the private field
//        Field asyncTasksField = ArDoCoForSadCodeTLRService.class.getDeclaredField("asyncTasks");
//        asyncTasksField.setAccessible(true);
//        asyncTasksField.set(service, new ConcurrentHashMap<>());
//        asyncTasks =
//                (ConcurrentHashMap<String, CompletableFuture<String>>) asyncTasksField.get(service);
//    }
//
//
//    @Container
//    @ServiceConnection
//    static GenericContainer redis = new GenericContainer<>(DockerImageName.parse("redis:7.4.0-alpine")).withExposedPorts(6379);
//
//    @Test
//    void connectionEstablished() {
//        assertThat(redis.isCreated()).isTrue();
//        assertTrue(redis.isRunning());
//    }
//
//
//    @Test
//    void testRunPipelineWithBigBlueButton_Success() throws Exception {
////TODO
//    }
//
//    @Test
//    void testRunPipeline_EmptyFile() throws Exception {
//        ClassPathResource inputTextFile = new ClassPathResource("emptyFile.txt");
//        ClassPathResource inputCodeFile = new ClassPathResource("bigBlueButton/codeModel.acm");
//
//        MockMultipartFile inputText = createMockMultipartFile(inputTextFile);
//        MockMultipartFile inputCode = createMockMultipartFile(inputCodeFile);
//
//        String projectName = "emptyFileProject";
//
//        assertThrows(FileNotFoundException.class, () -> {
//            service.runPipeline(projectName, inputText, inputCode, additionalConfigs);
//        });
//    }
//
//    @Test
//    void testRunPipelineAndWaitForResult_WithBBB_Success() throws Exception {
//// TODO
//    }
//
//    @Test
//    void testRunPipelineAndWaitForResult_EmptyFile() throws Exception {
//        ClassPathResource inputTextFile = new ClassPathResource("emptyFile.txt");
//        ClassPathResource inputCodeFile = new ClassPathResource("bigBlueButton/codeModel.acm");
//
//        MockMultipartFile inputText = createMockMultipartFile(inputTextFile);
//        MockMultipartFile inputCode = createMockMultipartFile(inputCodeFile);
//
//        String projectName = "emptyFileProject";
//
//        assertThrows(FileNotFoundException.class, () -> {
//            service.runPipelineAndWaitForResult(projectName, inputText, inputCode, additionalConfigs);
//        });
//    }
//
//
//    @Test
//    void testGetResult() throws Exception {
////TODO
//    }
//
//    @Test
//    void test_invalidKey() throws Exception {
//        assertThrows(IllegalArgumentException.class, () -> {service.getResult("invalidKey");});
//        assertThrows(IllegalArgumentException.class, () -> {service.waitForResult("invalidKey");});
//    }
//
//    @Test
//    void testWaitForResult() throws Exception {
////TODO
//    }
//
//
//
//
//
//
//
//    @Test
//    void testWaitForResult_ResultIsOnItsWay() throws Exception {
//
//        // Mock result is being processed
//        String id = "testInProgress";
//        CompletableFuture<String> future = CompletableFuture.completedFuture("finalResult");
//        asyncTasks.put(id, future);
//
//        // Call the method and assert
//        String actualResult = service.waitForResult(id);
//        assertEquals("finalResult", actualResult);
//
//        // Verify that the database is not accessed
//        verify(databaseAccessor, times(0)).getResult(id);
//    }
//
//    @Test
//    void testWaitForResult_TimeoutException() throws Exception {
//
//        String id = "testTimeout";
//        CompletableFuture<String> future = mock(CompletableFuture.class);
//        asyncTasks.put(id, future);
//
//        // Mock timeout exception
//        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(new java.util.concurrent.TimeoutException());
//
//        // Call the method and expect TimeoutException
//        TimeoutException exception = assertThrows(TimeoutException.class, () -> service.waitForResult(id));
//        assertEquals(id, exception.getId());
//    }
//
//    @Test
//    void testWaitForResult_ResultNotInDatabase() {
//        String id = "missingResult";
//
//        // Mock result not in the database
//        when(databaseAccessor.keyExistsInDatabase(id)).thenReturn(false);
//
//        // Call the method and expect IllegalArgumentException
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.waitForResult(id));
//        assertEquals(Messages.noResultForKey(id), exception.getMessage());
//    }
//
//
//
//
//
//
//
//
//    // Helper to create MockMultipartFile from ClassPathResource
//    private MockMultipartFile createMockMultipartFile(ClassPathResource resource) throws IOException {
//        File file = resource.getFile();
//        String fileName = file.getName();
//        String contentType = Files.probeContentType(file.toPath());
//        byte[] content = Files.readAllBytes(file.toPath());
//        return new MockMultipartFile(fileName, fileName, contentType, content);
//    }
//
//    // Helper to poll until Redis contains the result (returns true if the result is available
//    private boolean pollUntilResultIsAvailable(String pipelineId, int maxPolls) throws InterruptedException {
//        for (int i = 0; i < maxPolls; i++) {
//            String result = redisAccessor.getResult(pipelineId);
//            if (result != null) {
//                return true;
//            }
//            TimeUnit.SECONDS.sleep(1);
//        }
//        return false;
//    }
//}
//
