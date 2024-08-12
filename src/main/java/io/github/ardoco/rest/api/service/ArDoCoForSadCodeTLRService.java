package io.github.ardoco.rest.api.service;

import io.github.ardoco.rest.api.exception.FileNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.SortedMap;

@Service
public class ArDoCoForSadCodeTLRService implements runnerTLRService {

    @Override
    public void runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception {
        File inputTextFile = convertMultipartFileToFile(inputText);
        File inputCodeFile = convertMultipartFileToFile(inputCode);

        //TODO: (1) Check if file type is correct

        //TODO: (2) create ArDoCoForSadCodeTraceabilityLinkRecovery runner

        //TODO: (3) setUp Pipeline

        //TODO: (4) ArDoCoResult result = runner.run();

        //TODO: (5) Convert ArDoCoResult into a ResultEntity, so it can be mapped to a DataRepository and can be stored

    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws Exception {
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
