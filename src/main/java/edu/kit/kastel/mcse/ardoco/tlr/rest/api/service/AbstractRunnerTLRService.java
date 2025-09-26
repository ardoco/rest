/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.ArdocoException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository.CurrentlyRunningRequestsRepository;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository.DatabaseAccessor;

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
public abstract class AbstractRunnerTLRService extends AbstractService {

    /**
     * Prefix for error messages stored in the database.
     */
    protected static final String ERROR_PREFIX = "Error: ";

    private static final Logger logger = LoggerFactory.getLogger(AbstractRunnerTLRService.class);

    @Autowired
    private CurrentlyRunningRequestsRepository currentlyRunningRequestsRepository;

    /**
     * Database accessor to save and retrieve results from the database.
     */
    @Autowired
    protected DatabaseAccessor databaseAccessor;

    /**
     * The type of trace link handled by this service.
     */
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
    abstract protected ArDoCoApiResult convertResultToJsonString(ArDoCoResult result) throws JsonProcessingException;

    /**
     * Starts a new pipeline asynchronously, if the result is not already available or in progress.
     *
     * @param runner     the ArDoCoRunner to execute the pipeline
     * @param id         the unique identifier of the pipeline
     * @param inputFiles the input files for the pipeline
     * @return an optional containing the result if available, otherwise empty
     */
    public Optional<ArDoCoApiResult> runPipeline(ArDoCoRunner runner, String id, List<File> inputFiles) throws ArdocoException {
        if (!resultIsInDatabase(id) && !resultIsOnItsWay(id)) {
            logger.info("Start new TLR of type {} for {}", this.traceLinkType, id);
            CompletableFuture<ArDoCoApiResult> future = CompletableFuture.supplyAsync(() -> runPipelineAsync(runner, id, inputFiles));
            currentlyRunningRequestsRepository.addRequest(id, future);
        } else if (resultIsInDatabase(id)) {
            return Optional.of(getResultFromDatabase(id));
        }
        return Optional.empty();
    }

    /**
     * Asynchronously runs the pipeline and processes the results.
     *
     * @param runner     the ArDoCoRunner to execute the pipeline
     * @param id         the unique identifier of the pipeline
     * @param inputFiles the input files for the pipeline
     * @return the result in JSON format, or null if an error occurred
     */
    private ArDoCoApiResult runPipelineAsync(ArDoCoRunner runner, String id, List<File> inputFiles) throws ArdocoException {
        ArDoCoApiResult traceLinkJson;
        try {
            logger.info("Starting Pipeline...");
            ArDoCoResult result = runner.run();

            logger.debug("Converting found TraceLinks...");
            traceLinkJson = convertResultToJsonString(result);

            logger.info("Saving found TraceLinks...");
            databaseAccessor.saveResult(id, traceLinkJson.buildJsonString());

        } catch (Exception e) {
            String message = String.format("Error occurred while running the pipeline asynchronously for ID %s : %s", id, e.getMessage());
            logger.error(message, e);
            databaseAccessor.saveResult(id, ERROR_PREFIX + message);
            throw new ArdocoException(message, e);
        } finally {
            currentlyRunningRequestsRepository.removeRequest(id);
            for (File file : inputFiles) {
                file.delete();
            }
        }
        return traceLinkJson;
    }

}
