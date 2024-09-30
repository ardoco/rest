package io.github.ardoco.rest.api.api_response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.http.HttpStatus;


public class ArdocoResultResponse {
    private String projectId;
    private HttpStatus status;
    private String message;

    @JsonRawValue
    //@JsonDeserialize(using = ArdocoResultResponseDeserializer.class)
    private String traceLinks;

    public ArdocoResultResponse() {}

    public ArdocoResultResponse(String projectId, HttpStatus status) {
        this.projectId = projectId;
        this.status = status;
    }

    public ArdocoResultResponse(String projectId, HttpStatus status, String message) {
        this.projectId = projectId;
        this.status = status;
        this.message = message;
    }

    public ArdocoResultResponse(HttpStatus status, String traceLinks) {
        this.status = status;
        this.traceLinks = traceLinks;
    }

    public ArdocoResultResponse(String projectId, HttpStatus status, String traceLinks, String message) {
        this.projectId = projectId;
        this.status = status;
        this.traceLinks = traceLinks;
        this.message = message;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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
}
