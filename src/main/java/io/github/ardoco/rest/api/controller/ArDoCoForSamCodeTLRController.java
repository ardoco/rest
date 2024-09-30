package io.github.ardoco.rest.api.controller;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import io.github.ardoco.rest.api.api_response.ResultBag;
import io.github.ardoco.rest.api.exception.*;
import io.github.ardoco.rest.api.service.ArDoCoForSamCodeTLRService;
import io.github.ardoco.rest.api.service.RunnerTLRService;
import io.github.ardoco.rest.api.util.Messages;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

@RestController
public class ArDoCoForSamCodeTLRController {

    private ArDoCoForSamCodeTLRService service;

    public ArDoCoForSamCodeTLRController(ArDoCoForSamCodeTLRService service) {
        this.service = service;
    }


    @PostMapping(value = "/api/sam-code/start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipeline(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true)@RequestParam("architectureModelType") ArchitectureModelType architectureModelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode)
            throws FileNotFoundException, FileConversionException, HashingException {

        SortedMap<String, String> additionalConfigs = new TreeMap<>(); // can be later added to api call as param if needed
        ResultBag result = service.runPipeline(projectName, inputCode, inputArchitectureModel, architectureModelType, additionalConfigs);
        ArdocoResultResponse response;
        if (result.traceLinks() != null) {
            response = new ArdocoResultResponse(result.projectId(), HttpStatus.OK, result.traceLinks(), Messages.RESULT_IS_READY);
        } else {
            response = new ArdocoResultResponse(result.projectId(), HttpStatus.OK, Messages.RESULT_IS_BEING_PROCESSED);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }

    @PostMapping(value = "/api/sam-code/start-and-wait", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArdocoResultResponse> runPipelineAndWaitForResult(
            @Parameter(description = "The name of the project", required = true) @RequestParam("projectName") String projectName,
            @Parameter(description = "The architectureModel of the project", required = true) @RequestParam("inputArchitectureModel") MultipartFile inputArchitectureModel,
            @Parameter(description = "The type of architectureModel that is uploaded.", required = true)@RequestParam("architectureModelType") ArchitectureModelType architectureModelType,
            @Parameter(description = "The code of the project", required = true) @RequestParam("inputCode") MultipartFile inputCode)
            throws FileNotFoundException, FileConversionException, HashingException {

        SortedMap<String, String> additionalConfigs = new TreeMap<>(); // can be later added to api call as param if needed
        ArdocoResultResponse response;
        try {
            ResultBag result = service.runPipelineAndWaitForResult(projectName, inputCode, inputArchitectureModel, architectureModelType, additionalConfigs);
            response = new ArdocoResultResponse(result.projectId(), HttpStatus.OK, result.traceLinks(), Messages.RESULT_IS_READY);
        } catch (TimeoutException e) {
            response = new ArdocoResultResponse(e.getId(), HttpStatus.ACCEPTED, Messages.REQUEST_TIMED_OUT_START_AND_WAIT);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }


    @GetMapping("/api/sam-code/{id}")
    public ResponseEntity<ArdocoResultResponse> getResult(
            @Parameter(description = "The ID of the result to query", required = true)  @PathVariable("id") String id)
            throws ArdocoException, IllegalArgumentException {

        //TODO this method is a duplicate from the controller of sadcode
        Optional<String> result = service.getResult(id);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(id, HttpStatus.ACCEPTED, Messages.RESULT_NOT_READY);
        } else {
            response = new ArdocoResultResponse(id, HttpStatus.OK, result.get(), Messages.RESULT_IS_READY);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }


    @GetMapping("/api/sam-code/wait/{id}")
    public ResponseEntity<ArdocoResultResponse> waitForResult(
            @Parameter(description = "The ID of the result to query", required = true) @PathVariable("id") String id)
            throws ArdocoException, InterruptedException, IllegalArgumentException, TimeoutException {

        //TODO this method is a duplicate from the controller of sadcode
        ArdocoResultResponse response;
        try {
            String result = service.waitForResult(id);
            response = new ArdocoResultResponse(id, HttpStatus.OK, result, Messages.RESULT_IS_READY);
        } catch (TimeoutException e) {
            response = new ArdocoResultResponse(id, HttpStatus.ACCEPTED, Messages.REQUEST_TIMED_OUT);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }
}

