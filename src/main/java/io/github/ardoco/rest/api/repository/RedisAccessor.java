package io.github.ardoco.rest.api.repository;

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

    private final RedisTemplate<String, String> template;

    public RedisAccessor(RedisTemplate<String, String> template) {
        this.template = template;
    }

    @Override
    public String saveResult(String id, String jsonResult) {
        template.opsForValue().set(id, jsonResult, 24, TimeUnit.HOURS);
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
    public void saveError(String id, String error) {
        template.opsForValue().set(id, error, 24, TimeUnit.HOURS);
    }

    @Override
    public boolean deleteResult(String id) {
        return Boolean.TRUE.equals(template.delete(id));
    }
}
