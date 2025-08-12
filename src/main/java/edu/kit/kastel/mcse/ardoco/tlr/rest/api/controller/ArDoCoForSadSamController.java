/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.impl.factory.SortedMaps;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.FileConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.ArDoCoForSadSamTLRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * This controller handles the REST API endpoints for the SWATTR (sad-sam) trace link recovery process.
 */
@Tag(name = "SWATTR (sad-sam) TraceLinkRecovery")
@RequestMapping("/api/swattr")
@RestController
public class ArDoCoForSadSamController extends AbstractController {

    private static final Logger logger = LogManager.getLogger(ArDoCoForSadSamController.class);

    /**
     * Constructs a new {@code ArDoCoForSadSamController} with the specified service.
     *
     * @param service the service responsible for trace link recovery operations
     */
    public ArDoCoForSadSamController(ArDoCoForSadSamTLRService service) {
        super(service, TraceLinkType.SAD_SAM);
    }

    /**
     * Starts the SWATTR (sad-sam) processing pipeline with the given project name, architecture model type, and files.
     *
     * @param projectName            The name of the project.
     * @param inputText              The textual documentation of the project.
     * @param inputArchitectureModel The architecture model of the project.
     * @param modelType              The type of architecture model that is uploaded.
     * @param additionalConfigsJson  JSON string containing additional ArDoCo configuration.
     * @return ResponseEntity containing the result of the processing pipeline.
     * @throws FileNotFoundException   if a required file is not found.
     * @throws FileConversionException if there is an error converting files.
     */
    @Operation(summary = "Starts the SWATTR (sad-sam) processing pipeline", description = "Starts the SWATTR (sad-sam) processing pipeline with the given project name, the type of the architecture model and files.")
    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The textual documentation of the project", required = true) @RequestParam("inputText") MultipartFile inputText,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam("architectureModelType") ModelFormat modelType,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestPart(value = "additionalConfigs", required = false) String additionalConfigsJson)
            throws FileNotFoundException, FileConversionException, IOException {

        Map<String, File> inputFileMap = convertInputFiles(inputText, inputArchitectureModel);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        Swattr runner = setUpRunner(inputFileMap, modelType, projectName, additionalConfigs);

        return handleRunPipeLineResult(runner, id, inputFiles);
    }

    /**
     * Starts the SWATTR (sad-sam) processing pipeline and waits for the result with the given project name, architecture model type, and files.
     *
     * @param projectName            The name of the project.
     * @param inputText              The textual documentation of the project.
     * @param inputArchitectureModel The architecture model of the project.
     * @param modelType              The type of architecture model that is uploaded.
     * @param additionalConfigsJson  JSON string containing additional ArDoCo configuration.
     * @return ResponseEntity containing the result of the processing pipeline.
     * @throws FileNotFoundException   if a required file is not found.
     * @throws FileConversionException if there is an error converting files.
     */
    @Operation(summary = "Starts the SWATTR (sad-sam) processing pipeline and waits until the result is obtained", description = "performs the SadSamTraceLinks link recovery of ArDoCo with the given project name and files and waits until the SadSamTraceLinks are obtained.")
    @PostMapping(value = "/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The textual documentation of the project", required = true) @RequestParam("inputText") MultipartFile inputText,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam("architectureModelType") ModelFormat modelType,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestPart(value = "additionalConfigs", required = false) String additionalConfigsJson)
            throws FileNotFoundException, FileConversionException, IOException {

        Map<String, File> inputFileMap = convertInputFiles(inputText, inputArchitectureModel);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        Swattr runner = setUpRunner(inputFileMap, modelType, projectName, additionalConfigs);

        return handleRunPipelineAndWaitForResult(runner, id, inputFiles);
    }

    private Map<String, File> convertInputFiles(MultipartFile inputText, MultipartFile inputArchitectureModel) {
        logger.info("Convert multipartFiles to files");
        Map<String, File> inputFiles = new HashMap<>();

        inputFiles.put("inputText", FileConverter.convertMultipartFileToFile(inputText));
        inputFiles.put("inputArchitectureModel", FileConverter.convertMultipartFileToFile(inputArchitectureModel));

        return inputFiles;
    }

    private Swattr setUpRunner(Map<String, File> inputFileMap, ModelFormat modelType, String projectName, SortedMap<String, String> additionalConfigs)
            throws FileNotFoundException, FileConversionException, IOException {
        logger.info("Setting up Runner...");
        Swattr runner = new Swattr(projectName);

        ArchitectureConfiguration architectureConfiguration = new ArchitectureConfiguration(inputFileMap.get("inputArchitectureModel"), modelType);
        ImmutableSortedMap<String, String> additionalConfigsImmutable = SortedMaps.immutable.withSortedMap(additionalConfigs);

        runner.setUp(inputFileMap.get("inputText"), architectureConfiguration, additionalConfigsImmutable, Files.createTempDirectory("ardoco-sad-sam")
                .toFile());
        return runner;
    }
}
