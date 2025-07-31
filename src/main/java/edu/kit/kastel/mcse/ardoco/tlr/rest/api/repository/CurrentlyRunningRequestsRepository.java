package edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for managing asynchronous requests that are currently being processed by ardoco.
 * This repository is application-scoped, ensuring a single shared instance.
 */
@Repository
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CurrentlyRunningRequestsRepository {

    private final ConcurrentHashMap<String, CompletableFuture<ArDoCoApiResult>> asyncRequests = new ConcurrentHashMap<>();

    /**
     * Default constructor for CurrentlyRunningRequestsRepository.
     */
    public CurrentlyRunningRequestsRepository() {
        // No initialization needed, ConcurrentHashMap is already thread-safe
    }

    /**
     * Adds a new request to the repository.
     *
     * @param id the identifier for the request
     * @param request the request to add
     */
    public void addRequest(String id, CompletableFuture<ArDoCoApiResult> request) {
        asyncRequests.put(id, request);
    }

    /**
     * Retrieves a request by its identifier.
     *
     * @param id the identifier of the request
     * @return the request, or null if not found
     */
    public CompletableFuture<ArDoCoApiResult> getRequest(String id) {
        return asyncRequests.get(id);
    }

    /**
     * Checks if a request with the specified identifier exists.
     *
     * @param id the identifier to check
     * @return true if the request exists, false otherwise
     */
    public boolean containsRequest(String id) {
        return asyncRequests.containsKey(id);
    }

    /**
     * Removes a request by its identifier.
     *
     * @param id the identifier of the request to remove
     */
    public void removeRequest(String id) {
        asyncRequests.remove(id);
    }
}
