package io.github.ardoco.rest.api.service;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import io.github.ardoco.rest.api.exception.ArdocoException;
import io.github.ardoco.rest.api.exception.FileConversionException;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import io.github.ardoco.rest.api.exception.HashingException;
import io.github.ardoco.rest.api.repository.DatabaseAccessor;
import io.github.ardoco.rest.api.util.FileConverter;
import io.github.ardoco.rest.api.util.HashGenerator;
import io.github.ardoco.rest.api.util.TraceLinkConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service("sadCodeTLRService")
public class ArDoCoForSadCodeTLRService implements RunnerTLRService {

    private static final String STRING_KEY_PREFIX = "SadCodeResult:";
    private static final String ERROR_PREFIX = "Error: ";

    /** Map to track the progress of async tasks (thread safe)*/
    // private final ConcurrentHashMap<String, Boolean> asyncTasks = new ConcurrentHashMap<>();
    private final Set<String> asyncTasks = ConcurrentHashMap.newKeySet();
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
        String id = generateHashFromFiles(List.of(inputCodeFile, inputTextFile));

        if (!databaseAccessor.keyExistsInDatabase(id)) {
            asyncTasks.add(id);
            CompletableFuture.runAsync(() -> {
                runPipelineAsync(id, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs);
            });
        }
        return id;
    }

    @Override
    public Optional<String> getResult(String id) throws ArdocoException, IllegalArgumentException {
        if (asyncTasks.contains(id)) {
            return Optional.empty();
        }
        if (!databaseAccessor.keyExistsInDatabase(id)) {
            throw new IllegalArgumentException("No result with key " + id + " found in database." );
        }

        String result = databaseAccessor.getResult(id);
        if (result == null || result.startsWith(ERROR_PREFIX)) {
            throw new ArdocoException(result);
        }
        return Optional.of(result);
    }

    @Override
    public String waitForResult(String id) throws ArdocoException, InterruptedException, IllegalArgumentException {
        while (asyncTasks.contains(id)) {
            // wait because result is not ready yet
            TimeUnit.SECONDS.sleep(1);
        }

        if (!databaseAccessor.keyExistsInDatabase(id)) {
            throw new IllegalArgumentException("No result with key " + id + " found in database." );
        }

        String result = databaseAccessor.getResult(id);
        if (result == null || result.startsWith(ERROR_PREFIX)) {
            throw new ArdocoException(result);
        }

        return databaseAccessor.getResult(id);
    }

    @Override
    public String runPipelineAndWaitForResult(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs)
            throws FileNotFoundException, FileConversionException, HashingException, ArdocoException {
        File inputTextFile = FileConverter.convertMultipartFileToFile(inputText);
        File inputCodeFile = FileConverter.convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir();
        String id = generateHashFromFiles(List.of(inputCodeFile, inputTextFile));

        if (!databaseAccessor.keyExistsInDatabase(id)) {
            asyncTasks.add(id);
            CompletableFuture<String> future = runPipelineAsync(id, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs);

            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error while executing async pipeline for ID {}: {}", id, e.getMessage());
                // Re-throw the cause of ExecutionException as the original exception
                throw e.getCause() instanceof ArdocoException ? (ArdocoException) e.getCause() : new ArdocoException(e.getCause().getMessage());
            }
        }

        return databaseAccessor.getResult(id);
    }

    private String generateHashFromFiles(List<File> files)
            throws HashingException, FileNotFoundException, FileConversionException {
        HashGenerator hashGenerator = new HashGenerator();
        String hash = hashGenerator.getMD5HashFromFiles(files);
        return STRING_KEY_PREFIX + hash;
    }


    @Async
    protected CompletableFuture<String> runPipelineAsync(String id, String projectName, File inputTextFile, File inputCodeFile, File outputDir, SortedMap<String, String> additionalConfigs) {
        String traceLinkJson;
        try {
            // Run the pipeline
            ArDoCoForSadCodeTraceabilityLinkRecovery runner = new ArDoCoForSadCodeTraceabilityLinkRecovery(projectName);
            runner.setUp(inputTextFile, inputCodeFile, additionalConfigs, outputDir);
            ArDoCoResult result = runner.run();

            // Convert and store result as JSON
            List<SadCodeTraceLink> traceLinks = result.getSadCodeTraceLinks();
            TraceLinkConverter converter = new TraceLinkConverter();
            traceLinkJson = converter.convertListOfTraceLinksToJSONString(traceLinks);
            databaseAccessor.saveResult(id, traceLinkJson);

        } catch (Exception e) {
            String message = "Error occurred while running the pipeline asynchronously for ID " + id + ": " + e.getMessage();
            logger.error(message, e);
            databaseAccessor.saveResult(id, ERROR_PREFIX + message);
            return CompletableFuture.failedFuture(new ArdocoException(message));
//            throw new ArdocoException(message);
        } finally {
            asyncTasks.remove(id);
        }

        return CompletableFuture.completedFuture(traceLinkJson);
    }

}
