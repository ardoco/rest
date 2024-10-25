package io.github.ardoco.rest.api.exception;

public class TimeoutException extends RuntimeException {

    public TimeoutException(String id, Throwable cause) {
        super("The request with id " + id + " timed out before the traceLinks could be retrieved. Please try again using the projectId", cause);
    }

    public TimeoutException(String id) {
        super("The request with id " + id + " timed out before the traceLinks could be retrieved. Please try again using the projectId");
    }
}