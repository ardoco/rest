package io.github.ardoco.rest.api.exception;

import io.github.ardoco.rest.api.api_response.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import java.util.concurrent.ExecutionException;


@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FileNotFoundException.class)
    @ApiResponse(responseCode = "422", description = "When the provided file is empty or doesn't exist", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleFileNotFoundException(FileNotFoundException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "File not found", ex);
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(FileConversionException.class)
    @ApiResponse(responseCode = "422", description = "When the provided file cannot be converted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleFileConversionException(FileConversionException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "File not convertable", ex);
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(HashingException.class)
    @ApiResponse(responseCode = "500", description = "When an error occurred while generating the project_ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleHashingException(HashingException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex);
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(ArdocoException.class)
    @ApiResponse(responseCode = "500", description = "When querying ardoco resulted in an error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleArdocoException(ArdocoException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex);
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<ErrorResponse> handleInterruptedException(ExecutionException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error during async querying ardocoTLR", ex);
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ApiResponse(responseCode = "422", description = "One of the Provided Argument is invalid.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(FileNotFoundException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "The provided argument is invalid.", ex);
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex);
        return new ResponseEntity<>(error, error.getStatus());
    }

    //    @ExceptionHandler(TimeoutException.class)
//    public ResponseEntity<String> handleTimeoutException(TimeoutException ex) {
//        // Handle custom timeout exception
//        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out. ID: " + ex.getId());
//    }
}
