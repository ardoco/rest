package io.github.ardoco.rest.api.exception;

public class ResultNotFoundException extends RuntimeException {
    public ResultNotFoundException(String id) {
        super("Could not find Result for project with id " + id);
    }
}
