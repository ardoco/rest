package io.github.ardoco.rest.api.util;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    public String getHashFromFiles(List<File> files) throws NoSuchAlgorithmException, IOException, IllegalArgumentException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        // Convert the input string to bytes and update the digest
        byte[] filesInByte = convertFilesToByte(files);
        byte[] hashBytes = messageDigest.digest(filesInByte);
        String hash = DatatypeConverter.printHexBinary(hashBytes);

        return hash;
    }

    private byte[] convertFilesToByte(List<File> files) throws IOException, IllegalArgumentException {
        if (files.isEmpty()) {
            throw new IllegalArgumentException("File list is empty");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        for (File file : files) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            outputStream.write(bytes);
        }
        return outputStream.toByteArray( );
    }
}
