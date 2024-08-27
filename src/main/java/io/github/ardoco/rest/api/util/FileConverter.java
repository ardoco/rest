package io.github.ardoco.rest.api.util;

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
    public static byte[] convertFilesToByte(List<File> files) throws IOException, IllegalArgumentException {
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

    /**
     * Converts a {@link MultipartFile} to a {@link File} object.
     *
     * @param multipartFile the {@link MultipartFile} to convert
     * @return a {@link File} object containing the contents of the {@link MultipartFile}
     * @throws FileNotFoundException if the provided {@link MultipartFile} is empty
     * @throws IOException if an error occurs during the file transfer
     * @throws Exception for any unexpected errors during file conversion
     */
    public static File convertMultipartFileToFile(MultipartFile multipartFile) throws Exception {
        if (multipartFile.isEmpty()) {
            throw new FileNotFoundException();
        }
        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(file);
        } catch (IOException | IllegalStateException e) {
            throw new IOException("Error occurred while transferring the MultipartFile to File.", e);
        }
        return file;
    }
}
