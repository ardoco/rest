package io.github.ardoco.rest.api.exception;

public class ArdocoException extends RuntimeException {
    public ArdocoException(String message) {
        super(message);
    }

    public ArdocoException(String message, Throwable cause) {
        super(message, cause);
    }
}
