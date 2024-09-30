package io.github.ardoco.rest.api.service;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSamCodeTraceabilityLinkRecovery;
import io.github.ardoco.rest.api.api_response.ResultBag;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.exception.ArdocoException;
import io.github.ardoco.rest.api.exception.FileConversionException;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import io.github.ardoco.rest.api.exception.HashingException;
import io.github.ardoco.rest.api.repository.DatabaseAccessor;
import io.github.ardoco.rest.api.util.FileConverter;
import io.github.ardoco.rest.api.util.TraceLinkConverter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;

@RestController
public class ArDoCoForSamCodeTLRService extends AbstractRunnerTLRService {

    private static final Logger logger = LogManager.getLogger(ArDoCoForSamCodeTLRService.class);

    @Autowired
    public ArDoCoForSamCodeTLRService(DatabaseAccessor databaseAccessor) {
        super(databaseAccessor, TraceLinkType.SAM_CODE);
    }

    public ResultBag runPipeline(String projectName, MultipartFile inputCode, MultipartFile inputArchitectureModel, ArchitectureModelType architectureModelType, SortedMap<String, String> additionalConfigs)
            throws FileNotFoundException, FileConversionException, HashingException {
        File inputCodeFile = FileConverter.convertMultipartFileToFile(inputCode);
        File inputArchitectureModelFile = FileConverter.convertMultipartFileToFile(inputArchitectureModel);
        File outputDir = Files.createTempDir();
        String id = traceLinkType.getKeyPrefix() + generateHashFromFiles(List.of(inputArchitectureModelFile, inputCodeFile), projectName);

        if (!resultIsInDatabase(id) && !resultIsOnItsWay(id)) {
            logger.log(Level.INFO, "Start new SamCodeTLR for: " + id);
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return runPipelineAsync(id, projectName, architectureModelType, inputArchitectureModelFile, inputCodeFile, outputDir, additionalConfigs);
                } catch (Exception e) {
                    logger.error("Error running pipeline asynchronously: " + e.getMessage());
                    return null;
                }
            });
            asyncTasks.put(id, future);
        } else if (resultIsInDatabase(id)) {
            return new ResultBag(id, databaseAccessor.getResult(id));
        }

        return new ResultBag(id, null);
    }

    public ResultBag runPipelineAndWaitForResult(String projectName, MultipartFile inputCode, MultipartFile inputArchitectureModel, ArchitectureModelType architectureModelType, SortedMap<String, String> additionalConfigs)
            throws FileNotFoundException, FileConversionException, HashingException, ArdocoException, io.github.ardoco.rest.api.exception.TimeoutException {

        File inputCodeFile = FileConverter.convertMultipartFileToFile(inputCode);
        File inputArchitectureModelFile = FileConverter.convertMultipartFileToFile(inputArchitectureModel);
        File outputDir = Files.createTempDir();
        String id = traceLinkType.getKeyPrefix() + generateHashFromFiles(List.of(inputArchitectureModelFile, inputCodeFile), projectName);

        if (!resultIsInDatabase(id)) {
            if (!resultIsOnItsWay(id)) {
                logger.log(Level.INFO, "Start new samCodeTLR for: " + id);
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                        runPipelineAsync(id, projectName, architectureModelType, inputArchitectureModelFile, inputCodeFile, outputDir, additionalConfigs)
               );
                asyncTasks.put(id, future);
            }
            return new ResultBag(id, waitForResultHelper(id));

        }
        logger.log(Level.INFO, "Result is available for " + id);
        return new ResultBag(id, databaseAccessor.getResult(id));
    }

    private String runPipelineAsync(String id, String projectName, ArchitectureModelType architectureModelType, File inputArchitectureModel, File inputCodeFile, File outputDir, SortedMap<String, String> additionalConfigs) {
        String traceLinkJson;
        try {
            // Run the pipeline
            logger.log(Level.INFO, "Starting Pipeline...");
            ArDoCoForSamCodeTraceabilityLinkRecovery runner = new ArDoCoForSamCodeTraceabilityLinkRecovery(projectName);
            logger.log(Level.INFO, "Setting up Runner...");
            runner.setUp(inputArchitectureModel, architectureModelType, inputCodeFile, additionalConfigs, outputDir);
            logger.log(Level.INFO, "Finish setting up runner...");
            ArDoCoResult result = runner.run();

            // Convert and store result as JSON
            logger.log(Level.DEBUG, "Converting found TraceLinks...");
            List<SamCodeTraceLink> traceLinks = result.getSamCodeTraceLinks();
            TraceLinkConverter converter = new TraceLinkConverter();
            traceLinkJson = converter.convertListOfSamCodeTraceLinksToJSONString(traceLinks);

            logger.log(Level.INFO, "Saving found TraceLinks...");
            databaseAccessor.saveResult(id, traceLinkJson);

        } catch (Exception e) {
            String message = "Error occurred while running the pipeline asynchronously for ID " + id + ": " + e.getMessage();
            logger.error(message, e);
            databaseAccessor.saveResult(id, ERROR_PREFIX + message);
            return null;
        } finally {
            asyncTasks.remove(id);
            inputCodeFile.delete();
            inputArchitectureModel.delete();
        }
        return traceLinkJson;
    }
}
