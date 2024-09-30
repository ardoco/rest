package io.github.ardoco.rest.api.service;

import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.exception.ArdocoException;
import io.github.ardoco.rest.api.exception.FileConversionException;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import io.github.ardoco.rest.api.exception.HashingException;
import io.github.ardoco.rest.api.repository.DatabaseAccessor;
import io.github.ardoco.rest.api.util.HashGenerator;
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
public class AbstractRunnerTLRService {

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

    public Optional<String> getResult(String id) throws ArdocoException, IllegalArgumentException {
        if (resultIsOnItsWay(id)) {
            logger.log(Level.DEBUG, "Result is not yet available for " + id);
            return Optional.empty();
        }

        return Optional.of(getResultFromDatabase(id));
    }

    public String waitForResult(String id)
            throws ArdocoException, IllegalArgumentException, io.github.ardoco.rest.api.exception.TimeoutException {

        if (resultIsOnItsWay(id)) {
            return waitForResultHelper(id);
        }

        return getResultFromDatabase(id);
    }


    protected boolean resultIsOnItsWay(String id) {
        return asyncTasks.containsKey(id);
    }

    protected boolean resultIsInDatabase(String id) {
        return databaseAccessor.keyExistsInDatabase(id);
    }

    protected String getResultFromDatabase(String id) throws IllegalArgumentException, ArdocoException {
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

    protected String waitForResultHelper(String id) {
        try {
            logger.log(Level.INFO, "Waiting for the result of " + id);
            return asyncTasks.get(id).get(SECONDS_UNTIL_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.log(Level.INFO, "Waiting for " + id + " took too long...");
            throw new io.github.ardoco.rest.api.exception.TimeoutException(id);
        } catch (ExecutionException | InterruptedException e) {
            throw new ArdocoException(e.getMessage());
        }
    }

    protected String generateHashFromFiles(List<File> files, String projectName)
            throws HashingException, FileNotFoundException, FileConversionException {
        logger.log(Level.DEBUG, "Generating ID...");
        HashGenerator hashGenerator = new HashGenerator();
        String hash = hashGenerator.getMD5HashFromFiles(files);
        return projectName + hash;
    }


}
