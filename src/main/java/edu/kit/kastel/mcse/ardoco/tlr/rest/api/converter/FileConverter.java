package edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter;

import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileConversionException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.FileNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a utility class for converting files and handling file-related operations.
 * It provides methods to convert files to byte arrays, convert multipart files to regular files,
 * and detect character encoding in files.
 */
public final class FileConverter {

    private static final Pattern ENCODING_PATTERN = Pattern.compile("encoding=\"([^\"]+)\"");

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
            throw new FileConversionException("Multipart file with name" + multipartFile.getOriginalFilename() + "is null.");
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


    /**
     * Detects the encoding from the XML prolog in the MultipartFile.
     * If no encoding is specified, UTF-8 is used by default.
     * This method reads the first line of the file and attempts to parse the encoding from an XML declaration.
     *
     * @param multipartFile the multipart file to check for encoding
     * @return the detected Charset, or UTF-8 as the default if no encoding is specified
     * @throws IOException if an I/O error occurs
     */
    private static Charset detectEncodingOfFile(MultipartFile multipartFile) throws IOException {
        // Read the first few lines of the file to detect the encoding, since XML files declare the encoding usually there
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();

            if (firstLine != null && firstLine.startsWith("<?xml")) {
                Matcher matcher = ENCODING_PATTERN.matcher(firstLine);
                if (matcher.find()) {
                    String encoding = matcher.group(1);
                    return Charset.forName(encoding);
                }
            }
        }

        return StandardCharsets.UTF_8;
    }
}
