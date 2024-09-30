package io.github.ardoco.rest.api.api_response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.http.HttpStatus;


public class ArdocoResultResponse {
    private String requestId;
    private HttpStatus status;
    private String message;
    private TraceLinkType traceLinkType;

    @JsonRawValue
    //@JsonDeserialize(using = ArdocoResultResponseDeserializer.class)
    private String traceLinks;

    public ArdocoResultResponse() {}

    public ArdocoResultResponse(String requestId, HttpStatus status) {
        this.requestId = requestId;
        this.status = status;
    }

    public ArdocoResultResponse(String requestId, HttpStatus status, String message) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
    }

    public ArdocoResultResponse(HttpStatus status, String traceLinks, TraceLinkType traceLinkType) {
        this.status = status;
        this.traceLinks = traceLinks;
        this.traceLinkType = traceLinkType;
    }

    public ArdocoResultResponse(String requestId, HttpStatus status, String traceLinks, String message, TraceLinkType traceLinkType) {
        this.requestId = requestId;
        this.status = status;
        this.traceLinks = traceLinks;
        this.message = message;
        this.traceLinkType = traceLinkType;
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

    public String getTraceLinks() {
        return traceLinks;
    }

    public void setTraceLinks(String traceLinks) {
        this.traceLinks = traceLinks;
    }

    public TraceLinkType getTraceLinkType() {
        return traceLinkType;
    }

    public void setTraceLinkType(TraceLinkType traceLinkType) {
        this.traceLinkType = traceLinkType;
    }
}
