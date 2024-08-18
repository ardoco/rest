package io.github.ardoco.rest.api.service;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import io.github.ardoco.rest.api.entity.ArDoCoResultEntity;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import io.github.ardoco.rest.api.exception.ResultNotFoundException;
import io.github.ardoco.rest.api.repository.ArDoCoResultEntityRepository;
import io.github.ardoco.rest.api.util.TraceLinkConverter;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ArDoCoForSadCodeTLRService extends RunnerTLRService {

    // Map to track the progress of async tasks
    private final ConcurrentHashMap<Long, CompletableFuture<Void>> asyncTasks = new ConcurrentHashMap<>();

    public ArDoCoForSadCodeTLRService(ArDoCoResultEntityRepository repository) {
        super(repository);
    }

    @Override
    public long runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception {
        File inputTextFile = convertMultipartFileToFile(inputText);
        File inputCodeFile = convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir(); // temporary directory to store the ardoco result in

        //TODO: Check if file type is correct

        ArDoCoResultEntity resultEntity = new ArDoCoResultEntity();
        Long uid = saveResult(resultEntity);

        // Start asynchronous processing and store the future in the map
        CompletableFuture<Void> future = runPipelineAsync(uid, projectName, inputTextFile, inputCodeFile, outputDir, additionalConfigs);
        asyncTasks.put(uid, future);

        return uid;
    }

    @Override
    public String getResult(long id) throws ResultNotFoundException {
        CompletableFuture<Void> future = asyncTasks.get(id);
        if (future != null && !future.isDone()) {
            return "Result is still being processed. Please try again later.";
        }

        ArDoCoResultEntity resultEntity = arDoCoResultRepository.findById(id).orElseThrow(() -> new ResultNotFoundException(id));
        if (resultEntity.getSadCodeTraceLinksJson() == null) {
            return "Result is still being processed. Please try again later.";
        } else {
            return resultEntity.getSadCodeTraceLinksJson();
        }
    }

    @Async
    public CompletableFuture<Void> runPipelineAsync(Long uid, String projectName, File inputTextFile, File inputCodeFile, File outputDir, SortedMap<String, String> additionalConfigs) throws Exception {

        // Run the pipeline
        ArDoCoForSadCodeTraceabilityLinkRecovery runner = new ArDoCoForSadCodeTraceabilityLinkRecovery(projectName);
        runner.setUp(inputTextFile, inputCodeFile, additionalConfigs, outputDir);
        ArDoCoResult result = runner.run();

        // Convert and store result as JSON
        var traceLinks = result.getSadCodeTraceLinks();
        TraceLinkConverter converter = new TraceLinkConverter();
        String traceLinkJson = converter.convertListOfTraceLinksToJSONString(traceLinks);
        ArDoCoResultEntity resultEntity = arDoCoResultRepository.findById(uid).orElseThrow(() -> new ResultNotFoundException(uid));
        resultEntity.setSadCodeTraceLinksJson(traceLinkJson);
        arDoCoResultRepository.save(resultEntity);

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
