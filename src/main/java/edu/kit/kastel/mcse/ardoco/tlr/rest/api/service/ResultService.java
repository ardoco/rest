/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.ArdocoException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository.CurrentlyRunningRequestsRepository;

@Service("resultService")
public class ResultService extends AbstractService {

    /**
     * Timeout for waiting for a result, in seconds.
     */
    @Value("${tlr.timeout.seconds}")
    protected int secondsUntilTimeout;

    private static final Logger logger = LoggerFactory.getLogger(ResultService.class);

    @Autowired
    private CurrentlyRunningRequestsRepository currentlyRunningRequestsRepository;

    /**
     * Retrieves the result from the database if it is available.
     *
     * @param id the unique identifier of the result
     * @return an optional containing the result if available, otherwise empty
     * @throws ArdocoException          if an error occurs retrieving the result from the database
     * @throws IllegalArgumentException if the id is invalid
     */
    public Optional<ArDoCoApiResult> getResult(String id) throws ArdocoException, IllegalArgumentException {
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
     * @throws ArdocoException          if an error occurs while waiting for the result
     * @throws IllegalArgumentException if the id is invalid
     */
    public Optional<ArDoCoApiResult> waitForResult(String id) throws ArdocoException, IllegalArgumentException {
        if (resultIsOnItsWay(id)) {
            return waitForResultHelper(id);
        }
        return Optional.of(getResultFromDatabase(id));
    }

    /**
     * Helper method to wait for a result until it is available or the timeout is reached.
     *
     * @param id the unique identifier of the result
     * @return an optional containing the result if available, otherwise empty
     */
    private Optional<ArDoCoApiResult> waitForResultHelper(String id) {
        try {
            logger.info("Waiting for the result of {}", id);
            return Optional.of(currentlyRunningRequestsRepository.getRequest(id).get(secondsUntilTimeout, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            logger.info("Waiting for {} took too long...", id);
            return Optional.empty();
        } catch (ExecutionException | InterruptedException e) {
            throw new ArdocoException(e.getMessage(), e);
        }
    }

}
