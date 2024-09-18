package io.github.ardoco.rest.api.util;

public final class Messages {

    private Messages() {} // prevent instantiation

    /*
    Messages for ArDoCoResultResponse
     */
    public static final String RESULT_IS_READY = "The result is ready.";

    public static final String RESULT_IS_BEING_PROCESSED = "The result is being processed and can be queried using the id.";

    public static final String RESULT_NOT_READY = "Result is still being processed. Please try again later.";


    /*
    Messages for Errors
     */
    public static final String FILE_NOT_FOUND = "File not found.";

    public static final String FILE_NOT_CONVERTABLE = "File is not convertable.";

    public static final String INVALID_ARGUMENT = "The provided argument is invalid.";

    public static final String REQUEST_TIMED_OUT = "The request timed out before the traceLinks could be retrieved. Please try again using the projectId.";

    public static final String REQUEST_TIMED_OUT_START_AND_WAIT = "The request timed out before the traceLinks could be retrieved. Please try querying only the result using the projectId.";

    public static String noResultForKey(String id) {
        return "No result with key " + id + " found.";
    }
}
