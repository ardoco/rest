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


/**
 * Global exception handler that captures and processes various exceptions throughout the application,
 * and returns HTTP responses for each exception type.
 * <p>
 * This handler provides specific methods for exceptions commonly encountered in the application, and it also includes
 * a general fallback handler for any other exceptions. The handler uses the {@link ErrorResponse} class to format error
 * details in the response body.
 */

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(FileNotFoundException.class)
    @ApiResponse(responseCode = "422", description = "When the provided file is empty or doesn't exist", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleFileNotFoundException(FileNotFoundException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(FileConversionException.class)
    @ApiResponse(responseCode = "422", description = "When the provided file cannot be converted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleFileConversionException(FileConversionException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(ArdocoException.class)
    @ApiResponse(responseCode = "500", description = "When querying ardoco resulted in an error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleArdocoException(ArdocoException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ApiResponse(responseCode = "422", description = "One of the Provided Argument is invalid.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(TimeoutException.class)
    @ApiResponse(responseCode = "408", description = "The request timed out before the result could be retrieved.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ErrorResponse> handleTimeoutException(TimeoutException ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.REQUEST_TIMEOUT, ex.getMessage());
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error(ex);
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(error, error.getStatus());
    }

}
