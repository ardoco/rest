package io.github.ardoco.rest.api.service;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
// import io.github.ardoco.rest.api.entity.ArDoCoResultEntity;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import io.github.ardoco.rest.api.exception.ResultNotFoundException;
// import io.github.ardoco.rest.api.repository.ArDoCoResultEntityRepository;
import io.github.ardoco.rest.api.util.TraceLinkConverter;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ArDoCoForSadCodeTLRService extends RunnerTLRService {

    private static final String STRING_KEY_PREFIX = "SadCodeResult:";

    // Map to track the progress of async tasks
    private final ConcurrentHashMap<String, CompletableFuture<Void>> asyncTasks = new ConcurrentHashMap<>();

//    public ArDoCoForSadCodeTLRService(ArDoCoResultEntityRepository repository) {
//        super(repository);
//    }

    public ArDoCoForSadCodeTLRService() {}

    @Override
    public String runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception {
        File inputTextFile = convertMultipartFileToFile(inputText);
        File inputCodeFile = convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir(); // temporary directory to store the ardoco result in

        //TODO: Check if file type is correct

        // TODO: generate hash value (right no use mock random uuid until hash is integrated)
        String uid = "hash_value:" + UUID.randomUUID().toString();

        // ArDoCoResultEntity resultEntity = new ArDoCoResultEntity();
        // String uid = saveResult(resultEntity);

        // Start asynchronous processing and store the future in the map
        CompletableFuture<Void> future = runPipelineAsync(uid, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs);
        asyncTasks.put(uid, future);

        return uid;
    }

    @Override
    public Optional<String> getResult(String id) {
        CompletableFuture<Void> future = asyncTasks.get(id);
        if (future != null && !future.isDone()) {
            // Return empty Optional if the result is still being processed
            return Optional.empty();
        }

        // Query Redis for the result
        String result = template.opsForValue().get(id);

        // Return an empty Optional if value is null
        return Optional.ofNullable(result);
    }


    @Async
    public CompletableFuture<Void> runPipelineAsync(String uid, String projectName, File inputTextFile, File inputCodeFile, File outputDir, SortedMap<String, String> additionalConfigs) throws Exception {

        // Run the pipeline
        ArDoCoForSadCodeTraceabilityLinkRecovery runner = new ArDoCoForSadCodeTraceabilityLinkRecovery(projectName);
        runner.setUp(inputTextFile, inputCodeFile, additionalConfigs, outputDir);
        ArDoCoResult result = runner.run();

        // Convert and store result as JSON
        var traceLinks = result.getSadCodeTraceLinks();
        TraceLinkConverter converter = new TraceLinkConverter();
        String traceLinkJson = converter.convertListOfTraceLinksToJSONString(traceLinks);
        saveResult(uid, traceLinkJson);
//        ArDoCoResultEntity resultEntity = arDoCoResultRepository.findById(uid).orElseThrow(() -> new ResultNotFoundException(uid));
//        resultEntity.setSadCodeTraceLinksJson(traceLinkJson);
//        arDoCoResultRepository.save(resultEntity);

        // Remove the completed future from the map
        asyncTasks.remove(uid);

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
