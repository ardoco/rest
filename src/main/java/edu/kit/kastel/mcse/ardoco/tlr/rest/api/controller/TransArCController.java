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

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.impl.factory.SortedMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.FileConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.TransArCService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for handling the TransArC (sad-sam-code) processing pipeline.
 */
@Tag(name = "TransArC (sad-sam-code) TraceLinkRecovery")
@RequestMapping("/api/transarc")
@RestController
public class TransArCController extends AbstractController {

    private static final Logger logger = LoggerFactory.getLogger(TransArCController.class);

    /**
     * Constructs a new TransArCController with the specified service.
     *
     * @param service the service responsible for trace link recovery operations
     */
    public TransArCController(TransArCService service) {
        super(service, TraceLinkType.SAD_SAM_CODE);
    }

    /**
     * Starts the TransArC (sad-sam-code) processing pipeline with the provided project name, architecture model type, and files.
     *
     * @param projectName            the name of the project
     * @param inputText              the textual documentation of the project
     * @param inputArchitectureModel the architecture model of the project
     * @param modelType              the type of architecture model that is uploaded
     * @param inputCode              the code of the project
     * @param additionalConfigsJson  JSON string containing additional ArDoCo configuration (optional)
     * @return a ResponseEntity containing the result of the processing pipeline
     * @throws FileNotFoundException   if any of the input files are not found
     * @throws FileConversionException if there is an error converting multipart files to files
     */
    @Operation(summary = "Starts the TransArC (sad-sam-code) processing pipeline", description = "Starts the TransArc (sad-sam-code) processing pipeline with the given project name, the type of the architecture model and files.")
    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam(PROJECT_NAME_PARAMETER) String projectName,
            @Parameter(description = "The textual documentation of the project", required = true) @RequestParam(TEXTUAL_DOCUMENTATION_PARAMETER) MultipartFile inputText,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam(ARCHITECTURE_MODEL_PARAMETER) MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam(ARCHITECTURE_MODEL_FORMAT_PARAMETER) ModelFormat modelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam(CODE_PARAMETER) MultipartFile inputCode,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestParam(value = ADDITIONAL_CONFIGURATION_PARAMETER, required = false) String additionalConfigsJson)

            throws FileNotFoundException, FileConversionException, IOException {

        Map<String, File> inputFileMap = convertInputFiles(inputText, inputArchitectureModel, inputCode);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        Transarc runner = setUpRunner(additionalConfigs, inputFileMap, modelType, projectName);

        return handleRunPipeLineResult(runner, id, inputFiles);
    }

    /**
     * Starts the TransArc (sad-sam-code) processing pipeline and waits for the result.
     *
     * @param projectName            the name of the project
     * @param inputText              the textual documentation of the project
     * @param inputArchitectureModel the architecture model of the project
     * @param modelType              the type of architecture model that is uploaded
     * @param inputCode              the code of the project
     * @param additionalConfigsJson  JSON string containing additional ArDoCo configuration (optional)
     * @return a ResponseEntity containing the result of the processing pipeline
     * @throws FileNotFoundException   if any of the input files are not found
     * @throws FileConversionException if there is an error converting multipart files to files
     */
    @Operation(summary = "Starts the ardoco-pipeline to get a SadSamCodeTraceLinks and waits until the result is obtained", description = "performs the sadSamCode trace link recovery of ArDoCo with the given project name and files and waits until the SadSamCodeTraceLinks are obtained.")
    @PostMapping(value = "/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam(PROJECT_NAME_PARAMETER) String projectName,
            @Parameter(description = "The textual documentation of the project", required = true) @RequestParam(TEXTUAL_DOCUMENTATION_PARAMETER) MultipartFile inputText,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam(ARCHITECTURE_MODEL_PARAMETER) MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam(ARCHITECTURE_MODEL_FORMAT_PARAMETER) ModelFormat modelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam(CODE_PARAMETER) MultipartFile inputCode,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestParam(value = ADDITIONAL_CONFIGURATION_PARAMETER, required = false) String additionalConfigsJson)
            throws FileNotFoundException, FileConversionException, IOException {

        Map<String, File> inputFileMap = convertInputFiles(inputText, inputArchitectureModel, inputCode);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        Transarc runner = setUpRunner(additionalConfigs, inputFileMap, modelType, projectName);

        return handleRunPipelineAndWaitForResult(runner, id, inputFiles);
    }

    private Map<String, File> convertInputFiles(MultipartFile inputText, MultipartFile inputArchitectureModel, MultipartFile inputCode) {
        logger.info("Convert multipartFiles to files");
        Map<String, File> inputFiles = new HashMap<>();

        inputFiles.put(TEXTUAL_DOCUMENTATION_PARAMETER, FileConverter.convertMultipartFileToFile(inputText));
        inputFiles.put(ARCHITECTURE_MODEL_PARAMETER, FileConverter.convertMultipartFileToFile(inputArchitectureModel));
        inputFiles.put(CODE_PARAMETER, FileConverter.convertMultipartFileToFile(inputCode));

        return inputFiles;
    }

    private Transarc setUpRunner(SortedMap<String, String> additionalConfigs, Map<String, File> inputFileMap, ModelFormat modelType, String projectName)
            throws IOException {
        logger.info("Setting up Runner...");
        Transarc runner = new Transarc(projectName);

        ArchitectureConfiguration architectureConfiguration = new ArchitectureConfiguration(inputFileMap.get(ARCHITECTURE_MODEL_PARAMETER), modelType);
        CodeConfiguration codeConfiguration = new CodeConfiguration(inputFileMap.get(CODE_PARAMETER), CodeConfiguration.CodeConfigurationType.ACM_FILE);
        ImmutableSortedMap<String, String> additionalConfigsImmutable = SortedMaps.immutable.withSortedMap(additionalConfigs);

        runner.setUp(inputFileMap.get(TEXTUAL_DOCUMENTATION_PARAMETER), architectureConfiguration, codeConfiguration, additionalConfigsImmutable, Files
                .createTempDirectory("ardoco-sad-sam-code")
                .toFile());
        return runner;
    }
}
