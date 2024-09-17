package io.github.ardoco.rest.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testRedisConnection() {
        String key = "test:connection";
        String value = "connected";

        // Set a value in Redis
        redisTemplate.opsForValue().set(key, value);

        // Get the value from Redis
        String retrievedValue = redisTemplate.opsForValue().get(key);

        // Assert that the retrieved value matches the set value
        assertThat(retrievedValue).isEqualTo(value);

        // Clean up
        redisTemplate.delete(key);
    }
}
