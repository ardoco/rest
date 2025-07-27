package edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.http.HttpStatus;


public class ArdocoResultResponse {
    private String requestId;
    private HttpStatus status;
    private String message;
    private TraceLinkType traceLinkType;

    @JsonRawValue
    private String result;

    public ArdocoResultResponse() {}

    public ArdocoResultResponse(String requestId, HttpStatus status, String message) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
        this.traceLinkType = TraceLinkType.fromId(requestId);
    }

    public ArdocoResultResponse(String requestId, HttpStatus status, String result, String message) {
        this.requestId = requestId;
        this.status = status;
        this.result = result;
        this.message = message;
        this.traceLinkType = TraceLinkType.fromId(requestId);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public TraceLinkType getTraceLinkType() {
        return traceLinkType;
    }

    public void setTraceLinkType(TraceLinkType traceLinkType) {
        this.traceLinkType = traceLinkType;
    }
}
