/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import java.util.Optional;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.ArdocoException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.TimeoutException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.messages.ResultMessages;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Result Retrieval")
@RestController
@RequestMapping("/api")
public class ResultController {

    @Autowired
    private ResultService service;

    @Operation(summary = "Queries the TraceLinks for a given resultID, and returns it if it is ready", description = "Queries whether the TraceLinks are ready using the id, which was returned by tue runPipeline method. " + "In case the result is not yet ready, the user gets informed about it via an appropriate message and the user retrieves the unique id to query the result later")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "the sadCodeTraceLinks found by ardoco", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),
            @ApiResponse(responseCode = "202", description = "the sadCodeTraceLinks are not ready yet", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))), })
    @GetMapping("/get-result/{id}")
    public ResponseEntity<ArdocoResultResponse> getResult(
            @Parameter(description = "The ID of the result to query", required = true) @PathVariable("id") String id) throws ArdocoException,
            IllegalArgumentException {
        return handleGetResult(id);
    }

    @Operation(summary = "Waits up to 60s for the TraceLinks and returns them when they are ready.", description = "Queries the TraceLinks and returns them when the previously started pipeline (using the runPipeline Method) has finished." + "In case the result is not there within 60s of waiting, the user gets informed about it via an appropriate message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "the sadCodeTraceLinks found by ardoco", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),
            @ApiResponse(responseCode = "202", description = "the sadCodeTraceLinks are not ready yet, the waiting timed out", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),

    })
    @GetMapping("/wait-for-result/{id}")
    public ResponseEntity<ArdocoResultResponse> waitForResult(
            @Parameter(description = "The ID of the result to query", required = true) @PathVariable("id") String id) throws ArdocoException,
            IllegalArgumentException, TimeoutException {
        return handleWaitForResult(id);
    }

    /**
     * Handles the retrieval of a result and builds an appropriate response based on the result status.
     *
     * @param requestId the unique request ID for retrieving the result
     * @return a {@link ResponseEntity} containing the {@link ArdocoResultResponse} with the status and result message
     * @throws ArdocoException          if an error occurs while fetching the result
     * @throws IllegalArgumentException if the provided requestId is invalid
     */
    protected ResponseEntity<ArdocoResultResponse> handleGetResult(String requestId) throws ArdocoException, IllegalArgumentException {
        Optional<String> result = service.getResult(requestId);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(requestId, HttpStatus.ACCEPTED, ResultMessages.RESULT_NOT_READY);
        } else {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, result.get(), ResultMessages.RESULT_IS_READY);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * Handles the process of waiting for a result to become available, building a response based on the status.
     *
     * @param requestId the unique request ID for retrieving the result
     * @return a {@link ResponseEntity} containing the {@link ArdocoResultResponse} with the status and result message
     * @throws ArdocoException          if an error occurs while waiting for the result
     * @throws IllegalArgumentException if the provided requestId is invalid
     * @throws TimeoutException         if waiting for the result times out
     */
    protected ResponseEntity<ArdocoResultResponse> handleWaitForResult(String requestId) throws ArdocoException, IllegalArgumentException, TimeoutException {
        Optional<String> result = service.waitForResult(requestId);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(requestId, HttpStatus.ACCEPTED, ResultMessages.REQUEST_TIMED_OUT);
        } else {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, result.get(), ResultMessages.RESULT_IS_READY);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }
}
