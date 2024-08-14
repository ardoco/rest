package io.github.ardoco.rest.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.List;

/**
 * This class models an ArDoCoResult, so that it can be stored into a database
 */
@Entity
public class ArDoCoResultEntity {
    @Id @GeneratedValue
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
