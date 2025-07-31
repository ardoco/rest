/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.FileConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;

public class FileConverterTest {

    @TempDir
    Path tempDir;

    private File txtFile;
    private File pngFile;

    @BeforeEach
    void setUp() throws Exception {
        // Creating temporary test files in the temporary directory
        txtFile = tempDir.resolve("test.txt").toFile();
        try (FileOutputStream fos = new FileOutputStream(txtFile)) {
            fos.write("This is a test file.".getBytes());
        }

        pngFile = tempDir.resolve("test.png").toFile();
        try (FileOutputStream fos = new FileOutputStream(pngFile)) {
            fos.write("PNG file content".getBytes());
        }
    }

    @Test
    void testConvertFilesToByte_withValidFiles() throws Exception {
        List<File> files = Arrays.asList(txtFile, pngFile);

        byte[] byteArray = FileConverter.convertFilesToByte(files);

        assertNotNull(byteArray);
        assertTrue(byteArray.length > 0);

        // Check that the byte array is the combination of both file contents
        ByteArrayOutputStream expectedContent = new ByteArrayOutputStream();
        expectedContent.write(Files.readAllBytes(txtFile.toPath()));
        expectedContent.write(Files.readAllBytes(pngFile.toPath()));

        assertArrayEquals(expectedContent.toByteArray(), byteArray);
        assertEquals(expectedContent.toString(), new String(byteArray, StandardCharsets.UTF_8), "Expected: " + expectedContent + " but got: " + new String(
                byteArray, StandardCharsets.UTF_8));
    }

    @Test
    void testConvertFilesToByte_withEmptyFileList() {
        List<File> emptyFiles = Collections.emptyList();
        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            FileConverter.convertFilesToByte(emptyFiles);
        });

        assertEquals("File list is empty", exception.getMessage());
    }

    @Test
    void testConvertMultipartFileToFile_withMockMultipartFile_Plaintext() throws IOException, FileNotFoundException, FileConversionException {
        // Create a MockMultipartFile
        String fileContent = "This is a test file content.";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "testfile.txt", "text/plain", fileContent.getBytes());

        // Use FileConverter to convert the MockMultipartFile to a File
        File convertedFile = FileConverter.convertMultipartFileToFile(mockMultipartFile);

        // Verify the file was created
        assertNotNull(convertedFile);
        assertTrue(convertedFile.exists());
        assertEquals("testfile.txt", convertedFile.getName());
        assertEquals("This is a test file content.", new String(Files.readAllBytes(convertedFile.toPath())));
    }

    @Test
    void testConvertMultipartFileToFile_withMockMultipartFile_acmFile() throws IOException, FileNotFoundException, FileConversionException {
        // Create a MockMultipartFile
        String fileContent = "This is a test file content.";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "testfile.txt", "text/plain", fileContent.getBytes());

        // Use FileConverter to convert the MockMultipartFile to a File
        File convertedFile = FileConverter.convertMultipartFileToFile(mockMultipartFile);

        // Verify the file was created
        assertNotNull(convertedFile);
        assertTrue(convertedFile.exists());
        assertEquals("testfile.txt", convertedFile.getName());
        assertEquals("This is a test file content.", new String(java.nio.file.Files.readAllBytes(convertedFile.toPath())));
    }

    @Test
    void testConvertMultipartFileToFile_withMockMultipartFile_png() throws IOException, FileNotFoundException, FileConversionException {
        // Create a MockMultipartFile
        byte[] fileContent = { (byte) 137, (byte) 80, (byte) 78, (byte) 71 };
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "image.png", "image/png", fileContent);

        // Use FileConverter to convert the MockMultipartFile to a File
        File convertedFile = FileConverter.convertMultipartFileToFile(mockMultipartFile);

        // Verify the file was created
        assertNotNull(convertedFile);
        assertTrue(convertedFile.exists());
        assertEquals("image.png", convertedFile.getName());
    }

    @Test
    void testConvertMultipartFileToFile_withEmptyMultipartFile() {
        // Create a MockMultipartFile
        String fileContent = "";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "testfile.txt", "text/plain", fileContent.getBytes());

        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            FileConverter.convertMultipartFileToFile(mockMultipartFile);
        });

        assertEquals("The file with name testfile.txt is empty.", exception.getMessage());
    }

    @Test
    void testConvertMultipartFileToFile_withTransferFailure() throws Exception {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn("failure.txt");
        when(multipartFile.isEmpty()).thenReturn(false);

        doThrow(new IOException("Transfer failed")).when(multipartFile).transferTo(any(File.class));

        Exception exception = assertThrows(FileConversionException.class, () -> {
            FileConverter.convertMultipartFileToFile(multipartFile);
        });

        assertTrue(exception.getMessage().contains("Error occurred while transferring the MultipartFile to File"));
    }
}
