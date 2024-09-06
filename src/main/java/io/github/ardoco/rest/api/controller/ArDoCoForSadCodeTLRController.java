package io.github.ardoco.rest.api.controller;

import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import io.github.ardoco.rest.api.exception.ArdocoException;
import io.github.ardoco.rest.api.exception.FileConversionException;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import io.github.ardoco.rest.api.exception.HashingException;
import io.github.ardoco.rest.api.service.RunnerTLRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.http.MediaType;

@RestController
// @Tag(name = "ArDoCo Sad-Sam-TLR", description = "API for managing ArDoCo Sad Sam Trace Link Recovery")
public class ArDoCoForSadCodeTLRController {

    private final RunnerTLRService runnerTLRService;

    public ArDoCoForSadCodeTLRController(@Qualifier("sadCodeTLRService") RunnerTLRService runnerTLRService) {
        this.runnerTLRService = runnerTLRService;
    }


    @Tag(name = "Start Sad-Sam TraceLinkRecovery")
    @Operation(
            summary = "Starts the processing pipeline",
            description = "Starts the sad-code processing pipeline with the given project name and files."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The id which can be used to later retrieve the samSadCode traceLinks.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),
    })
    @PostMapping(value = "/api/sad-code/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The documentation of the project", required = true) @RequestParam("inputText") MultipartFile inputText,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode)
            throws FileNotFoundException, FileConversionException, HashingException {

        SortedMap<String, String> additionalConfigs = new TreeMap<>(); // can be later added to api call as param if needed
        String projectId = runnerTLRService.runPipeline(projectName, inputText, inputCode, additionalConfigs);

        ArdocoResultResponse response = new ArdocoResultResponse(projectId, HttpStatus.OK);
        return new ResponseEntity<>(response, response.getStatus());
    }


    @Tag(name = "Retrieve Sad-Sam TraceLinks")
    @Operation(
            summary = "Queries whether the ArDoCoResult is already there.",
            description = "Queries whether the SamSadTraceLinks is already there using the id which was returned by tue runPipeline method. " +
                    "In case the result is not yet ready, the user gets informed about that as well via an appropriate message"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "the sadCodeTraceLinks found by ardoco", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),
            @ApiResponse(responseCode = "202", description = "the sadCodeTraceLinks are not ready yet", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),
    })
    @GetMapping("/api/sad-code/{id}")
    public ResponseEntity<ArdocoResultResponse> getResult(
            @Parameter(description = "The ID of the result to query", required = true)  @PathVariable("id") String id)
            throws ArdocoException, IllegalArgumentException {

        Optional<String> result = runnerTLRService.getResult(id);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(id, HttpStatus.ACCEPTED, "Result is still being processed. Please try again later.");
        } else {
            response = new ArdocoResultResponse(HttpStatus.OK, result.get());
        }
        return new ResponseEntity<>(response, response.getStatus());
    }


    @Tag(name = "Retrieve Sad-Sam TraceLinks")
    @Operation(
            summary = "Queries the SamSadTraceLinks and returns them when they are ready.",
            description = "Queries the SamSadTraceLinks and returns them when the previously started pipeline (using the runPipeline Method) has finished." +
                    "In case it is not ready yet, it performs busy-waiting, meaning it waits until the result ready "
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "the sadCodeTraceLinks found by ardoco", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),
    })
    @GetMapping("/api/sad-code/wait/{id}")
    public ResponseEntity<ArdocoResultResponse> waitForResult(
            @Parameter(description = "The ID of the result to query", required = true) @PathVariable("id") String id)
            throws ArdocoException, InterruptedException, IllegalArgumentException {

        String result = runnerTLRService.waitForResult(id);
        ArdocoResultResponse response = new ArdocoResultResponse(HttpStatus.OK, result);
        return new ResponseEntity<>(response, response.getStatus());
    }


    @Tag(name = "Start Sad-Sam TraceLinkRecovery")
    @Operation(
            summary = "Starts the ardoco-pipeline to get a SadCodeTraceLinks and waits until the result is obtained",
            description = "performs the sadCode trace link recovery of ArDoCo with the given project name and files and waits until the SadCodeTraceLinks are obtained."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "the sadCodeTraceLinks found by ardoco", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),
    })
    @PostMapping(value = "/api/sad-code/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The documentation of the project", required = true) @RequestParam("inputText") MultipartFile inputText,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode)
            throws FileConversionException, HashingException, ArdocoException {

        SortedMap<String, String> additionalConfigs = new TreeMap<>(); // can be later added to api call as param if needed
        String result = runnerTLRService.runPipelineAndWaitForResult(projectName, inputText, inputCode, additionalConfigs);
        ArdocoResultResponse response = new ArdocoResultResponse(HttpStatus.OK, result);
        return new ResponseEntity<>(response, response.getStatus());
    }

}
