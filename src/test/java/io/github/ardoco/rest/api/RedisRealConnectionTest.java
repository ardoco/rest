package io.github.ardoco.rest.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")  // Ensure test profile is used to avoid side effects
public class RedisRealConnectionTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /*@Test
    public void testRedisConnection() {
        // Attempt to connect and perform a basic operation
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
    }*/

//    @Test
//    public void testRedisConnectionFailure() {
//        // Use invalid configuration or catch connection failures to test the error handling
//        // For example, check if wrong credentials throw a RedisConnectionFailureException
//
//        assertThatThrownBy(() -> {
//            redisTemplate.opsForValue().get("invalid_key");
//        }).hasRootCauseInstanceOf(org.springframework.data.redis.RedisConnectionFailureException.class);
//    }
}
