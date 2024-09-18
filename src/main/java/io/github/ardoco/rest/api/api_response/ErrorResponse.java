package io.github.ardoco.rest.api.api_response;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Objects;



public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private final LocalDateTime timestamp;

    private HttpStatus status;
    private String message;


    public ErrorResponse() {
        timestamp = LocalDateTime.now();
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

    public LocalDateTime getTimestamp() {
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
        if (o == null || getClass() != o.getClass()) return false;
        ErrorResponse that = (ErrorResponse) o;
        return Objects.equals(timestamp, that.timestamp) &&
                status == that.status &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, status, message);
    }
}