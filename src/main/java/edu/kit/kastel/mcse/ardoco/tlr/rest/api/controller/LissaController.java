/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArdocoResultResponse;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.FileConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.messages.ResultMessages;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.LissaService;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.service.ResultService;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.util.HashGenerator;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact.ArtifactType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "LiSSA TraceLinkRecovery")
@RequestMapping("/api/lissa")
@RestController
public class LissaController {

    public static final String PROJECT_NAME_PARAMETER = "projectName";
    public static final String SOURCE_ARTIFACTS_PARAMETER = "inputSourceArtifacts";
    public static final String SOURCE_ARTIFACT_TYPE_PARAMETER = "sourceArtifactType";
    public static final String TARGET_ARTIFACTS_PARAMETER = "inputTargetArtifacts";
    public static final String TARGET_ARTIFACT_TYPE_PARAMETER = "targetArtifactType";
    public static final String ADDITIONAL_CONFIGURATION_PARAMETER = "configuration";
    public static final String API_KEYS_PARAMETER = "apiKeys";

    private static final Logger logger = LoggerFactory.getLogger(LissaController.class);

    private final LissaService service;

    @Autowired
    private ResultService resultService;

    public LissaController(LissaService service) {
        this.service = service;
    }

    @Operation(summary = "Starts the LiSSA processing pipeline", description = "Starts the LiSSA trace link recovery pipeline for the given source and target artifacts. Multiple files may be uploaded per side; each side's artifact type must be specified explicitly.")
    @PostMapping(value = "/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam(PROJECT_NAME_PARAMETER) String projectName,

            @Parameter(description = "One or more source artifact files (e.g. requirements/documentation)", required = true, array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))) @RequestPart(SOURCE_ARTIFACTS_PARAMETER) MultipartFile[] inputSourceArtifacts,

            @Parameter(description = "The artifact type of the source files", required = true) @RequestParam(SOURCE_ARTIFACT_TYPE_PARAMETER) ArtifactType sourceArtifactType,

            @Parameter(description = "One or more target artifact files (e.g. source code)", required = true, array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))) @RequestPart(TARGET_ARTIFACTS_PARAMETER) MultipartFile[] inputTargetArtifacts,

            @Parameter(description = "The artifact type of the target files", required = true) @RequestParam(TARGET_ARTIFACT_TYPE_PARAMETER) ArtifactType targetArtifactType,

            @Parameter(description = "JSON string containing additional LiSSA configuration. If not provided, the default is used.", required = false, schema = @Schema(type = "object")) @RequestPart(name = ADDITIONAL_CONFIGURATION_PARAMETER, required = false) String additionalConfigsJson,

            @Parameter(description = "JSON object with the caller's own API keys, mapping environment variable names to values (e.g. {\"OPENAI_API_KEY\": \"sk-...\"}).", required = false, schema = @Schema(type = "object")) @RequestPart(name = API_KEYS_PARAMETER, required = false) String apiKeys) {
        logger.info("Received request to start LiSSA processing pipeline for project: {}", projectName);

        PipelineStart start = startPipeline(projectName, inputSourceArtifacts, sourceArtifactType, inputTargetArtifacts, targetArtifactType,
                additionalConfigsJson, apiKeys);
        String id = start.id();
        Optional<ArDoCoApiResult> result = start.result();

        return buildResponse(id, result, ResultMessages.RESULT_IS_BEING_PROCESSED, HttpStatus.OK);
    }

    @Operation(summary = "Starts the LiSSA processing pipeline and waits until the result is obtained")
    @PostMapping(value = "/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam(PROJECT_NAME_PARAMETER) String projectName,

            @Parameter(description = "One or more source artifact files (e.g. requirements/documentation)", required = true, array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))) @RequestPart(SOURCE_ARTIFACTS_PARAMETER) MultipartFile[] inputSourceArtifacts,

            @Parameter(description = "The artifact type of the source files", required = true) @RequestParam(SOURCE_ARTIFACT_TYPE_PARAMETER) ArtifactType sourceArtifactType,

            @Parameter(description = "One or more target artifact files (e.g. source code)", required = true, array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))) @RequestPart(TARGET_ARTIFACTS_PARAMETER) MultipartFile[] inputTargetArtifacts,

            @Parameter(description = "The artifact type of the target files", required = true) @RequestParam(TARGET_ARTIFACT_TYPE_PARAMETER) ArtifactType targetArtifactType,

            @Parameter(description = "JSON string containing additional LiSSA configuration. If not provided, the default is used.", required = false, schema = @Schema(type = "object")) @RequestPart(name = ADDITIONAL_CONFIGURATION_PARAMETER, required = false) String additionalConfigsJson,

            @Parameter(description = "JSON object with the caller's own API keys, mapping environment variable names to values (e.g. {\"OPENAI_API_KEY\": \"sk-...\"}).", required = false, schema = @Schema(type = "object")) @RequestPart(name = API_KEYS_PARAMETER, required = false) String apiKeys) {
        PipelineStart start = startPipeline(projectName, inputSourceArtifacts, sourceArtifactType, inputTargetArtifacts, targetArtifactType,
                additionalConfigsJson, apiKeys);
        String id = start.id();
        Optional<ArDoCoApiResult> result = start.result();

        if (result.isEmpty()) {
            result = resultService.waitForResult(id);
        }
        return buildResponse(id, result, ResultMessages.REQUEST_TIMED_OUT_START_AND_WAIT, HttpStatus.ACCEPTED);
    }

    private ResponseEntity<ArdocoResultResponse> buildResponse(String id, Optional<ArDoCoApiResult> result, String pendingMessage, HttpStatus pendingStatus) {
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(id, pendingStatus, pendingMessage);
        } else {
            response = new ArdocoResultResponse(id, HttpStatus.OK, result.get(), ResultMessages.RESULT_IS_READY);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }

    private record PipelineStart(String id, Optional<ArDoCoApiResult> result) {
    }

    /**
     * Stores the uploads in a fresh directory owned by this request (so concurrent requests with equally named files
     * cannot interfere) and hands it to the service, which deletes it once it is no longer needed.
     */
    private PipelineStart startPipeline(String projectName, MultipartFile[] inputSourceArtifacts, ArtifactType sourceArtifactType,
            MultipartFile[] inputTargetArtifacts, ArtifactType targetArtifactType, @Nullable String additionalConfigsJson, @Nullable String apiKeys) {
        Path uploadDir;
        try {
            uploadDir = Files.createTempDirectory("lissa-upload-");
        } catch (IOException e) {
            throw new FileConversionException("Failed to create upload directory: " + e.getMessage(), e);
        }

        List<File> sourceFiles;
        List<File> targetFiles;
        try {
            sourceFiles = convertToFiles(inputSourceArtifacts, uploadDir.resolve("source"));
            targetFiles = convertToFiles(inputTargetArtifacts, uploadDir.resolve("target"));
        } catch (RuntimeException e) {
            LissaService.deleteQuietly(uploadDir);
            throw e;
        }
        List<File> inputFiles = new ArrayList<>(sourceFiles);
        inputFiles.addAll(targetFiles);

        String id = generateRequestId(inputFiles, projectName, additionalConfigsJson);
        Optional<ArDoCoApiResult> result = service.runPipeline(sourceFiles, sourceArtifactType, targetFiles, targetArtifactType, uploadDir, id,
                additionalConfigsJson, apiKeys);
        return new PipelineStart(id, result);
    }

    private List<File> convertToFiles(MultipartFile[] multipartFiles, Path directory) {
        logger.debug("Converting {} multipart files to files...", multipartFiles.length);
        List<File> files = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            files.add(FileConverter.convertMultipartFileToFile(multipartFile));
        }
        return files;
    }

    private String generateRequestId(List<File> files, String projectName, @Nullable String additionalConfigsJson) {
        String hash = HashGenerator.getMD5HashFromFiles(files);
        if (additionalConfigsJson != null) {
            hash += HashGenerator.getMD5HashFromString(additionalConfigsJson);
        }
        return TraceLinkType.LISSA.getKeyPrefix() + "-" + projectName + hash;
    }
}
