package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.FileConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;
import edu.kit.kastel.mcse.ardoco.id.execution.runner.ArDoCoForInconsistencyDetection;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.InconsistencyService;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.util.HashGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

@Tag(name = "Sad-Sam TraceLinkRecovery with Inconsistency Detection")
@RequestMapping("/api/find-inconsistencies")
@RestController
public class InconsistencyController extends AbstractController {
    private static final Logger logger = LogManager.getLogger(InconsistencyController.class);

    public InconsistencyController(InconsistencyService service) {
        super(service, TraceLinkType.SAD_SAM);
    }

    @Operation(summary = "Starts the sad-sam processing pipeline with Inconsistency Detection",
            description = "Starts the inconsistency processing pipeline with the given project name, the type of the architecture model and files.")
    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The textual documentation of the project", required = true) @RequestParam("inputText") MultipartFile inputText,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam("architectureModelType") ArchitectureModelType modelType,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false)
            @RequestPart(value = "additionalConfigs", required = false) String additionalConfigsJson)
            throws FileNotFoundException, FileConversionException {

        Map<String, File> inputFileMap = convertInputFiles(inputText, inputArchitectureModel);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        ArDoCoForInconsistencyDetection runner = setUpRunner(inputFileMap, modelType, projectName, additionalConfigs);

        return handleRunPipeLineResult(runner, id, inputFiles);
    }

    @Operation(summary = "Starts the ardoco-pipeline to get a SadSamTraceLinks as well as Inconsistencies and waits until the result is obtained",
            description = "performs the inconsistency pipeline of ArDoCo with the given project name and files and waits until the SadSamTraceLinks and Inconsistencies are obtained.")
    @PostMapping(value = "/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The textual documentation of the project", required = true) @RequestParam("inputText") MultipartFile inputText,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true) @RequestParam("architectureModelType") ArchitectureModelType modelType,
            @Parameter(description = "JSON string containing additional ArDoCo configuration. If not provided, the default configuration of ArDoCo is used.", required = false)
            @RequestPart(value = "additionalConfigs", required = false) String additionalConfigsJson)
            throws FileNotFoundException, FileConversionException {

        Map<String, File> inputFileMap = convertInputFiles(inputText, inputArchitectureModel);
        List<File> inputFiles = new ArrayList<>(inputFileMap.values());
        SortedMap<String, String> additionalConfigs = parseAdditionalConfigs(additionalConfigsJson);

        String id = generateRequestId(inputFiles, projectName);
        ArDoCoForInconsistencyDetection runner = setUpRunner(inputFileMap, modelType, projectName, additionalConfigs);

        return handleRunPipelineAndWaitForResult(runner, id, inputFiles);
    }



    private Map<String, File> convertInputFiles(MultipartFile inputText, MultipartFile inputArchitectureModel) {
        logger.info("Convert multipartFiles to files");
        Map<String, File> inputFiles = new HashMap<>();

        inputFiles.put("inputText", FileConverter.convertMultipartFileToFile(inputText));
        inputFiles.put("inputArchitectureModel", FileConverter.convertMultipartFileToFile(inputArchitectureModel));

        return inputFiles;
    }

    private ArDoCoForInconsistencyDetection setUpRunner(Map<String, File> inputFileMap, ArchitectureModelType modelType, String projectName, SortedMap<String, String> additionalConfigs)
            throws FileNotFoundException, FileConversionException {
        logger.info("Setting up Runner...");
        ArDoCoForInconsistencyDetection runner = new ArDoCoForInconsistencyDetection(projectName);
        runner.setUp(inputFileMap.get("inputText"), inputFileMap.get("inputArchitectureModel"), modelType, additionalConfigs, Files.createTempDir());
        return runner;
    }

    @Override
    protected String generateRequestId(List<File> files, String projectName) throws FileNotFoundException, FileConversionException {
        logger.info("Generating ID...");
        String hash = HashGenerator.getMD5HashFromFiles(files);
        return TraceLinkType.SAD_SAM.getKeyPrefix() + "Inconsistency:" + projectName + hash;
    }
}
