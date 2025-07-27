package edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response;

import com.fasterxml.jackson.annotation.JsonRawValue;

public class ArDoCoApiResult {

    @JsonRawValue
    private String traceLinks;

    @JsonRawValue
    private String inconsistencies;

    public ArDoCoApiResult(String traceLinks, String inconsistencies) {
        this.traceLinks = traceLinks;
        this.inconsistencies = inconsistencies;
    }

    public ArDoCoApiResult(String traceLinks) {
        this.traceLinks = traceLinks;
        this.inconsistencies = "[]";
    }

    public String getTraceLinks() {
        return traceLinks;
    }

    public String getInconsistencies() {
        return inconsistencies;
    }

    public String buildJsonString() {
        return "{\"traceLinks\":" + traceLinks + ", \"inconsistencies\":" + inconsistencies + "}";
    }
}
