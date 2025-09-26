/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import static edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller.AbstractController.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;

public class ArDoCoForSadCodeControllerTest extends AbstractTLRControllerTest {

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
        parameters.add(PROJECT_NAME_PARAMETER, projectName);
        parameters.add(TEXTUAL_DOCUMENTATION_PARAMETER, new ClassPathResource("emptyFile.txt"));
        parameters.add(CODE_PARAMETER, new ClassPathResource("bigBlueButton/codeModel.acm"));

        return new HttpEntity<>(parameters, headers);
    }

    // Utility method to build request entity for multipart files
    private HttpEntity<MultiValueMap<String, Object>> setUpRequestParamToStartPipelineBBB(String projectName) {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();

        parameters.add(PROJECT_NAME_PARAMETER, projectName);
        parameters.add(TEXTUAL_DOCUMENTATION_PARAMETER, new ClassPathResource("bigBlueButton/bigbluebutton.txt"));
        parameters.add(CODE_PARAMETER, new ClassPathResource("bigBlueButton/codeModel.acm"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(parameters, headers);
    }
}
