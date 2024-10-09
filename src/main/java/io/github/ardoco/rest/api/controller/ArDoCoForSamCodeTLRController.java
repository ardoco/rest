package io.github.ardoco.rest.api.controller;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSamCodeTraceabilityLinkRecovery;
import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.exception.*;
import io.github.ardoco.rest.api.service.AbstractRunnerTLRService;
import io.github.ardoco.rest.api.util.FileConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Tag(name = "Sam-Code TraceLinkRecovery")
@RestController
public class ArDoCoForSamCodeTLRController extends AbstractController {

    private static final Logger logger = LogManager.getLogger(ArDoCoForSamCodeTLRController.class);

    public ArDoCoForSamCodeTLRController(@Qualifier("samCodeTLRService") AbstractRunnerTLRService service) {
        super(service, TraceLinkType.SAM_CODE);
    }


    @Operation(
            summary = "Starts the sam-code processing pipeline",
            description = "Starts the sam-code processing pipeline with the given project name, the type of the architecture model and files."
    )
    @PostMapping(value = "/api/sam-code/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true)@RequestParam("architectureModelType") ArchitectureModelType architectureModelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode)
            throws FileNotFoundException, FileConversionException, HashingException {

        Map<String, File> inputFileMap = convertInputFilesHelper(inputCode, inputArchitectureModel);
        List <File> inputFiles = new ArrayList<>(inputFileMap.values());

        String id = generateRequestId(inputFiles, projectName);
        ArDoCoForSamCodeTraceabilityLinkRecovery runner = setUpRunner(inputFileMap, architectureModelType, projectName);

        return handleRunPipeLineResult(runner, id, inputFiles);
    }

    @Operation(
            summary = "Starts the ardoco-pipeline to get a SamCodeTraceLinks and waits until the result is obtained",
            description = "performs the SamCodeTraceLinks link recovery of ArDoCo with the given project name and files and waits until the SamCodeTraceLinks are obtained."
    )
    @PostMapping(value = "/api/sam-code/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true)@RequestParam("architectureModelType") ArchitectureModelType architectureModelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode)
            throws FileNotFoundException, FileConversionException, HashingException {

        Map<String, File> inputFileMap = convertInputFilesHelper(inputCode, inputArchitectureModel);
        List <File> inputFiles = new ArrayList<>(inputFileMap.values());

        String id = generateRequestId(inputFiles, projectName);
        ArDoCoForSamCodeTraceabilityLinkRecovery runner = setUpRunner(inputFileMap, architectureModelType, projectName);

        return handleRunPipelineAndWaitForResult(runner, id, inputFiles);
    }


    @Operation(
            summary = "Queries the TraceLinks for a given resultID, and returns it if it is ready",
            description = "Queries whether the TraceLinks are ready using the id, which was returned by tue runPipeline method. " +
                    "In case the result is not yet ready, the user gets informed about it via an appropriate message"
    )
    @GetMapping("/api/sam-code/{id}")
    public ResponseEntity<ArdocoResultResponse> getResult(
            @Parameter(description = "The ID of the result to query", required = true)  @PathVariable("id") String id)
            throws ArdocoException, IllegalArgumentException {
        return handleGetResult(id);
    }


    @Operation(
            summary = "Waits up to 60s for the TraceLinks and returns them when they are ready.",
            description = "Queries the TraceLinks and returns them when the previously started pipeline (using the runPipeline Method) has finished." +
                    "In case the result is not there within 60s of waiting, the user gets informed about it via an appropriate message"
    )
    @GetMapping("/api/sam-code/wait/{id}")
    public ResponseEntity<ArdocoResultResponse> waitForResult(
            @Parameter(description = "The ID of the result to query", required = true) @PathVariable("id") String id)
            throws ArdocoException, InterruptedException, IllegalArgumentException, TimeoutException {
        return handleWaitForResult(id);
    }

    private Map<String, File> convertInputFilesHelper(MultipartFile inputCode, MultipartFile inputArchitectureModel) {
        logger.log(Level.DEBUG, "Convert multipartFiles to files");
        Map<String, File> inputFiles = new HashMap<>();

        inputFiles.put("inputCodeFile", FileConverter.convertMultipartFileToFile(inputCode));
        inputFiles.put("inputArchitectureModelFile", FileConverter.convertMultipartFileToFile(inputArchitectureModel));

        return inputFiles;
    }

    private ArDoCoForSamCodeTraceabilityLinkRecovery setUpRunner(Map<String, File> inputFileMap, ArchitectureModelType modelType, String projectName) {
        SortedMap<String, String> additionalConfigs = new TreeMap<>(); // can be later added to api call as param if needed

        logger.log(Level.INFO, "Setting up Runner...");
        ArDoCoForSamCodeTraceabilityLinkRecovery runner = new ArDoCoForSamCodeTraceabilityLinkRecovery(projectName);
        runner.setUp(
                inputFileMap.get("inputArchitectureModelFile"),
                modelType,
                inputFileMap.get("inputCodeFile"),
                additionalConfigs,
                Files.createTempDir());
        return runner;
    }
}
