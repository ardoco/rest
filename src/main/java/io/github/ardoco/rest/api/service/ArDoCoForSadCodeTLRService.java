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
    private static final int SECONDS_UNTIL_TIMEOUT = 60;

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
    public ResultBag runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs)
            throws FileNotFoundException, FileConversionException, HashingException {
        File inputTextFile = FileConverter.convertMultipartFileToFile(inputText);
        File inputCodeFile = FileConverter.convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir();
        String id = generateHashFromFiles(List.of(inputCodeFile, inputTextFile), projectName);

        if (!resultIsInDatabase(id) && !resultIsOnItsWay(id)) {
            logger.log(Level.INFO, "Start new samSadTLR for: " + id);
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return runPipelineAsync(id, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs);
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

    @Override
    public Optional<String> getResult(String id) throws ArdocoException, IllegalArgumentException {
        if (resultIsOnItsWay(id)) {
            logger.log(Level.DEBUG, "Result is not yet available for " + id);
            return Optional.empty();
        }

        return Optional.of(getResultFromDatabase(id));
    }

    @Override
    public String waitForResult(String id)
            throws ArdocoException, IllegalArgumentException, io.github.ardoco.rest.api.exception.TimeoutException {

        if (resultIsOnItsWay(id)) {
            return waitForResultHelper(id);
        }

        return getResultFromDatabase(id);
    }

    @Override
    public ResultBag runPipelineAndWaitForResult(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs)
            throws FileNotFoundException, FileConversionException, HashingException, ArdocoException, io.github.ardoco.rest.api.exception.TimeoutException {

        File inputTextFile = FileConverter.convertMultipartFileToFile(inputText);
        File inputCodeFile = FileConverter.convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir();
        String id = generateHashFromFiles(List.of(inputCodeFile, inputTextFile), projectName);

        if (!resultIsInDatabase(id)) {
            if (!resultIsOnItsWay(id)) {
                logger.log(Level.INFO, "Start new samSadTLR for: " + id);
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                        runPipelineAsync(id, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs)
                );
                asyncTasks.put(id, future);
            }
            return new ResultBag(id, waitForResultHelper(id));

        }
        logger.log(Level.INFO, "Result is available for " + id);
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

    private boolean resultIsOnItsWay(String id) {
        return asyncTasks.containsKey(id);
    }

    private boolean resultIsInDatabase(String id) {
        return databaseAccessor.keyExistsInDatabase(id);
    }

    private String getResultFromDatabase(String id) throws IllegalArgumentException, ArdocoException {
        if (!resultIsInDatabase(id)) {
            throw new IllegalArgumentException(Messages.noResultForKey(id));
        }

        String result = databaseAccessor.getResult(id);
        if (result == null || result.startsWith(ERROR_PREFIX)) {
            throw new ArdocoException(result);
        }
        logger.log(Level.INFO, "Result is available for " + id);
        return result;
    }

    private String waitForResultHelper(String id) {
        try {
            logger.log(Level.INFO, "Waiting for the result of " + id);
            return asyncTasks.get(id).get(SECONDS_UNTIL_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.log(Level.INFO, "Waiting for " + id + " took too long...");
            throw new io.github.ardoco.rest.api.exception.TimeoutException(id);
        } catch (ExecutionException | InterruptedException e) {
            throw new ArdocoException(e.getMessage());
        }
    }

}
