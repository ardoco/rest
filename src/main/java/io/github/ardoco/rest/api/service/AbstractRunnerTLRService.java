package io.github.ardoco.rest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.exception.ArdocoException;
import io.github.ardoco.rest.api.repository.DatabaseAccessor;
import io.github.ardoco.rest.api.util.Messages;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public abstract class AbstractRunnerTLRService {

    protected static final String ERROR_PREFIX = "Error: ";
    protected static final int SECONDS_UNTIL_TIMEOUT = 60;

    private static final Logger logger = LogManager.getLogger(AbstractRunnerTLRService.class);

    /** Map to track the progress of async tasks*/
    protected static ConcurrentHashMap<String, CompletableFuture<String>> asyncTasks = new ConcurrentHashMap<>();


    protected final DatabaseAccessor databaseAccessor;
    protected final TraceLinkType traceLinkType;

    public AbstractRunnerTLRService(@Autowired DatabaseAccessor databaseAccessor, @Autowired(required = false) TraceLinkType traceLinkType) {
        this.databaseAccessor = databaseAccessor;
        this.traceLinkType = traceLinkType;
    }

    abstract protected String convertResultToJSONString(ArDoCoResult result) throws JsonProcessingException;


    public Optional<String> runPipeline(ArDoCoRunner runner, String id, List<File> inputFiles) {
        return runPipelineHelper(runner, id, inputFiles, false);
    }

    public Optional<String> runPipelineAndWaitForResult(ArDoCoRunner runner, String id, List<File> inputFiles) {
        return runPipelineHelper(runner, id, inputFiles, true);
    }

    private Optional<String> runPipelineHelper(ArDoCoRunner runner, String id, List<File> inputFiles, boolean waitForResult)
            throws ArdocoException, io.github.ardoco.rest.api.exception.TimeoutException {

        if (!resultIsInDatabase(id) && !resultIsOnItsWay(id)) {
            logger.log(Level.INFO, "Start new TLR of type " + this.traceLinkType + " for: " + id);
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

    // templateMethod for starting the pipeline
    private String runPipelineAsync(ArDoCoRunner runner, String id, List<File> inputFiles) {
        String traceLinkJson;
        try {
            // Run the pipeline
            logger.log(Level.INFO, "Starting Pipeline...");
            ArDoCoResult result = runner.run();

            logger.log(Level.DEBUG, "Converting found TraceLinks...");
            traceLinkJson = convertResultToJSONString(result);

            logger.log(Level.INFO, "Saving found TraceLinks...");
            databaseAccessor.saveResult(id, traceLinkJson);

        } catch (Exception e) {
            String message = "Error occurred while running the pipeline asynchronously for ID " + id + ": " + e.getMessage();
            logger.error(message, e);
            databaseAccessor.saveResult(id, ERROR_PREFIX + message);
            return null;
        } finally {
            asyncTasks.remove(id);
            for (File file : inputFiles) {
                file.delete();
            }
        }
        return traceLinkJson;
    }


    public Optional<String> getResult(String id) throws ArdocoException, IllegalArgumentException {
        if (resultIsOnItsWay(id)) {
            logger.log(Level.DEBUG, "Result is not yet available for " + id);
            return Optional.empty();
        }
        return Optional.of(getResultFromDatabase(id));
    }

    public Optional<String> waitForResult(String id)
            throws ArdocoException, IllegalArgumentException, io.github.ardoco.rest.api.exception.TimeoutException {

        if (resultIsOnItsWay(id)) {
            return waitForResultHelper(id);
        }
        return Optional.of(getResultFromDatabase(id));
    }


    private Optional<String> waitForResultHelper(String id) {
        try {
            logger.log(Level.INFO, "Waiting for the result of " + id);
            return Optional.of(asyncTasks.get(id).get(SECONDS_UNTIL_TIMEOUT, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            logger.log(Level.INFO, "Waiting for " + id + " took too long...");
            return Optional.empty();
        } catch (ExecutionException | InterruptedException e) {
            throw new ArdocoException(e.getMessage());
        }
    }

    private String getResultFromDatabase(String id) throws IllegalArgumentException, ArdocoException {
        if (!resultIsInDatabase(id)) {
            throw new IllegalArgumentException(Messages.noResultForKey(id));
        }

        String result = databaseAccessor.getResult(id);
        if (result == null || result.startsWith(ERROR_PREFIX)) {
            throw new ArdocoException(result);
        }
        logger.log(Level.INFO, "Result is available for " + id);
        return result;
    }

    private boolean resultIsOnItsWay(String id) {
        return asyncTasks.containsKey(id);
    }

    private boolean resultIsInDatabase(String id) {
        return databaseAccessor.keyExistsInDatabase(id);
    }
}
