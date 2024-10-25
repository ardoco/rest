package io.github.ardoco.rest.api.api_response;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;



public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss z")
    private final ZonedDateTime timestamp;

    private HttpStatus status;
    private String message;


    public ErrorResponse() {
        timestamp = LocalDateTime.now().atZone(ZoneId.of("Europe/Berlin"));
    }

    public ErrorResponse(HttpStatus status) {
        this();
        this.status = status;
        this.message = "Unexpected error";
    }

    public ErrorResponse(HttpStatus status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }


    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

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