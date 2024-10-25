package io.github.ardoco.rest.api.controller;

import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import io.github.ardoco.rest.api.api_response.ArdocoResultResponse;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.exception.ArdocoException;
import io.github.ardoco.rest.api.exception.FileConversionException;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import io.github.ardoco.rest.api.exception.TimeoutException;
import io.github.ardoco.rest.api.messages.ResultMessages;
import io.github.ardoco.rest.api.service.AbstractRunnerTLRService;
import io.github.ardoco.rest.api.util.HashGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * The {@code AbstractController} class provides foundational methods for handling various REST responses
 * for traceability link recovery (TLR) processes. It is designed to work with specific types of trace links
 * and delegate processing to the {@link AbstractRunnerTLRService}.
 * <p>
 * This abstract class is intended to be extended by specific controller implementations that can handle
 * different types of trace links, represented by the {@link TraceLinkType} enum.
 */
public abstract class AbstractController {

    protected final TraceLinkType traceLinkType;

    protected final AbstractRunnerTLRService service;

    private static final Logger logger = LogManager.getLogger(AbstractController.class);

    /**
     * Constructs a new {@code AbstractController} with the specified service and trace link type.
     *
     * @param service the service responsible for trace link recovery operations
     * @param traceLinkType the type of trace link this controller manages
     */
    public AbstractController(AbstractRunnerTLRService service, TraceLinkType traceLinkType) {
        this.traceLinkType = traceLinkType;
        this.service = service;
    }


    /**
     * Handles the process of running a pipeline and building a response based on the result status.
     *
     * @param runner the {@link ArDoCoRunner} instance to execute
     * @param requestId the unique request ID associated with the pipeline run
     * @param inputFiles the list of input files for the pipeline run
     * @return a {@link ResponseEntity} containing the {@link ArdocoResultResponse} with the status and result message
     * @throws FileNotFoundException if any of the input files cannot be found
     * @throws FileConversionException if there's an error converting any file during the pipeline process
     */
    protected ResponseEntity<ArdocoResultResponse> handleRunPipeLineResult(ArDoCoRunner runner, String requestId, List<File> inputFiles)
            throws FileNotFoundException, FileConversionException {
        Optional<String> result = service.runPipeline(runner, requestId, inputFiles);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, ResultMessages.RESULT_IS_BEING_PROCESSED);
        } else {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, result.get(), ResultMessages.RESULT_IS_READY);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * Handles the retrieval of a result and builds an appropriate response based on the result status.
     *
     * @param requestId the unique request ID for retrieving the result
     * @return a {@link ResponseEntity} containing the {@link ArdocoResultResponse} with the status and result message
     * @throws ArdocoException if an error occurs while fetching the result
     * @throws IllegalArgumentException if the provided requestId is invalid
     */
    protected ResponseEntity<ArdocoResultResponse> handleGetResult(String requestId) throws ArdocoException, IllegalArgumentException  {
        Optional<String> result = service.getResult(requestId);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(requestId, HttpStatus.ACCEPTED, ResultMessages.RESULT_NOT_READY);
        } else {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, result.get(), ResultMessages.RESULT_IS_READY);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }


    /**
     * Handles the process of waiting for a result to become available, building a response based on the status.
     *
     * @param requestId the unique request ID for retrieving the result
     * @return a {@link ResponseEntity} containing the {@link ArdocoResultResponse} with the status and result message
     * @throws ArdocoException if an error occurs while waiting for the result
     * @throws IllegalArgumentException if the provided requestId is invalid
     * @throws TimeoutException if waiting for the result times out
     */
    protected ResponseEntity<ArdocoResultResponse> handleWaitForResult(String requestId) throws ArdocoException, IllegalArgumentException, TimeoutException {
        Optional<String> result = service.waitForResult(requestId);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
            response = new ArdocoResultResponse(requestId, HttpStatus.ACCEPTED, ResultMessages.REQUEST_TIMED_OUT);
        } else {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, result.get(), ResultMessages.RESULT_IS_READY);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }

    /**
     * Handles the process of running a pipeline and waiting for the result, building a response accordingly.
     *
     * @param runner the {@link ArDoCoRunner} instance to execute
     * @param requestId the unique request ID associated with the pipeline run
     * @param inputFiles the list of input files for the pipeline run
     * @return a {@link ResponseEntity} containing the {@link ArdocoResultResponse} with the status and result message
     * @throws ArdocoException if an error occurs during the pipeline process or waiting for the result
     */
    protected ResponseEntity<ArdocoResultResponse> handleRunPipelineAndWaitForResult(ArDoCoRunner runner, String requestId, List<File> inputFiles) throws ArdocoException{
        Optional<String> result = service.runPipelineAndWaitForResult(runner, requestId, inputFiles);
        ArdocoResultResponse response;
        if (result.isEmpty()) {
             response = new ArdocoResultResponse(requestId, HttpStatus.ACCEPTED, ResultMessages.REQUEST_TIMED_OUT_START_AND_WAIT);
        } else {
            response = new ArdocoResultResponse(requestId, HttpStatus.OK, result.get(), ResultMessages.RESULT_IS_READY);
        }
        return new ResponseEntity<>(response, response.getStatus());
    }


    protected String generateRequestId(List<File> files, String projectName)
            throws FileNotFoundException, FileConversionException {
        logger.info("Generating ID...");
        String hash = HashGenerator.getMD5HashFromFiles(files);
        return traceLinkType.getKeyPrefix() + projectName + hash;
    }
}
