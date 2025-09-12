/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import edu.kit.kastel.mcse.ardoco.tlr.execution.Ardocode;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.FileConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.ArdocoException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.TimeoutException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.ArDoCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

/**
 * This controller handles the REST API endpoints for the ArDoCode (sad-code) trace link recovery process.
 */
@Tag(name = "ArDoCode (sad-code) TraceLinkRecovery")
@RequestMapping("/api/ardocode")
@RestController
public class ArDoCodeController extends AbstractController {

    private static final Logger logger = LogManager.getLogger(ArDoCodeController.class);

    /**
     * Constructs a new {@code ArDoCodeController} with the specified service.
     *
     * @param service the service responsible for trace link recovery operations
     */
    public ArDoCodeController(ArDoCodeService service) {
        super(service, TraceLinkType.SAD_CODE);
    }

    /**
     * Starts the processing pipeline for ArDoCode (sad-code) trace link recovery.
     *
     * @param projectName           the name of the project
     * @param inputText             the documentation of the project as a MultipartFile
     * @param inputCode             the code of the project as a MultipartFile
     * @param additionalConfigsJson JSON string containing additional ArDoCo configuration
     * @return ResponseEntity containing the result response with the request ID
     * @throws FileNotFoundException   if a required file is not found
     * @throws FileConversionException if there is an error converting files
     */
    @Operation(summary = "Starts ArDoCode (sad-code) the processing pipeline", description = "Starts the ArDoCode (sad-code) processing pipeline with the given project name and files.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The id which can be used to later retrieve the samSadCode traceLinks.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),

    })
    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The documentation of the project", required = true) @RequestParam("inputText") MultipartFile inputText,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestParam(value = "additionalConfigs", required = false) String additionalConfigsJson)

            throws FileNotFoundException, FileConversionException, IOException {

        Map<String, File> inputFileMap = convertInputFiles(inputText, inputCode);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        Ardocode runner = setUpRunner(additionalConfigs, inputFileMap, projectName);

        return handleRunPipeLineResult(runner, id, inputFiles);
    }

    /**
     * Starts the ArDoCode (sad-code) processing pipeline and waits until the result is obtained.
     *
     * @param projectName           the name of the project
     * @param inputText             the documentation of the project as a MultipartFile
     * @param inputCode             the code of the project as a MultipartFile
     * @param additionalConfigsJson JSON string containing additional ArDoCo configuration
     * @return ResponseEntity containing the result response with the sadCodeTraceLinks
     * @throws FileConversionException if there is an error converting files
     * @throws ArdocoException         if there is an error during processing
     * @throws TimeoutException        if waiting for the result times out
     */
    @Operation(summary = "Starts the ArDoCode (sad-code) processing pipeline and waits until the result is obtained", description = "performs the sadCode trace link recovery of ArDoCo with the given project name and files and waits until the SadCodeTraceLinks are obtained.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "the sadCodeTraceLinks found by ardoco", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArdocoResultResponse.class))),})
    @PostMapping(value = "/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The documentation of the project", required = true) @RequestParam("inputText") MultipartFile inputText,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false) @RequestParam(value = "additionalConfigs", required = false) String additionalConfigsJson)

            throws FileConversionException, ArdocoException, TimeoutException, IOException {

        Map<String, File> inputFileMap = convertInputFiles(inputText, inputCode);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        Ardocode runner = setUpRunner(additionalConfigs, inputFileMap, projectName);

        return handleRunPipelineAndWaitForResult(runner, id, inputFiles);
    }

    private Map<String, File> convertInputFiles(MultipartFile inputText, MultipartFile inputCode) {
        logger.info("Convert multipartFiles to files");
        Map<String, File> inputFiles = new HashMap<>();

        inputFiles.put("inputText", FileConverter.convertMultipartFileToFile(inputText));
        inputFiles.put("inputCode", FileConverter.convertMultipartFileToFile(inputCode));

        return inputFiles;
    }

    private Ardocode setUpRunner(SortedMap<String, String> additionalConfigs, Map<String, File> inputFileMap, String projectName) throws IOException {
        logger.info("Setting up Runner...");
        Ardocode runner = new Ardocode(projectName);
        CodeConfiguration codeConfiguration = new CodeConfiguration(inputFileMap.get("inputCode"), CodeConfiguration.CodeConfigurationType.ACM_FILE);
        ImmutableSortedMap<String, String> additionalConfigsImmutable = SortedMaps.immutable.withSortedMap(additionalConfigs);

        runner.setUp(inputFileMap.get("inputText"), codeConfiguration, additionalConfigsImmutable, Files.createTempDirectory("ardoco-sad-code").toFile());
        return runner;
    }
}
