package io.github.ardoco.rest.api.service;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import io.github.ardoco.rest.api.api_response.ResultBag;
import io.github.ardoco.rest.api.exception.ArdocoException;
import io.github.ardoco.rest.api.exception.FileConversionException;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import io.github.ardoco.rest.api.exception.HashingException;
import io.github.ardoco.rest.api.repository.DatabaseAccessor;
import io.github.ardoco.rest.api.util.FileConverter;
import io.github.ardoco.rest.api.util.HashGenerator;
import io.github.ardoco.rest.api.util.Messages;
import io.github.ardoco.rest.api.util.TraceLinkConverter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.*;

@Service("sadCodeTLRService")
public class ArDoCoForSadCodeTLRService implements RunnerTLRService {

    private static final String STRING_KEY_PREFIX = "SadCodeResult:";
    private static final String ERROR_PREFIX = "Error: ";

    /** Map to track the progress of async tasks*/
    private final ConcurrentHashMap<String, CompletableFuture<String>> asyncTasks = new ConcurrentHashMap<>();
    /**
     * Storing the CompletableFuture as well, makes it harder to maintain the Concurrent hashmap, so that
     * all ids for which the ardocoResult is running are actually stored in the list
     */

    private static final Logger logger = LogManager.getLogger(ArDoCoForSadCodeTLRService.class);

    private final DatabaseAccessor databaseAccessor;

    public ArDoCoForSadCodeTLRService(DatabaseAccessor databaseAccessor) {
        this.databaseAccessor = databaseAccessor;
    }

    @Override
    public String runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs)
            throws FileNotFoundException, FileConversionException, HashingException {
        File inputTextFile = FileConverter.convertMultipartFileToFile(inputText);
        File inputCodeFile = FileConverter.convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir();
        String id = generateHashFromFiles(List.of(inputCodeFile, inputTextFile), projectName);

        if (!databaseAccessor.keyExistsInDatabase(id)) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return runPipelineAsync(id, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs);
                } catch (Exception e) {
                    logger.error("Error running pipeline asynchronously: " + e.getMessage());
                    return null;
                }
            });
            asyncTasks.put(id, future);
        }
        return id;
    }

    @Override
    public Optional<String> getResult(String id) throws ArdocoException, IllegalArgumentException {
        if (asyncTasks.containsKey(id)) {
            return Optional.empty();
        }
        if (!databaseAccessor.keyExistsInDatabase(id)) {
            throw new IllegalArgumentException(Messages.noResultForKey(id));
        }

        String result = databaseAccessor.getResult(id);
        if (result == null || result.startsWith(ERROR_PREFIX)) {
            throw new ArdocoException(result);
        }
        return Optional.of(result);
    }

    @Override
    public String waitForResult(String id) throws ArdocoException, InterruptedException, IllegalArgumentException {
        if (asyncTasks.containsKey(id)) {
            try {
                asyncTasks.get(id).get(60, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new io.github.ardoco.rest.api.exception.TimeoutException(id);
            } catch (ExecutionException e) {
                throw new ArdocoException(e.getMessage());
            }
        }

        if (!databaseAccessor.keyExistsInDatabase(id)) {
            throw new IllegalArgumentException(Messages.noResultForKey(id));
        }

        String result = databaseAccessor.getResult(id);
        if (result == null || result.startsWith(ERROR_PREFIX)) {
            throw new ArdocoException(result);
        }

        return result;
    }

    @Override
    public ResultBag runPipelineAndWaitForResult(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs)
            throws FileNotFoundException, FileConversionException, HashingException, ArdocoException {
        File inputTextFile = FileConverter.convertMultipartFileToFile(inputText);
        File inputCodeFile = FileConverter.convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir();
        String id = generateHashFromFiles(List.of(inputCodeFile, inputTextFile), projectName);

        if (!databaseAccessor.keyExistsInDatabase(id)) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                    runPipelineAsync(id, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs)
            );
            asyncTasks.put(id, future);

            try {
                return new ResultBag(id, future.get(60, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error while executing async pipeline for ID {}: {}", id, e.getMessage());
                throw new ArdocoException(e.getCause().getMessage());
            } catch (TimeoutException e) {
                throw new io.github.ardoco.rest.api.exception.TimeoutException(id);
            }
        }

        return new ResultBag(id, databaseAccessor.getResult(id));
    }

    private String generateHashFromFiles(List<File> files, String projectName)
            throws HashingException, FileNotFoundException, FileConversionException {
        logger.log(Level.DEBUG, "Generating ID...");
        HashGenerator hashGenerator = new HashGenerator();
        String hash = hashGenerator.getMD5HashFromFiles(files);
        return STRING_KEY_PREFIX + projectName + hash;
    }

    private String runPipelineAsync(String id, String projectName, File inputTextFile, File inputCodeFile, File outputDir, SortedMap<String, String> additionalConfigs) {
        String traceLinkJson;
        try {
            // Run the pipeline
            logger.log(Level.INFO, "Starting Pipeline...");
            ArDoCoForSadCodeTraceabilityLinkRecovery runner = new ArDoCoForSadCodeTraceabilityLinkRecovery(projectName);
            runner.setUp(inputTextFile, inputCodeFile, additionalConfigs, outputDir);
            ArDoCoResult result = runner.run();

            // Convert and store result as JSON
            logger.log(Level.DEBUG, "Converting found TraceLinks...");
            List<SadCodeTraceLink> traceLinks = result.getSadCodeTraceLinks();
            TraceLinkConverter converter = new TraceLinkConverter();
            traceLinkJson = converter.convertListOfTraceLinksToJSONString(traceLinks);

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
            inputTextFile.delete();
        }
        return traceLinkJson;
    }

}
