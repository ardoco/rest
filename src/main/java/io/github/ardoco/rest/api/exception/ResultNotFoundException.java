package io.github.ardoco.rest.api.exception;

public class ResultNotFoundException extends RuntimeException {
    public ResultNotFoundException(Long id) {
        super("Could not find Result for project with id " + id);
    }
}
