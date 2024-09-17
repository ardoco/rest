package io.github.ardoco.rest.api.service;

import io.github.ardoco.rest.api.api_response.ResultBag;
import io.github.ardoco.rest.api.exception.ArdocoException;
import io.github.ardoco.rest.api.exception.FileConversionException;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import io.github.ardoco.rest.api.exception.HashingException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.SortedMap;

public interface RunnerTLRService {

    /**
     * Starts the trace link recovery pipeline asynchronously for the given project.
     *
     * @param projectName         The name of the project for which the pipeline should run.
     * @param inputText           The documentation file (text) for the project.
     * @param inputCode           The code file for the project.
     * @param additionalConfigs   Any additional configurations needed for the pipeline.
     * @return                    The unique ID associated with the pipeline execution, used for querying the result later.
     * @throws FileNotFoundException      If the input files are not found or invalid.
     * @throws FileConversionException    If there is an error during the file conversion process.
     * @throws HashingException           If there is an error generating a hash from the input files.
     */
    String runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs)
            throws FileNotFoundException, FileConversionException, HashingException;

    /**
     * Retrieves the result of the trace link recovery pipeline for the given ID.
     *
     * @param id   The ID associated with the pipeline execution.
     * @return     An {@code Optional<String>} containing the pipeline result in JSON format or an empty {@code Optional}
     *             if the result is not yet available.
     * @throws ArdocoException           If the pipeline has finished with an error.
     * @throws IllegalArgumentException  If the provided ID does not exist in the database.
     */
    Optional<String> getResult(String id) throws ArdocoException, IllegalArgumentException;

    /**
     * Waits for the result of the trace link recovery pipeline for the given ID.
     * This method blocks until the result is available.
     *
     * @param id   The ID associated with the pipeline execution.
     * @return     The result in JSON format if available.
     * @throws ArdocoException           If the pipeline finished with an error.
     * @throws InterruptedException      If the current thread is interrupted while waiting.
     * @throws IllegalArgumentException  If the provided ID does not exist in the database.
     */
    String waitForResult(String id) throws ArdocoException, InterruptedException, IllegalArgumentException;

    /**
     * Synchronously runs the trace link recovery pipeline and waits for the result.
     *
     * @param projectName         The name of the project for which the pipeline should run.
     * @param inputText           The documentation file (text) for the project.
     * @param inputCode           The code file for the project.
     * @param additionalConfigs   Any additional configurations needed for the pipeline.
     * @return                    The resultBag containing the traceLinks in JSON format if available and the projectId.
     * @throws FileNotFoundException      If the input files are not found or invalid.
     * @throws FileConversionException    If there is an error during the file conversion process.
     * @throws HashingException           If there is an error generating a hash from the input files.
     * @throws ArdocoException            If an error occurs during pipeline execution.
     */
    ResultBag runPipelineAndWaitForResult(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs)
            throws FileConversionException, HashingException, ArdocoException;

}
