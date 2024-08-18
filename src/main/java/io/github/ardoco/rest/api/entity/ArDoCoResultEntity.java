package io.github.ardoco.rest.api.entity;

import jakarta.persistence.*;

/**
 * This class models an ArDoCoResult, so that it can be stored into a database
 */
@Entity
@Table(name = "ARDOCO_RESULT")
public class ArDoCoResultEntity {

    @Id @GeneratedValue
    private Long id;

    private String sadCodeTraceLinksJson;


    public ArDoCoResultEntity() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getSadCodeTraceLinksJson() {
        return sadCodeTraceLinksJson;
    }

    public void setSadCodeTraceLinksJson(String sadCodeTraceLinksJson) {
        this.sadCodeTraceLinksJson = sadCodeTraceLinksJson;
    }
}
