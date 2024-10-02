package io.github.ardoco.rest.api.controller;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadCodeTraceabilityLinkRecovery;
import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.exception.*;
import io.github.ardoco.rest.api.service.AbstractRunnerTLRService;
import io.github.ardoco.rest.api.util.FileConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

import org.springframework.http.MediaType;

@Tag(name = "Sad-Code TraceLinkRecovery")
@RestController
public class ArDoCoForSadCodeTLRController extends AbstractController{

    private static final Logger logger = LogManager.getLogger(ArDoCoForSadCodeTLRController.class);

    public ArDoCoForSadCodeTLRController(@Qualifier("sadCodeTLRService") AbstractRunnerTLRService service) {
        super(service, TraceLinkType.SAD_CODE);
    }


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

        Map<String, File> inputFileMap = convertInputFilesHelper(inputText, inputCode);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());

        String id = generateRequestId(inputFiles, projectName);
        ArDoCoForSadCodeTraceabilityLinkRecovery runner = setUpRunner(inputFileMap, projectName);

        return handleRunPipeLineResult(runner, id, inputFiles);
    }

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
            throws FileConversionException, HashingException, ArdocoException, TimeoutException {

        Map<String, File> inputFileMap = convertInputFilesHelper(inputText, inputCode);
        List <File> inputFiles = new ArrayList<>(inputFileMap.values());

        String id = generateRequestId(inputFiles, projectName);
        ArDoCoForSadCodeTraceabilityLinkRecovery runner = setUpRunner(inputFileMap, projectName);

        return handleRunPipelineAndWaitForResult(runner, id, inputFiles);
    }


    @Operation(
            summary = "Queries whether the ArDoCoResult is already there.",
            description = "Queries whether the SadCodeTraceLinks is already there using the id which was returned by tue runPipeline method. " +
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
        return handleGetResult(id);
    }


    @Operation(
            summary = "Queries the SadCodeTraceLinks and returns them when they are ready.",
            description = "Queries the SamSadTraceLinks and returns them when the previously started pipeline (using the runPipeline Method) has finished." +
                    "In case it is not ready yet, it performs busy-waiting, meaning it waits until the result ready "
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "the sadCodeTraceLinks found by ardoco", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),
            @ApiResponse(responseCode = "202", description = "the sadCodeTraceLinks are not ready yet, the waiting timed out", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),

    })
    @GetMapping("/api/sad-code/wait/{id}")
    public ResponseEntity<ArdocoResultResponse> waitForResult(
            @Parameter(description = "The ID of the result to query", required = true) @PathVariable("id") String id)
            throws ArdocoException, InterruptedException, IllegalArgumentException, TimeoutException {
        return handleWaitForResult(id);
    }

    private Map<String, File> convertInputFilesHelper(MultipartFile inputText, MultipartFile inputCode) {
        logger.log(Level.INFO, "Convert multipartFiles to files");
        Map<String, File> inputFiles = new HashMap<>();

        inputFiles.put("inputText", FileConverter.convertMultipartFileToFile(inputText));
        inputFiles.put("inputCode", FileConverter.convertMultipartFileToFile(inputCode));

        return inputFiles;
    }

    private ArDoCoForSadCodeTraceabilityLinkRecovery setUpRunner(Map<String, File> inputFileMap, String projectName) {
        SortedMap<String, String> additionalConfigs = new TreeMap<>(); // can be later added to api call as param if needed

        logger.log(Level.INFO, "Setting up Runner...");
        ArDoCoForSadCodeTraceabilityLinkRecovery runner = new ArDoCoForSadCodeTraceabilityLinkRecovery(projectName);

        runner.setUp(
                inputFileMap.get("inputText"),
                inputFileMap.get("inputCode"),
                additionalConfigs,
                Files.createTempDir()
        );
        return runner;
    }
}
