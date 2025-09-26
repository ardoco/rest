/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.util;

import java.io.File;
import java.util.List;
import java.util.UUID;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.FileConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;

/**
 * This class provides hashing capabilities. It is used as part of generating a unique ID for a request.
 */

public final class HashGenerator {

    private HashGenerator() {
    }

    /**
     * Method to generate an MD5 hash for a given string.
     *
     * @param files The List of Files from which one hash value should be generated
     * @return The MD5 hash as a hex string.
     */
    public static String getMD5HashFromFiles(List<File> files) throws FileNotFoundException, FileConversionException {
        byte[] filesInByte = FileConverter.convertFilesToByte(files);
        return UUID.nameUUIDFromBytes(filesInByte).toString();
    }

}
