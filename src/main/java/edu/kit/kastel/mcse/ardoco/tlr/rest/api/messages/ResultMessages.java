package edu.kit.kastel.mcse.ardoco.tlr.rest.api.messages;

public final class ResultMessages {

    private ResultMessages() {} // prevent instantiation

    public static final String RESULT_IS_READY = "The result is ready.";
    public static final String RESULT_IS_BEING_PROCESSED = "The result is being processed and can be queried using the id.";
    public static final String RESULT_NOT_READY = "Result is still being processed. Please try again later.";

    public static final String REQUEST_TIMED_OUT = "The request timed out before the traceLinks could be retrieved. Please try again using the projectId.";
    public static final String REQUEST_TIMED_OUT_START_AND_WAIT = "The request timed out before the traceLinks could be retrieved. Please try querying only the result using the projectId.";
}
