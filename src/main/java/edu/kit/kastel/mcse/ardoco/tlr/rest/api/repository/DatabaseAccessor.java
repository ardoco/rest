package edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository;

/**
 * Interface for the repository of the used database to implement, allowing for easy database switching.
 * This interface provides methods to store, retrieve, check, and delete results by key.
 */
public interface DatabaseAccessor {

    /**
     * Stores a result in the database for a defined period (typically 24 hours).
     *
     * @param id the unique key that will be used to retrieve the result from the database
     * @param jsonResult the JSON-formatted result to save in the database
     * @return the id used to store and retrieve the result, for confirmation
     */
    String saveResult(String id, String jsonResult);

    /**
     * Retrieves a stored result from the database by its unique key.
     *
     * @param id the unique key for identifying and retrieving the result from the database
     * @return the JSON-formatted result corresponding to the provided key, or null if no result is found
     */
    String getResult(String id);

    /**
     * Checks if a specified key exists in the database.
     *
     * @param key the unique key to check in the database
     * @return {@code true} if the key exists in the database; {@code false} otherwise
     */
    boolean keyExistsInDatabase(String key);

    /**
     * Deletes a result from the database based on its unique key.
     *
     * @param id the unique key identifying the result to delete
     * @return {@code true} if the result was successfully deleted; {@code false} if the key does not exist
     */
    boolean deleteResult(String id);
}
