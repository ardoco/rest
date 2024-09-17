package io.github.ardoco.rest.api.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

 @SpringBootTest
public class RedisAccessorTest {
    private static final String REDIS_IMAGE_NAME = "redis:7.0-alpine";
    private static final int REDIS_PORT = 6379;

    @Autowired
    private RedisAccessor redisAccessor;

    private static GenericContainer<?> redis;

    @BeforeAll
    static void beforeAll() {
        redis = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE_NAME)).withExposedPorts(REDIS_PORT);
        redis.start();
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getMappedPort(REDIS_PORT).toString());
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
    }

    @Test
    void testSaveKeyValueAndGetValue() {
        String key = "testKey";
        String value = "testValue";
        String id = redisAccessor.saveResult(key, value);

        assertEquals(id, key);
        assertTrue(redisAccessor.keyExistsInDatabase(key));
        assertEquals(redisAccessor.getResult(key), value);
    }

    @Test
    void testNonExistentKey() {
        String nonExistentKey = "nonExistentKey";

        assertNull(redisAccessor.getResult(nonExistentKey));
        assertFalse(redisAccessor.keyExistsInDatabase(nonExistentKey));
    }

    @Test
    void testSaveResultMultipleValues() {
        String key = "testKey";
        String value = "testValue";
        String id = redisAccessor.saveResult(key, value);
        assertEquals(redisAccessor.getResult(key), value);

        String updatedValue = "updatedValue";
        String id2 = redisAccessor.saveResult(key, updatedValue);

        assertEquals(id2, key);
        assertTrue(redisAccessor.keyExistsInDatabase(key));
        assertEquals(redisAccessor.getResult(key), updatedValue);
    }

    @Test
    void testSaveResultNoKey() {
        String key = null;
        String value = "testValue";
        assertThrows(IllegalArgumentException.class, () -> redisAccessor.saveResult(key, value));
    }

}
