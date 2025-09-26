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

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;

public class ArDoCoForSadSamTLRControllerTest extends AbstractTLRControllerTest {

    public ArDoCoForSadSamTLRControllerTest() {
        super(TraceLinkType.SAD_SAM);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void TestRunPipelineAndGetResult_umlModel() throws IOException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB("bigBlueButtonUML", ModelFormat.UML);
        runPipeline_start_and_getResult(requestEntity);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void TestRunPipelineAndGetResult_pcmModel() throws IOException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB("bigBlueButtonPCM", ModelFormat.PCM);
        runPipeline_start_and_getResult(requestEntity);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void TestRunPipelineAndWaitForResult_pcmModel() throws IOException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB("bigBlueButton2", ModelFormat.PCM);
        test_runPipelineAndWaitForResult_helper(requestEntity);
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void TestRunPipelineAndWaitForResult_umlModel() throws IOException {
        HttpEntity<MultiValueMap<String, Object>> requestEntity = setUpRequestParamToStartPipelineBBB("bigBlueButton2", ModelFormat.UML);
        test_runPipelineAndWaitForResult_helper(requestEntity);
    }

    @Override
    protected HttpEntity<MultiValueMap<String, Object>> prepareRequestEntityForEmptyFileTest(String projectName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add(PROJECT_NAME_PARAMETER, projectName);
        parameters.add(ARCHITECTURE_MODEL_PARAMETER, new ClassPathResource("bigBlueButton/bbb.repository"));
        parameters.add(TEXTUAL_DOCUMENTATION_PARAMETER, new ClassPathResource("emptyFile.txt"));
        parameters.add(ARCHITECTURE_MODEL_FORMAT_PARAMETER, ModelFormat.PCM.toString());

        return new HttpEntity<>(parameters, headers);
    }

    // Utility method to build request entity for multipart files
    private HttpEntity<MultiValueMap<String, Object>> setUpRequestParamToStartPipelineBBB(String projectName, ModelFormat modelType) {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();

        parameters.add(PROJECT_NAME_PARAMETER, projectName);
        parameters.add(TEXTUAL_DOCUMENTATION_PARAMETER, new ClassPathResource("bigBlueButton/bigbluebutton.txt"));
        parameters.add(ARCHITECTURE_MODEL_FORMAT_PARAMETER, modelType.toString());
        if (modelType == ModelFormat.PCM) {
            parameters.add(ARCHITECTURE_MODEL_PARAMETER, new ClassPathResource("bigBlueButton/bbb.repository"));
        } else if (modelType == ModelFormat.UML) {
            parameters.add(ARCHITECTURE_MODEL_PARAMETER, new ClassPathResource("bigBlueButton/bbb.uml"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(parameters, headers);
    }
}
