package io.github.ardoco.rest.api.repository;

public interface DatabaseAccessor {

    /**
     * Stores a result in the database for 24 hours
     *
     * @param id the key that should be used to retrieve the jsonResult from the database
     * @param jsonResult the value that should be saved to the database
     * @return the id which can be used to retrieve the result
     */
    String saveResult(String id, String jsonResult);

    /**
     * Retrieves a result from the database by its key.
     *
     * @param id the key that should be used to retrieve the jsonResult from the database
     * @return the jsonResult retrieved from the database
     */
    String getResult(String id);

    /**
     * Checks if a key exists in the database.
     *
     * @param key the key to check in the database
     * @return true if the key exists, false otherwise
     */
    boolean keyExistsInDatabase(String key);
}
