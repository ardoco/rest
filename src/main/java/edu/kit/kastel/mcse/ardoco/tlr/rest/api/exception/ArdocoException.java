package edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception;

/**
 * Custom exception class for handling errors in the ArDoCo API.
 */
public class ArdocoException extends RuntimeException {

    /**
     * Constructs a new ArdocoException with the specified message.
     *
     * @param message the detail message
     */
    public ArdocoException(String message) {
        super(message);
    }

    /**
     * Constructs a new ArdocoException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public ArdocoException(String message, Throwable cause) {
        super(message, cause);
    }
}
