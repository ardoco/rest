package io.github.ardoco.rest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.exception.ArdocoException;
import io.github.ardoco.rest.api.repository.DatabaseAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The {@code AbstractRunnerTLRService} provides a template for services that
 * handle TraceLink Recovery (TLR) operations.
 * <p>
 * This class provides methods for running pipeline and waiting for pipelines, handling
 * asynchronous tasks, and interacting with a database for storing and retrieving
 * results.
 * <p>
 * Implementing classes are responsible for defining how the {@link ArDoCoResult} is
 * converted to JSON format by implementing the {@link #convertResultToJsonString(ArDoCoResult)} method.
 */
public abstract class AbstractRunnerTLRService {

    /** Prefix for error messages stored in the database. */
    protected static final String ERROR_PREFIX = "Error: ";

    /** Timeout for waiting for a result, in seconds. */
    @Value("${tlr.timeout.seconds}")
    protected int secondsUntilTimeout;

    private static final Logger logger = LogManager.getLogger(AbstractRunnerTLRService.class);

    /** Map for tracking currently asynchronous task. */
    protected static ConcurrentHashMap<String, CompletableFuture<String>> asyncTasks = new ConcurrentHashMap<>();

    /** Database accessor to save and retrieve results from the database. */
    @Autowired
    protected DatabaseAccessor databaseAccessor;

    /** The type of trace link handled by this service. */
    protected final TraceLinkType traceLinkType;

    /**
     * Constructor to initialize database accessor and trace link type.
     *
     * @param traceLinkType the trace link type for which this service is for
     */
    public AbstractRunnerTLRService(TraceLinkType traceLinkType) {
        this.traceLinkType = traceLinkType;
    }

    /**
     * Takes the {@link ArDoCoResult} and converts the contained trace links of the service's type to JSON format.
     *
     * @param result the ArDoCo result containing trace links
     * @return JSON representation of trace links
     * @throws JsonProcessingException if there is an error during conversion
     */
    abstract protected String convertResultToJsonString(ArDoCoResult result) throws JsonProcessingException;

    /**
     * Retrieves the result from the database if it is available.
     *
     * @param id the unique identifier of the result
     * @return an optional containing the result if available, otherwise empty
     * @throws ArdocoException if an error occurs retrieving the result from the database
     * @throws IllegalArgumentException if the id is invalid
     */
    public Optional<String> getResult(String id) throws ArdocoException, IllegalArgumentException {
        if (resultIsOnItsWay(id)) {
            logger.debug("Result is not yet available for {}", id);
            return Optional.empty();
        }
        return Optional.of(getResultFromDatabase(id));
    }

    /**
     * Waits for a result until it is available, with a timeout.
     *
     * @param id the unique identifier of the result
     * @return an optional containing the result if available, otherwise empty
     * @throws ArdocoException if an error occurs while waiting for the result
     * @throws IllegalArgumentException if the id is invalid
     */
    public Optional<String> waitForResult(String id) throws ArdocoException, IllegalArgumentException {
        if (resultIsOnItsWay(id)) {
            return waitForResultHelper(id);
        }
        return Optional.of(getResultFromDatabase(id));
    }

    /**
     * Starts a new pipeline asynchronously, if the result is not already available or in progress.
     *
     * @param runner the ArDoCoRunner to execute the pipeline
     * @param id the unique identifier of the pipeline
     * @param inputFiles the input files for the pipeline
     * @return an optional containing the result if available, otherwise empty
     */
    public Optional<String> runPipeline(ArDoCoRunner runner, String id, List<File> inputFiles)
            throws ArdocoException {
        return runPipelineHelper(runner, id, inputFiles, false);
    }

    /**
     * Starts a new pipeline synchronously, waiting for the result.
     *
     * @param runner the ArDoCoRunner to execute the pipeline
     * @param id the unique identifier of the pipeline
     * @param inputFiles the input files for the pipeline
     * @return an optional containing the result if available, otherwise empty
     */
    public Optional<String> runPipelineAndWaitForResult(ArDoCoRunner runner, String id, List<File> inputFiles)
            throws ArdocoException {
        return runPipelineHelper(runner, id, inputFiles, true);
    }

    /**
     * Helper method to start the pipeline, either synchronously or asynchronously.
     *
     * @param runner the ArDoCoRunner to execute the pipeline
     * @param id the unique identifier of the pipeline
     * @param inputFiles the input files for the pipeline
     * @param waitForResult if true, waits for the result synchronously
     * @return an optional containing the result if available, otherwise empty
     */
    private Optional<String> runPipelineHelper(ArDoCoRunner runner, String id, List<File> inputFiles, boolean waitForResult)
            throws ArdocoException {

        if (!resultIsInDatabase(id) && !resultIsOnItsWay(id)) {
            logger.info("Start new TLR of type {} for {}", this.traceLinkType, id);
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
                    runPipelineAsync(runner, id, inputFiles)
            );
            asyncTasks.put(id, future);
        } else if (resultIsInDatabase(id)) {
            return Optional.of(getResultFromDatabase(id));
        }

        if (waitForResult) {
            return waitForResultHelper(id);
        }
        return Optional.empty();
    }

    /**
     * Asynchronously runs the pipeline and processes the results.
     *
     * @param runner the ArDoCoRunner to execute the pipeline
     * @param id the unique identifier of the pipeline
     * @param inputFiles the input files for the pipeline
     * @return the result in JSON format, or null if an error occurred
     */
    private String runPipelineAsync(ArDoCoRunner runner, String id, List<File> inputFiles) throws ArdocoException {
        String traceLinkJson;
        try {
            logger.info("Starting Pipeline...");
            ArDoCoResult result = runner.run();

            logger.debug("Converting found TraceLinks...");
            traceLinkJson = convertResultToJsonString(result);

            logger.info("Saving found TraceLinks...");
            databaseAccessor.saveResult(id, traceLinkJson);

        } catch (Exception e) {
            String message = String.format("Error occurred while running the pipeline asynchronously for ID %s : %s", id, e.getMessage());
            logger.error(message, e);
            databaseAccessor.saveResult(id, ERROR_PREFIX + message);
            throw new ArdocoException(message, e);
        } finally {
            asyncTasks.remove(id);
            for (File file : inputFiles) {
                file.delete();
            }
        }
        return traceLinkJson;
    }

    /**
     * Helper method to wait for a result until it is available or the timeout is reached.
     *
     * @param id the unique identifier of the result
     * @return an optional containing the result if available, otherwise empty
     */
    private Optional<String> waitForResultHelper(String id) {
        try {
            logger.info("Waiting for the result of {}", id);
            return Optional.of(asyncTasks.get(id).get(secondsUntilTimeout, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            logger.info("Waiting for {} took too long...", id);
            return Optional.empty();
        } catch (ExecutionException | InterruptedException e) {
            throw new ArdocoException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves the result from the database if it is available.
     *
     * @param id the unique identifier of the result
     * @return the result in JSON format
     * @throws IllegalArgumentException if the result is not in the database
     * @throws ArdocoException if there is an error with the retrieved result
     */
    private String getResultFromDatabase(String id) throws IllegalArgumentException, ArdocoException {
        if (!resultIsInDatabase(id)) {
            throw new IllegalArgumentException(String.format("No result with key %s found.", id));
        }

        String result = databaseAccessor.getResult(id);
        if (result == null || result.startsWith(ERROR_PREFIX)) {
            throw new ArdocoException(result);
        }
        logger.info("Result is available for {}", id);
        return result;
    }

    private boolean resultIsOnItsWay(String id) {
        return asyncTasks.containsKey(id);
    }

    private boolean resultIsInDatabase(String id) {
        return databaseAccessor.keyExistsInDatabase(id);
    }
}
