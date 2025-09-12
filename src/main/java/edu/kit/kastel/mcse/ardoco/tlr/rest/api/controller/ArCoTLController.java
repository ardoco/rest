/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Arcotl;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.FileConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.ArCoTLService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.impl.factory.SortedMaps;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

// Sam-Code TraceLink Recovery Controller

/**
 * This controller handles the REST API endpoints for the ArCoTL (sam-code) trace link recovery process.
 */
@Tag(name = "ArCoTL (sam-code) TraceLinkRecovery")
@RequestMapping("/api/arcotl")
@RestController
public class ArCoTLController extends AbstractController {

    private static final Logger logger = LogManager.getLogger(ArCoTLController.class);

    /**
     * Constructs a new {@code ArCoTLController} with the specified service.
     *
     * @param service the service responsible for trace link recovery operations
     */
    public ArCoTLController(ArCoTLService service) {
        super(service, TraceLinkType.SAM_CODE);
    }

    /**
     * Starts the Arcotl (sam-code) processing pipeline with the given project name, architecture model, and code files.
     *
     * @param projectName            the name of the project
     * @param inputArchitectureModel the architecture model file
     * @param architectureModelType  the type of architecture model
     * @param inputCode              the code file
     * @param additionalConfigsJson  JSON string containing additional ArDoCo configuration
     * @return ResponseEntity containing the result of the processing pipeline
     * @throws FileNotFoundException   if a required file is not found
     * @throws FileConversionException if there is an error converting files
     */
    @Operation(summary = "Starts the ArCoTL (sam-code) processing pipeline", description = "Starts the ArCoTL (sam-code) processing pipeline with the given project name, the type of the architecture model and files.")
    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam("architectureModelType") ModelFormat architectureModelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestParam(value = "additionalConfigs", required = false) String additionalConfigsJson)
            throws FileNotFoundException, FileConversionException, IOException {

        Map<String, File> inputFileMap = convertInputFiles(inputCode, inputArchitectureModel);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        Arcotl runner = setUpRunner(additionalConfigs, inputFileMap, architectureModelType, projectName);

        return handleRunPipeLineResult(runner, id, inputFiles);
    }

    /**
     * Starts the Arcotl pipeline to get a SamCodeTraceLinks and waits until the result is obtained.
     *
     * @param projectName            the name of the project
     * @param inputArchitectureModel the architecture model file
     * @param architectureModelType  the type of architecture model
     * @param inputCode              the code file
     * @param additionalConfigsJson  JSON string containing additional ArDoCo configuration
     * @return ResponseEntity containing the result of the processing pipeline
     * @throws FileNotFoundException   if a required file is not found
     * @throws FileConversionException if there is an error converting files
     */
    @Operation(summary = "Starts the ArCoTL (sam-code) processing pipeline and waits until the result is obtained", description = "Starts the ArCoTL (sam-code) processing pipeline with the given project name, the type of the architecture model and files. and waits until the SamCodeTraceLinks are obtained.")
    @PostMapping(value = "/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam("architectureModelType") ModelFormat architectureModelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestParam(value = "additionalConfigs", required = false) String additionalConfigsJson)

            throws FileNotFoundException, FileConversionException, IOException {

        Map<String, File> inputFileMap = convertInputFiles(inputCode, inputArchitectureModel);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        Arcotl runner = setUpRunner(additionalConfigs, inputFileMap, architectureModelType, projectName);

        return handleRunPipelineAndWaitForResult(runner, id, inputFiles);
    }

    private Map<String, File> convertInputFiles(MultipartFile inputCode, MultipartFile inputArchitectureModel) {
        logger.debug("Convert multipartFiles to files...");
        Map<String, File> inputFiles = new HashMap<>();

        inputFiles.put("inputCode", FileConverter.convertMultipartFileToFile(inputCode));
        inputFiles.put("inputArchitectureModel", FileConverter.convertMultipartFileToFile(inputArchitectureModel));

        return inputFiles;
    }

    private Arcotl setUpRunner(SortedMap<String, String> additionalConfigs, Map<String, File> inputFileMap, ModelFormat modelType, String projectName)
            throws IOException {
        logger.info("Setting up Runner...");
        Arcotl runner = new Arcotl(projectName);

        ArchitectureConfiguration architectureConfiguration = new ArchitectureConfiguration(inputFileMap.get("inputArchitectureModel"), modelType);
        CodeConfiguration codeConfiguration = new CodeConfiguration(inputFileMap.get("inputCode"), CodeConfiguration.CodeConfigurationType.ACM_FILE);
        ImmutableSortedMap<String, String> additionalConfigsImmutable = SortedMaps.immutable.withSortedMap(additionalConfigs);

        runner.setUp(architectureConfiguration, codeConfiguration, additionalConfigsImmutable, Files.createTempDirectory("ardoco-sam-code").toFile());
        return runner;
    }
}
