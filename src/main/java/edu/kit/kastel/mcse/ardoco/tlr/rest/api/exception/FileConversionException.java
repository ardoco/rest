package edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception;

/**
 * Exception thrown when there is an error during file conversion.
 */
public class FileConversionException extends RuntimeException {

    /**
     * Constructs a new FileConversionException with the specified message.
     *
     * @param message the detail message
     */
    public FileConversionException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileConversionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public FileConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
