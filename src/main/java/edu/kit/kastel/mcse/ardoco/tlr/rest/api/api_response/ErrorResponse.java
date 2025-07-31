package edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;


/**
 * Represents an error response structure for the ArDoCo API.
 */
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss z")
    private final ZonedDateTime timestamp;

    private HttpStatus status;
    private String message;


    /**
     * Default constructor for ErrorResponse.
     * Initializes the timestamp to the current time in the Europe/Berlin timezone.
     */
    public ErrorResponse() {
        timestamp = LocalDateTime.now().atZone(ZoneId.of("Europe/Berlin"));
    }

    /**
     * Constructor for ErrorResponse with a specific HTTP status.
     *
     * @param status the HTTP status of the error response
     */
    public ErrorResponse(HttpStatus status) {
        this();
        this.status = status;
        this.message = "Unexpected error";
    }

    /**
     * Constructor for ErrorResponse with a specific HTTP status and message.
     *
     * @param status  the HTTP status of the error response
     * @param message a message providing additional information about the error
     */
    public ErrorResponse(HttpStatus status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    /**
     * Getter for the timestamp of the error response.
     *
     * @return the timestamp when the error occurred"
     */
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for the HTTP status of the error response.
     *
     * @return the HTTP status of the error response
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Setter for the HTTP status of the error response.
     *
     * @param status the HTTP status to set for the error response
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    /**
     * Getter for the message of the error response.
     *
     * @return the message providing additional information about the error
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for the message of the error response.
     *
     * @param message the message to set for the error response
     */
    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ErrorResponse that)) return false;
        return Objects.equals(timestamp, that.timestamp) &&
                status == that.status &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, status, message);
    }
}