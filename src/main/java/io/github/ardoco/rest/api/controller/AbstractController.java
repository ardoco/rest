package io.github.ardoco.rest.api.controller;

import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import io.github.ardoco.rest.api.api_response.ResultBag;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.exception.*;
import io.github.ardoco.rest.api.service.AbstractRunnerTLRService;
import io.github.ardoco.rest.api.util.HashGenerator;
import io.github.ardoco.rest.api.util.Messages;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.List;
import java.util.Optional;


public abstract class AbstractController {

    protected final TraceLinkType traceLinkType;

    protected final AbstractRunnerTLRService service;

    private static final Logger logger = LogManager.getLogger(AbstractController.class);

    public AbstractController(AbstractRunnerTLRService service, TraceLinkType traceLinkType) {
        this.traceLinkType = traceLinkType;
        this. service = service;
    }


    // build result for runPipeline
    protected ResponseEntity<ArdocoResultResponse> handleRunPipeLineResult(ArDoCoRunner runner, String requestId, List<File> inputFiles)
            throws FileNotFoundException, FileConversionException, HashingException {
        Optional<String> result = service.runPipeline(runner, requestId, inputFiles);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, Messages.RESULT_IS_BEING_PROCESSED);
        } else {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, result.get(), Messages.RESULT_IS_READY, traceLinkType);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }

    // build result for getResult
    protected ResponseEntity<ArdocoResultResponse> handleGetResult(String requestId) throws ArdocoException, IllegalArgumentException  {
        Optional<String> result = service.getResult(requestId);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(requestId, HttpStatus.ACCEPTED, Messages.RESULT_NOT_READY);
        } else {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, result.get(), Messages.RESULT_IS_READY, TraceLinkType.fromId(requestId));
        }
        return new ResponseEntity<>(response, response.getStatus());
    }

    // build result for waitForResult
    protected ResponseEntity<ArdocoResultResponse> handleWaitForResult(String requestId) throws ArdocoException, IllegalArgumentException, TimeoutException {
        Optional<String> result = service.waitForResult(requestId);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(requestId, HttpStatus.ACCEPTED, Messages.REQUEST_TIMED_OUT);
        } else {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, result.get(), Messages.RESULT_IS_READY, TraceLinkType.fromId(requestId));
        }
        return new ResponseEntity<>(response, response.getStatus());
    }

    // build result for runPipelineAndWaitForResult
    protected ResponseEntity<ArdocoResultResponse> handleRunPipelineAndWaitForResult(ArDoCoRunner runner, String requestId, List<File> inputFiles) throws ArdocoException{
        Optional<String> result = service.runPipelineAndWaitForResult(runner, requestId, inputFiles);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
             response = new ArdocoResultResponse(requestId, HttpStatus.ACCEPTED, Messages.REQUEST_TIMED_OUT_START_AND_WAIT);
        } else {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, result.get(), Messages.RESULT_IS_READY, TraceLinkType.fromId(requestId));

        }
        return new ResponseEntity<>(response, response.getStatus());
    }


    protected String generateRequestId(List<File> files, String projectName)
            throws HashingException, FileNotFoundException, FileConversionException {
        logger.log(Level.DEBUG, "Generating ID...");
        HashGenerator hashGenerator = new HashGenerator();
        String hash = hashGenerator.getMD5HashFromFiles(files);
        return traceLinkType.getKeyPrefix() + projectName + hash;
    }
}
