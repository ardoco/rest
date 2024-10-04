package io.github.ardoco.rest.api.controller;

import io.github.ardoco.rest.api.api_response.TraceLinkType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ArDoCoForSadCodeControllerTest extends AbstractControllerTest {

    public ArDoCoForSadCodeControllerTest() {
        super(TraceLinkType.SAD_CODE);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void TestRunPipelineAndGetResult() throws IOException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB("bigBlueButton");
        runPipeline_start_and_getResult(requestEntity);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void TestRunPipelineAndWaitForResult() throws IOException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB("bigBlueButton2");
        test_runPipelineAndWaitForResult_helper(requestEntity);
    }

    @Override
    protected HttpEntity<MultiValueMap<String, Object>> prepareRequestEntityForEmptyFileTest(String projectName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("projectName", projectName);
        parameters.add("inputText", new ClassPathResource("emptyFile.txt"));
        parameters.add("inputCode", new ClassPathResource("bigBlueButton/codeModel.acm"));

        return new HttpEntity<>(parameters, headers);
    }

    // Utility method to build request entity for multipart files
    private HttpEntity<MultiValueMap<String, Object>> setUpRequestParamToStartPipelineBBB(String projectName) {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();

        parameters.add("projectName", projectName);
        parameters.add("inputText", new ClassPathResource("bigBlueButton/bigbluebutton.txt"));
        parameters.add("inputCode", new ClassPathResource("bigBlueButton/codeModel.acm"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(parameters, headers);
    }
}
