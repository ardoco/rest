package io.github.ardoco.rest.api.service;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service("sadCodeTLRService")
public class ArDoCoForSadCodeTLRService implements RunnerTLRService {

    private static final String STRING_KEY_PREFIX = "SadCodeResult:";

    /** Map to track the progress of async tasks */
    private final ConcurrentHashMap<String, CompletableFuture<Void>> asyncTasks = new ConcurrentHashMap<>();

    private static final Logger logger = LogManager.getLogger(ArDoCoForSadCodeTLRService.class);

    private final DatabaseAccessor databaseAccessor;

    public ArDoCoForSadCodeTLRService(DatabaseAccessor databaseAccessor) {
        this.databaseAccessor = databaseAccessor;
    }

    @Override
    public String runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception {
        File inputTextFile = FileConverter.convertMultipartFileToFile(inputText);
        File inputCodeFile = FileConverter.convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir();
        String id = generateHashFromFiles(List.of(inputCodeFile, inputTextFile));

        if(databaseAccessor.keyExistsInDatabase(id)) {
            CompletableFuture<Void> future = runPipelineAsync(id, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs);
            asyncTasks.put(id, future);
            future.whenComplete((result, ex) -> asyncTasks.remove(id));
        }
        return id;
    }

    @Override
    public Optional<String> getResult(String id) {
        CompletableFuture<Void> future = asyncTasks.get(id);
        if (future != null && !future.isDone()) {
            return Optional.empty();
        }

        String result = databaseAccessor.getResult(id);
        return Optional.ofNullable(result);
    }

    @Override
    public String waitForResult(String id) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = asyncTasks.get(id);
        if (future != null && !future.isDone()) {
            future.get();
        }
        return databaseAccessor.getResult(id);
    }

    @Override
    public String runPipelineAndWaitForResult(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception {
        File inputTextFile = FileConverter.convertMultipartFileToFile(inputText);
        File inputCodeFile = FileConverter.convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir();
        String id = generateHashFromFiles(List.of(inputCodeFile, inputTextFile));

        if (databaseAccessor.keyExistsInDatabase(id)) {
            CompletableFuture<Void> future = runPipelineAsync(id, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs);
            asyncTasks.put(id, future);
            future.get();
            asyncTasks.remove(id);
        }
        return databaseAccessor.getResult(id);
    }

    private String generateHashFromFiles(List<File> files) throws NoSuchAlgorithmException, IOException {
        HashGenerator hashGenerator = new HashGenerator();
        String hash = hashGenerator.getMD5HashFromFiles(files);
        return STRING_KEY_PREFIX + hash;
    }


    @Async
    protected CompletableFuture<Void> runPipelineAsync(String id, String projectName, File inputTextFile, File inputCodeFile, File outputDir, SortedMap<String, String> additionalConfigs) {
        try {
            // Run the pipeline
            ArDoCoForSadCodeTraceabilityLinkRecovery runner = new ArDoCoForSadCodeTraceabilityLinkRecovery(projectName);
            runner.setUp(inputTextFile, inputCodeFile, additionalConfigs, outputDir);
            ArDoCoResult result = runner.run();

            // Convert and store result as JSON
            var traceLinks = result.getSadCodeTraceLinks();
            TraceLinkConverter converter = new TraceLinkConverter();
            String traceLinkJson = converter.convertListOfTraceLinksToJSONString(traceLinks);
            databaseAccessor.saveResult(id, traceLinkJson);

        } catch (Exception e) {
            String message = "Error occurred while running the pipeline asynchronously for ID: " + id;
            logger.error(message, e);
            return CompletableFuture.failedFuture(e);
        }
        return CompletableFuture.completedFuture(null);
    }

}
