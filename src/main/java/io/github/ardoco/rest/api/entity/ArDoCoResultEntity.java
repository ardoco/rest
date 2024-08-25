//package io.github.ardoco.rest.api.entity;
//
//import org.springframework.data.annotation.Id;
//import org.springframework.data.redis.core.RedisHash;
//
//import java.util.UUID;
//
///**
// * This class models an ArDoCoResult, so that it can be stored into a database
// */
//
//@RedisHash("ArDoCoResultEntity")
//public class ArDoCoResultEntity {
//
//    @Id
//    private String id;
//
//    private String sadCodeTraceLinksJson;
//
//
//    public ArDoCoResultEntity() {
//        this.id = UUID.randomUUID().toString();
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public String getSadCodeTraceLinksJson() {
//        return sadCodeTraceLinksJson;
//    }
//
//    public void setSadCodeTraceLinksJson(String sadCodeTraceLinksJson) {
//        this.sadCodeTraceLinksJson = sadCodeTraceLinksJson;
//    }
//}
