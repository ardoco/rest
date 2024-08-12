package io.github.ardoco.rest.api.exception;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException() {
        super("Could not find file");
    }
}
