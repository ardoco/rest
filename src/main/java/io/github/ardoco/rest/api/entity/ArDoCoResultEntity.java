package io.github.ardoco.rest.api.entity;

import jakarta.persistence.*;
import org.springframework.data.redis.core.RedisHash;

/**
 * This class models an ArDoCoResult, so that it can be stored into a database
 */

@RedisHash("ArDoCoResultEntity")
public class ArDoCoResultEntity {

    @Id @GeneratedValue
    private String id;

    private String sadCodeTraceLinksJson;


    public ArDoCoResultEntity() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getSadCodeTraceLinksJson() {
        return sadCodeTraceLinksJson;
    }

    public void setSadCodeTraceLinksJson(String sadCodeTraceLinksJson) {
        this.sadCodeTraceLinksJson = sadCodeTraceLinksJson;
    }
}
