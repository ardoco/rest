/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;

public class HashGeneratorTest {

    private File tempFile1;
    private File tempFile2;
    private File tempFile3;

    @BeforeEach
    void setUp() throws IOException {
        // Create temporary files for testing
        tempFile1 = File.createTempFile("tempFile1", ".txt");
        tempFile2 = File.createTempFile("tempFile2", ".txt");
        tempFile3 = File.createTempFile("tempFile3", ".txt");

        // Write some data to the files
        try (FileOutputStream fos = new FileOutputStream(tempFile1)) {
            fos.write("Hello World!".getBytes());
        }
        try (FileOutputStream fos = new FileOutputStream(tempFile2)) {
            fos.write("Hello World!".getBytes());
        }
        try (FileOutputStream fos = new FileOutputStream(tempFile3)) {
            fos.write("Different content".getBytes());
        }
    }

    @Test
    void testHashesAreEqualForSameFileContent() throws FileNotFoundException, FileConversionException {
        // Single file hash test
        List<File> file1 = Collections.singletonList(tempFile1);
        String hash1 = HashGenerator.getMD5HashFromFiles(file1);

        List<File> file2 = Collections.singletonList(tempFile2);
        String hash2 = HashGenerator.getMD5HashFromFiles(file2);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertEquals(hash1, hash2);
        assertEquals(hash1.length(), hash2.length());
    }

    @Test
    void testMultipleFilesHash() throws FileNotFoundException, FileConversionException {
        // Multiple files hash test
        List<File> files = Arrays.asList(tempFile1, tempFile2);
        String hash = HashGenerator.getMD5HashFromFiles(files);

        List<File> file1 = Collections.singletonList(tempFile1);
        String hash2 = HashGenerator.getMD5HashFromFiles(file1);

        assertNotNull(hash);
        assertNotNull(hash2);
        assertNotEquals(hash, hash2);
        assertEquals(hash.length(), hash2.length());
    }

    @Test
    void testEmptyFileList() {
        // Empty file list test
        List<File> files = Collections.emptyList();

        assertThrows(FileNotFoundException.class, () -> HashGenerator.getMD5HashFromFiles(files));
    }

    @Test
    void testNonExistingFile() {
        // Non-existing file test
        File nonExistingFile = new File("nonExistingFile.txt");
        List<File> files = Collections.singletonList(nonExistingFile);
        assertThrows(FileConversionException.class, () -> HashGenerator.getMD5HashFromFiles(files));
    }

    @Test
    void testDifferentFileContentHash() throws FileNotFoundException, FileConversionException {
        // Different file content should generate different hashes
        List<File> files1 = Collections.singletonList(tempFile1);
        List<File> files2 = Collections.singletonList(tempFile3);

        String hash1 = HashGenerator.getMD5HashFromFiles(files1);
        String hash2 = HashGenerator.getMD5HashFromFiles(files2);

        assertNotEquals(hash1, hash2);
    }
}
