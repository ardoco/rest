/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.ArdocoException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository.CurrentlyRunningRequestsRepository;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository.DatabaseAccessor;

/**
 * The {@code AbstractService} class provides foundational methods for handling results in the
 * traceability link recovery (TLR) process. It is designed to be extended by specific service
 * implementations.
 * <p>
 * This abstract class provides methods to retrieve results from the database and check the status
 * of results, ensuring that results are handled consistently across different TLR services.
 */
@Service
public abstract class AbstractService {

    /** Prefix for error messages stored in the database. */
    protected static final String ERROR_PREFIX = "Error: ";

    @Autowired
    private CurrentlyRunningRequestsRepository currentlyRunningRequestsRepository;

    /** Database accessor to save and retrieve results from the database. */
    @Autowired
    private DatabaseAccessor databaseAccessor;

    private static final Logger logger = LogManager.getLogger(AbstractService.class);

    /** Default constructor for AbstractService. */
    public AbstractService() { }

    /**
     * Retrieves the result from the database if it is available.
     *
     * @param id the unique identifier of the result
     * @return the result in JSON format
     * @throws IllegalArgumentException if the result is not in the database
     * @throws ArdocoException          if there is an error with the retrieved result
     */
    protected String getResultFromDatabase(String id) throws IllegalArgumentException, ArdocoException {
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

    /**
     * Checks if the result is currently being processed
     * @param id the unique identifier of the result
     * @return true if the result is currently being processed, false otherwise
     */
    protected boolean resultIsOnItsWay(String id) {
        return currentlyRunningRequestsRepository.containsRequest(id);
    }

    /**
     * Checks if the result is already stored in the database.
     *
     * @param id the unique identifier of the result
     * @return true if the result is in the database, false otherwise
     */
    protected boolean resultIsInDatabase(String id) {
        return databaseAccessor.keyExistsInDatabase(id);
    }
}
