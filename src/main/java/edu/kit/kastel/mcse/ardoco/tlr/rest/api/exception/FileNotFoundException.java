package edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception;

/**
 * Exception thrown when there is an error during file conversion.
 * This exception extends RuntimeException, allowing it to be thrown without being declared in method signatures.
 */
public class FileNotFoundException extends RuntimeException {

    /**
     * Default constructor for FileNotFoundException.
     * This constructor allows for the creation of an exception without a specific message or cause.
     */
    public FileNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor for FileNotFoundException with a specific message and cause.
     * This constructor allows for the creation of an exception with a detailed message and an underlying cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of the exception, which can be another throwable
     */
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
