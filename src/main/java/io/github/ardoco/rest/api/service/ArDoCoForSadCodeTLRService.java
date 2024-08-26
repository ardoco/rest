package io.github.ardoco.rest.api.service;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
// import io.github.ardoco.rest.api.entity.ArDoCoResultEntity;
// import io.github.ardoco.rest.api.controller.ArDoCoForSadCodeTLRController;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
// import io.github.ardoco.rest.api.exception.ResultNotFoundException;
// import io.github.ardoco.rest.api.repository.ArDoCoResultEntityRepository;
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

@Service
public class ArDoCoForSadCodeTLRService extends RunnerTLRService {

    private static final String STRING_KEY_PREFIX = "SadCodeResult:";

    // Map to track the progress of async tasks
    private final ConcurrentHashMap<String, CompletableFuture<Void>> asyncTasks = new ConcurrentHashMap<>();

    private static final Logger logger = LogManager.getLogger(ArDoCoForSadCodeTLRService.class);

//    public ArDoCoForSadCodeTLRService(ArDoCoResultEntityRepository repository) {
//        super(repository);
//    }

    public ArDoCoForSadCodeTLRService() {}

    @Override
    public String runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception {
        File inputTextFile = convertMultipartFileToFile(inputText);
        File inputCodeFile = convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir(); // temporary directory to store the ardoco result in
        String id = generateHashFromFiles(List.of(inputCodeFile, inputTextFile));

        // ArDoCoResultEntity resultEntity = new ArDoCoResultEntity();
        // String id = saveResult(resultEntity);

        //only start async processing to query ardoco if result doesn't exist in database yet.
        if(Boolean.FALSE.equals(template.hasKey(id))) {
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

        // Query Redis for the result
        String result = template.opsForValue().get(id);
        return Optional.ofNullable(result);
    }

    @Override
    public String waitForResult(String id) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = asyncTasks.get(id);
        if (future != null && !future.isDone()) {
            future.get();
        }
        return template.opsForValue().get(id);
    }

    @Override
    public String runPipelineAndWaitForResult(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception {
        File inputTextFile = convertMultipartFileToFile(inputText);
        File inputCodeFile = convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir();
        String id = generateHashFromFiles(List.of(inputCodeFile, inputTextFile));

        //only start async processing to query ardoco if result doesn't exist in database yet.
        if (Boolean.FALSE.equals(template.hasKey(id))) {
            CompletableFuture<Void> future = runPipelineAsync(id, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs);
            asyncTasks.put(id, future);
            future.get();
            asyncTasks.remove(id);
        }
        return template.opsForValue().get(id);
    }

    private String generateHashFromFiles(List<File> files) throws NoSuchAlgorithmException, IOException {
        HashGenerator hashGenerator = new HashGenerator();
        String hash = hashGenerator.getHashFromFiles(files);
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
            saveResult(id, traceLinkJson);
            //        ArDoCoResultEntity resultEntity = arDoCoResultRepository.findById(id).orElseThrow(() -> new ResultNotFoundException(id));
            //        resultEntity.setSadCodeTraceLinksJson(traceLinkJson);
            //        arDoCoResultRepository.save(resultEntity);
        } catch (Exception e) {
            String message = "Error occurred while running the pipeline asynchronously for ID: " + id;
            logger.error(message, e);
            return CompletableFuture.failedFuture(e);
        }

//        // Remove the completed future from the map
//        asyncTasks.remove(id);

        return CompletableFuture.completedFuture(null);
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws Exception {
        if (multipartFile.isEmpty()) {
            throw new FileNotFoundException();
        }
        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(file);
        } catch (IOException | IllegalStateException e) {
            throw new IOException("Error occurred while transferring the MultipartFile to File.", e);
        }
        return file;
    }

}
