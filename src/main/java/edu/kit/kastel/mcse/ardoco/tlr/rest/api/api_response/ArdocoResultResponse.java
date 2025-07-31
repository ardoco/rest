package edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.http.HttpStatus;

/**
 * Represents the response structure for the ArDoCo API results.
 */
public class ArdocoResultResponse {
    private String requestId;
    private HttpStatus status;
    private String message;
    private TraceLinkType traceLinkType;

    @JsonRawValue
    private String result;

    /**
     * Default constructor for ArdocoResultResponse.
     * This is required for deserialization purposes.
     */
    public ArdocoResultResponse() {}

    /**
     * Constructor for ArdocoResultResponse with requestId, status, and message.
     *
     * @param requestId the unique identifier for the request
     * @param status the HTTP status of the response
     * @param message a message providing additional information about the response
     */
    public ArdocoResultResponse(String requestId, HttpStatus status, String message) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
        this.traceLinkType = TraceLinkType.fromId(requestId);
    }

    /**
     * Constructor for ArdocoResultResponse with requestId, status, result, and message.
     *
     * @param requestId the unique identifier for the request
     * @param status the HTTP status of the response
     * @param result the result of the ArDoCo processing, in JSON format
     * @param message a message providing additional information about the response
     */
    public ArdocoResultResponse(String requestId, HttpStatus status, String result, String message) {
        this.requestId = requestId;
        this.status = status;
        this.result = result;
        this.message = message;
        this.traceLinkType = TraceLinkType.fromId(requestId);
    }

    /**
     * Getter for requestId.
     *
     * @return the unique identifier for the request
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Setter for requestId.
     *
     * @param requestId the unique identifier for the request
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Getter for status.
     *
     * @return the HTTP status of the response
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Setter for status.
     *
     * @param status the HTTP status of the response
     */
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    /**
     * Getter for message.
     *
     * @return the message providing additional information about the response
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for message.
     *
     * @param message the message providing additional information about the response
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter for result.
     *
     * @return the result of the ArDoCo processing, in JSON format
     */
    public String getResult() {
        return result;
    }

    /**
     * Setter for result.
     *
     * @param result the result of the ArDoCo processing, in JSON format
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * Getter for traceLinkType.
     *
     * @return the type of trace link associated with the request
     */
    public TraceLinkType getTraceLinkType() {
        return traceLinkType;
    }

    /**
     * Setter for traceLinkType.
     *
     * @param traceLinkType the type of trace link associated with the request
     */
    public void setTraceLinkType(TraceLinkType traceLinkType) {
        this.traceLinkType = traceLinkType;
    }
}
