package io.github.ardoco.rest.api.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;

public interface RunnerTLRService {

    String runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception;

    Optional<String> getResult(String id);

    String waitForResult(String id) throws ExecutionException, InterruptedException;

    String runPipelineAndWaitForResult(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception;

}