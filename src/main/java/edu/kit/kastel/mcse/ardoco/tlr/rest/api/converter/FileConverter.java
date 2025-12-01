/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;

/**
 * This is a utility class for converting files and handling file-related operations.
 * It provides methods to convert files to byte arrays, convert multipart files to regular files,
 * and detect character encoding in files.
 */
public final class FileConverter {

    private FileConverter() {
    }

    /**
     * Converts a list of {@link File} objects to a single byte array.
     * It reads the contents of each file in the list and concatenates them into a single byte array.
     *
     * @param files the list of {@link File} objects to convert to a byte array
     * @return a byte array representing the concatenated contents of all files
     * @throws FileConversionException if an error occurs during file reading or conversion
     * @throws FileNotFoundException   if the provided file list is empty
     */
    public static byte[] convertFilesToByte(List<File> files) throws FileConversionException, FileNotFoundException {
        if (files.isEmpty()) {
            throw new FileNotFoundException("File list is empty");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (File file : files) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                outputStream.write(bytes);
            } catch (IOException e) {
                throw new FileConversionException("Error occurred while transferring the file with name " + file.getName() + " to Bytes: " + e.getMessage(), e);
            }
        }
        return outputStream.toByteArray();
    }

    /**
     * Converts a {@link MultipartFile} to a {@link File} object.
     * If the multipart file is empty or null, an exception is thrown.
     * Otherwise, the file is saved in the system’s temporary directory.
     *
     * @param multipartFile the {@link MultipartFile} to convert
     * @return a {@link File} object containing the contents of the {@link MultipartFile}
     * @throws FileNotFoundException   if the file is empty or null
     * @throws FileConversionException if an error occurs during file conversion
     */
    public static File convertMultipartFileToFile(MultipartFile multipartFile) throws FileNotFoundException, FileConversionException {
        if (multipartFile == null) {
            throw new FileConversionException("Multipart file is null.");
        }

        if (multipartFile.isEmpty()) {
            throw new FileNotFoundException("The file with name " + multipartFile.getOriginalFilename() + " is empty.");
        }

        try {
            // Create a temporary file
            File convertedFile = new File(System.getProperty("java.io.tmpdir") + File.separator + multipartFile.getOriginalFilename());
            convertedFile.createNewFile();

            // Transfer content to the file
            multipartFile.transferTo(convertedFile);

            return convertedFile;
        } catch (IOException e) {
            throw new FileConversionException("Error occurred while transferring the MultipartFile to File: " + e.getMessage(), e);
        }
    }
}
