/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * Application configuration class for setting up Redis connection
 */
@Configuration
@EnableAutoConfiguration
@EnableRedisRepositories
public class ApplicationConfig {

    @Value("${spring.data.redis.host}")
    private String hostName;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * Default constructor for ApplicationConfig.
     */
    public ApplicationConfig() {
    }

    /**
     * Creates a LettuceConnectionFactory bean for Redis.
     *
     * @return a LettuceConnectionFactory configured with the provided host and port.
     */
    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        // ! You have to provide the redisStandaloneConfiguration or else the app wont
        // ! work with docker
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(hostName);
        redisStandaloneConfiguration.setPort(port);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    /**
     * Creates a RedisTemplate bean for Redis operations.
     *
     * @param redisConnectionFactory the RedisConnectionFactory to use for creating the template.
     * @return a RedisTemplate configured with the provided connection factory.
     */
    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
