package io.github.ardoco.rest.api.exception;

import io.github.ardoco.rest.api.util.Messages;

public class TimeoutException extends RuntimeException {
    private final String id;

    public TimeoutException(String id) {
        super(Messages.REQUEST_TIMED_OUT);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}