package io.github.ardoco.rest.api.util;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class HashGenerator {

    /**
     * Method to generate an MD5 hash for a given string.
     *
     * @param files The List of Files from which one hash value should be generated
     * @return The MD5 hash as a hex string.
     * @throws NoSuchAlgorithmException If MD5 algorithm is not available.
     */
    public String getMD5HashFromFiles(List<File> files) throws NoSuchAlgorithmException, IOException, IllegalArgumentException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        byte[] filesInByte = FileConverter.convertFilesToByte(files);
        byte[] hashBytes = messageDigest.digest(filesInByte);
        String hash = DatatypeConverter.printHexBinary(hashBytes);
        return hash;
    }

}
