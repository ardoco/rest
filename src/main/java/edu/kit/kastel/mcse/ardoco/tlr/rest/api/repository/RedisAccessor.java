package edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * Class which is responsible for accessing the Redis Database
 */
@Primary
@Repository
public class RedisAccessor implements DatabaseAccessor {

    @Value("${redis.time-to-live.hours}")
    private long timeToLive;

    private final RedisTemplate<String, String> template;

    public RedisAccessor(RedisTemplate<String, String> template) {
        this.template = template;
    }

    @Override
    public String saveResult(String id, String jsonResult) {
        template.opsForValue().set(id, jsonResult, timeToLive, TimeUnit.HOURS);
        return id;
    }

    @Override
    public String getResult(String id) {
        return template.opsForValue().get(id);
    }

    @Override
    public boolean keyExistsInDatabase(String key) {
        return Boolean.TRUE.equals(template.hasKey(key));
    }

    @Override
    public boolean deleteResult(String id) {
        return Boolean.TRUE.equals(template.delete(id));
    }
}
