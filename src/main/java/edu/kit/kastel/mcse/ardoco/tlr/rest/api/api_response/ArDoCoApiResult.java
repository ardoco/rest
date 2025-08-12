/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * Represents the result of an ArDoCo API call, containing trace links and inconsistencies.
 */
public class ArDoCoApiResult {

    @JsonRawValue
    private String traceLinks;

    @JsonRawValue
    private String inconsistencies;

    /**
     * Constructs an ArDoCoApiResult with the specified trace links and inconsistencies.
     *
     * @param traceLinks      JSON string representing the trace links.
     * @param inconsistencies JSON string representing the inconsistencies.
     */
    public ArDoCoApiResult(String traceLinks, String inconsistencies) {
        this.traceLinks = traceLinks;
        this.inconsistencies = inconsistencies;
    }

    /**
     * Constructs an ArDoCoApiResult with the specified trace links and an empty inconsistencies array.
     *
     * @param traceLinks JSON string representing the trace links.
     */
    public ArDoCoApiResult(String traceLinks) {
        this.traceLinks = traceLinks;
        this.inconsistencies = "[]";
    }

    /**
     * Getter for traceLinks.
     */
    public String getTraceLinks() {
        return traceLinks;
    }

    /**
     * Getter for inconsistencies.
     */
    public String getInconsistencies() {
        return inconsistencies;
    }

    /**
     * Builds a JSON string representation of the ArDoCoApiResult.
     *
     * @return A JSON string containing trace links and inconsistencies.
     */
    public String buildJsonString() {
        return "{\"traceLinks\":" + traceLinks + ", \"inconsistencies\":" + inconsistencies + "}";
    }
}
