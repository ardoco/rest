package io.github.ardoco.rest.api.api_response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.http.HttpStatus;


public class ArdocoResultResponse {
    private String projectId;
    private HttpStatus status;
    private String message;

    @JsonRawValue
    private String samSadTraceLinks;

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

    public ArdocoResultResponse(HttpStatus status, String samSadTraceLinks) {
        this.status = status;
        this.samSadTraceLinks = samSadTraceLinks;
    }

    public ArdocoResultResponse(String projectId, HttpStatus status, String samSadTraceLinks, String message) {
        this.projectId = projectId;
        this.status = status;
        this.samSadTraceLinks = samSadTraceLinks;
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

    public String getSamSadTraceLinks() {
        return samSadTraceLinks;
    }

    public void setSamSadTraceLinks(String samSadTraceLinks) {
        this.samSadTraceLinks = samSadTraceLinks;
    }
}
