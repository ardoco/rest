package io.github.ardoco.rest.api.exception;

public class TimeoutException extends RuntimeException {
    private final String id;

    public TimeoutException(String id) {
        super("Request timed out. ID: " + id);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}