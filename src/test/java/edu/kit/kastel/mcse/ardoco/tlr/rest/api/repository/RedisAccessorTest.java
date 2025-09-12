/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        System.out.println(redis.getHost() + ":" + redis.getMappedPort(REDIS_PORT));
    }

    @DynamicPropertySource
    static void configureRedisProperties(DynamicPropertyRegistry registry) {
        // Dynamically set Redis host and port
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(REDIS_PORT));
        System.out.println(redis.getHost() + ":" + redis.getMappedPort(REDIS_PORT));
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

        assertEquals(key, id);
        assertTrue(redisAccessor.keyExistsInDatabase(key));
        assertEquals(value, redisAccessor.getResult(key));
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
        assertEquals(value, redisAccessor.getResult(key));

        String updatedValue = "updatedValue";
        String id2 = redisAccessor.saveResult(key, updatedValue);

        assertEquals(key, id2);
        assertTrue(redisAccessor.keyExistsInDatabase(key));
        assertEquals(updatedValue, redisAccessor.getResult(key));
    }

    @Test
    void testSaveResultNoKey() {
        String key = null;
        String value = "testValue";
        assertThrows(IllegalArgumentException.class, () -> redisAccessor.saveResult(key, value));
    }

}
