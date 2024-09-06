package io.github.ardoco.rest.api.util;

import io.github.ardoco.rest.api.exception.FileConversionException;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * This is a utility class for converting files and handling file-related operations.
 */
public final class FileConverter {

    private FileConverter() {}


    /**
     * Converts a list of {@link File} objects to a single byte array.
     * It reads the contents of each file in the list and concatenates them into a single byte array.
     *
     * @param files the list of {@link File} objects to convert to a byte array
     * @return a byte array representing the concatenated contents of all files
     * @throws IOException if an I/O error occurs reading from any of the files
     * @throws IllegalArgumentException if the provided file list is empty
     */
    public static byte[] convertFilesToByte(List<File> files) throws FileConversionException, FileNotFoundException {
        if (files.isEmpty()) {
            throw new FileNotFoundException("File list is empty");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            for (File file : files) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                outputStream.write(bytes);
            }
        } catch (Exception e) {
            throw new FileConversionException("Error occurred while transferring the MultipartFile to File: " + e.getMessage());
        }
        return outputStream.toByteArray( );
    }

    /**
     * Converts a {@link MultipartFile} to a {@link File} object.
     *
     * @param multipartFile the {@link MultipartFile} to convert
     * @return a {@link File} object containing the contents of the {@link MultipartFile}
     */
    public static File convertMultipartFileToFile(MultipartFile multipartFile) throws FileNotFoundException, FileConversionException {
        if (multipartFile.isEmpty()) {
            throw new FileNotFoundException("The file with name " + multipartFile.getOriginalFilename() + " is empty");
        }
        try {
            File file = new File(System.getProperty("java.io.tmpdir") + File.separator + multipartFile.getOriginalFilename());
            multipartFile.transferTo(file);
            return file;
        } catch (Exception e) {
            throw new FileConversionException("Error occurred while transferring the MultipartFile to File: " + e.getMessage());
        }
    }
}
