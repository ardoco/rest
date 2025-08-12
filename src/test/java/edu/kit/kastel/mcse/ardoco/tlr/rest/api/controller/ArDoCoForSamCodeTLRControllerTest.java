/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.controller;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.junit.jupiter.Testcontainers;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.tlr.rest.ArDoCoRestApplication;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;

@Testcontainers
@SpringBootTest(classes = ArDoCoRestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArDoCoForSamCodeTLRControllerTest extends AbstractTLRControllerTest {

    public ArDoCoForSamCodeTLRControllerTest() {
        super(TraceLinkType.SAM_CODE);
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
        parameters.add("projectName", projectName);
        parameters.add("inputArchitectureModel", new ClassPathResource("emptyFile.txt"));
        parameters.add("inputCode", new ClassPathResource("bigBlueButton/codeModel.acm"));
        parameters.add("ModelFormat", ModelFormat.PCM.toString());

        return new HttpEntity<>(parameters, headers);
    }

    // Utility method to build request entity for multipart files
    private HttpEntity<MultiValueMap<String, Object>> setUpRequestParamToStartPipelineBBB(String projectName, ModelFormat modelType) {
        LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("projectName", projectName);

        parameters.add("inputCode", new ClassPathResource("bigBlueButton/codeModel.acm"));
        parameters.add("ModelFormat", modelType.toString());

        if (modelType == ModelFormat.PCM) {
            parameters.add("inputArchitectureModel", new ClassPathResource("bigBlueButton/bbb.repository"));
        } else if (modelType == ModelFormat.UML) {
            parameters.add("inputArchitectureModel", new ClassPathResource("bigBlueButton/bbb.uml"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(parameters, headers);
    }
}
