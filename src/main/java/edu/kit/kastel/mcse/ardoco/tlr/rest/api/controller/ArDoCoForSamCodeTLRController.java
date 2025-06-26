/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSamCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.FileConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.ArDoCoForSamCodeTLRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Sam-Code TraceLinkRecovery")
@RequestMapping("/api/sam-code")
@RestController
public class ArDoCoForSamCodeTLRController extends AbstractController {

    private static final Logger logger = LogManager.getLogger(ArDoCoForSamCodeTLRController.class);

    public ArDoCoForSamCodeTLRController(ArDoCoForSamCodeTLRService service) {
        super(service, TraceLinkType.SAM_CODE);
    }

    @Operation(summary = "Starts the sam-code processing pipeline", description = "Starts the sam-code processing pipeline with the given project name, the type of the architecture model and files.")
    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam("architectureModelType") ArchitectureModelType architectureModelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestPart(value = "additionalConfigs", required = false) String additionalConfigsJson)
            throws FileNotFoundException, FileConversionException {

        Map<String, File> inputFileMap = convertInputFiles(inputCode, inputArchitectureModel);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        ArDoCoForSamCodeTraceabilityLinkRecovery runner = setUpRunner(additionalConfigs, inputFileMap, architectureModelType, projectName);

        return handleRunPipeLineResult(runner, id, inputFiles);
    }

    @Operation(summary = "Starts the ardoco-pipeline to get a SamCodeTraceLinks and waits until the result is obtained", description = "performs the SamCodeTraceLinks link recovery of ArDoCo with the given project name and files and waits until the SamCodeTraceLinks are obtained.")
    @PostMapping(value = "/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam("architectureModelType") ArchitectureModelType architectureModelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestPart(value = "additionalConfigs", required = false) String additionalConfigsJson)

    throws FileNotFoundException, FileConversionException {

        Map<String, File> inputFileMap = convertInputFiles(inputCode, inputArchitectureModel);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        ArDoCoForSamCodeTraceabilityLinkRecovery runner = setUpRunner(additionalConfigs, inputFileMap, architectureModelType, projectName);

        return handleRunPipelineAndWaitForResult(runner, id, inputFiles);
    }

    private Map<String, File> convertInputFiles(MultipartFile inputCode, MultipartFile inputArchitectureModel) {
        logger.debug("Convert multipartFiles to files...");
        Map<String, File> inputFiles = new HashMap<>();

        inputFiles.put("inputCodeFile", FileConverter.convertMultipartFileToFile(inputCode));
        inputFiles.put("inputArchitectureModelFile", FileConverter.convertMultipartFileToFile(inputArchitectureModel));

        return inputFiles;
    }

    private ArDoCoForSamCodeTraceabilityLinkRecovery setUpRunner(SortedMap<String, String> additionalConfigs, Map<String, File> inputFileMap, ArchitectureModelType modelType, String projectName) {
        logger.info("Setting up Runner...");
        ArDoCoForSamCodeTraceabilityLinkRecovery runner = new ArDoCoForSamCodeTraceabilityLinkRecovery(projectName);
        runner.setUp(inputFileMap.get("inputArchitectureModelFile"), modelType, inputFileMap.get("inputCodeFile"), additionalConfigs, Files.createTempDir());
        return runner;
    }
}
