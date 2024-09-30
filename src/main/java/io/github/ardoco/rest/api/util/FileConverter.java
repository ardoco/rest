package io.github.ardoco.rest.api.util;

import io.github.ardoco.rest.api.exception.FileConversionException;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a utility class for converting files and handling file-related operations.
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
     * @throws FileConversionException
     * @throws FileNotFoundException
     */
    public static byte[] convertFilesToByte(List<File> files) throws FileConversionException, FileNotFoundException {
        if (files.isEmpty()) {
            throw new FileNotFoundException("File list is empty");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            for (File file : files) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                outputStream.write(bytes);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new FileConversionException("Error occurred while transferring the MultipartFile to File: " + e.getMessage());
        }
    }

    /**
     * Converts a {@link MultipartFile} to a {@link File} object.
     *
     * @param multipartFile the {@link MultipartFile} to convert
     * @return a {@link File} object containing the contents of the {@link MultipartFile}
     */
    public static File convertMultipartFileToFile(MultipartFile multipartFile) throws FileNotFoundException, FileConversionException {
        if (multipartFile == null) {
            throw new FileConversionException("Multipart file is null");
        }

        if (multipartFile.isEmpty()) {
            throw new FileNotFoundException("The file with name " + multipartFile.getOriginalFilename() + " is empty");
        }
//
//            File file = new File(System.getProperty("java.io.tmpdir") + File.separator + multipartFile.getOriginalFilename());
//            multipartFile.transferTo(file);
////            return file;
//            File file = new File(multipartFile.getOriginalFilename());
//            file.createNewFile();
//
//
//
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(multipartFile.getBytes());
//            fos.close();
//            return file;
//        } catch (Exception e) {
//            throw new FileConversionException("Error occurred while transferring the MultipartFile to File: " + e.getMessage());
//        }

        try {
            // Create a temporary file to store the converted content
            File convertedFile = new File(System.getProperty("java.io.tmpdir") + File.separator + multipartFile.getOriginalFilename());
            convertedFile.createNewFile();

            Charset encoding = detectEncodingOfFile(multipartFile);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), encoding));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(convertedFile), encoding))) { // try-with-resources

                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            return convertedFile;
        } catch (Exception e) {
            throw new FileConversionException("Error occurred while transferring the MultipartFile to File: " + e.getMessage());
        }
    }


    /**
     * Detects the encoding from the XML prolog in the MultipartFile. Defaults to UTF-8 if no encoding is specified.
     *
     * @param multipartFile the multipart file to check for encoding
     * @return the detected Charset, or UTF-8 as the default
     * @throws IOException if an I/O error occurs
     */
    private static Charset detectEncodingOfFile(MultipartFile multipartFile) throws IOException {
        // Read the first few lines of the file to detect the encoding, since XML files declare the encoding usually there
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();

            // Check if the first line contains an encoding declaration (e.g., <?xml version="1.0" encoding="UTF-8"?>)
            if (firstLine != null && firstLine.startsWith("<?xml")) {
                Matcher matcher = ENCODING_PATTERN.matcher(firstLine);
                if (matcher.find()) {
                    String encoding = matcher.group(1);
                    return Charset.forName(encoding);
                }
            }
        }

        // Default to UTF-8 if no encoding is found or if it's not an XML file
        return StandardCharsets.UTF_8;
    }
}
