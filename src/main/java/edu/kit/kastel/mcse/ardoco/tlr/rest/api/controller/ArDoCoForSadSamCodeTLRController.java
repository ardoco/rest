/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.google.common.io.Files;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.impl.factory.SortedMaps;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.FileConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.ArDoCoForSadSamCodeTLRService;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for handling the TransArC (sad-sam-code) processing pipeline.
 */
@Tag(name = "TransArC (sad-sam-code) TraceLinkRecovery")
@RequestMapping("/api/transarc")
@RestController
public class ArDoCoForSadSamCodeTLRController extends AbstractController {

    private static final Logger logger = LogManager.getLogger(ArDoCoForSadSamCodeTLRController.class);

    /**
     * Constructs a new ArDoCoForSadSamCodeTLRController with the specified service.
     *
     * @param service the service responsible for trace link recovery operations
     */
    public ArDoCoForSadSamCodeTLRController(ArDoCoForSadSamCodeTLRService service) {
        super(service, TraceLinkType.SAD_SAM_CODE);
    }

    /**
     * Starts the TransArC (sad-sam-code) processing pipeline with the provided project name, architecture model type, and files.
     *
     * @param projectName               the name of the project
     * @param inputText                 the textual documentation of the project
     * @param inputArchitectureModel    the architecture model of the project
     * @param modelType                 the type of architecture model that is uploaded
     * @param inputCode                 the code of the project
     * @param additionalConfigsJson     JSON string containing additional ArDoCo configuration (optional)
     * @return a ResponseEntity containing the result of the processing pipeline
     * @throws FileNotFoundException if any of the input files are not found
     * @throws FileConversionException if there is an error converting multipart files to files
     */
    @Operation(summary = "Starts the TransArC (sad-sam-code) processing pipeline", description = "Starts the TransArc (sad-sam-code) processing pipeline with the given project name, the type of the architecture model and files.")
    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The textual documentation of the project", required = true) @RequestParam("inputText") MultipartFile inputText,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam("architectureModelType") ModelFormat modelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestPart(value = "additionalConfigs", required = false) String additionalConfigsJson)

    throws FileNotFoundException, FileConversionException {

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
     * @param projectName               the name of the project
     * @param inputText                 the textual documentation of the project
     * @param inputArchitectureModel    the architecture model of the project
     * @param modelType                 the type of architecture model that is uploaded
     * @param inputCode                 the code of the project
     * @param additionalConfigsJson     JSON string containing additional ArDoCo configuration (optional)
     * @return a ResponseEntity containing the result of the processing pipeline
     * @throws FileNotFoundException if any of the input files are not found
     * @throws FileConversionException if there is an error converting multipart files to files
     */
    @Operation(summary = "Starts the ardoco-pipeline to get a SadSamCodeTraceLinks and waits until the result is obtained", description = "performs the sadSamCode trace link recovery of ArDoCo with the given project name and files and waits until the SadSamCodeTraceLinks are obtained.")
    @PostMapping(value = "/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The textual documentation of the project", required = true) @RequestParam("inputText") MultipartFile inputText,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam("architectureModelType") ModelFormat modelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestPart(value = "additionalConfigs", required = false) String additionalConfigsJson)
    throws FileNotFoundException, FileConversionException {

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

        inputFiles.put("inputText", FileConverter.convertMultipartFileToFile(inputText));
        inputFiles.put("inputArchitectureModel", FileConverter.convertMultipartFileToFile(inputArchitectureModel));
        inputFiles.put("inputCode", FileConverter.convertMultipartFileToFile(inputCode));

        return inputFiles;
    }

    private Transarc setUpRunner(SortedMap<String, String> additionalConfigs, Map<String, File> inputFileMap, ModelFormat modelType, String projectName) {
        logger.info("Setting up Runner...");
        Transarc runner = new Transarc(projectName);

        ArchitectureConfiguration architectureConfiguration = new ArchitectureConfiguration(inputFileMap.get("inputArchitectureModel"), modelType);
        CodeConfiguration codeConfiguration = new CodeConfiguration(inputFileMap.get("inputCode"), CodeConfiguration.CodeConfigurationType.ACM_FILE);
        ImmutableSortedMap<String, String> additionalConfigsImmutable = SortedMaps.immutable.withSortedMap(additionalConfigs);

        runner.setUp(inputFileMap.get("inputText"), architectureConfiguration, codeConfiguration, additionalConfigsImmutable,
                Files.createTempDir());
        return runner;
    }
}