package io.github.ardoco.rest.api.exception;

public class HashingException extends RuntimeException {
        public HashingException() {
            super("Error occurred while generating an id for the project");
        }
}
