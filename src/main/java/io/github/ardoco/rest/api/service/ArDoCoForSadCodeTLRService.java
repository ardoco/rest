package io.github.ardoco.rest.api.service;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.core.common.util.TraceLinkUtilities;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSadCodeTraceabilityLinkRecovery;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import io.github.ardoco.rest.api.exception.FileNotFoundException;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;

@Service
public class ArDoCoForSadCodeTLRService implements runnerTLRService {

    @Override
    public void runPipeline(String projectName, MultipartFile inputText, MultipartFile inputCode, SortedMap<String, String> additionalConfigs) throws Exception {
        File inputTextFile = convertMultipartFileToFile(inputText);
        File inputCodeFile = convertMultipartFileToFile(inputCode);
        File outputDir = Files.createTempDir(); // temporary directory to store the ardoco result in


        //TODO: (1) Check if file type is correct

        //TODO: (2) create ArDoCoForSadCodeTraceabilityLinkRecovery runner
        // output directory should be a temporary directory
        ArDoCoForSadCodeTraceabilityLinkRecovery runner = new ArDoCoForSadCodeTraceabilityLinkRecovery(projectName);

        //TODO: (3) setUp Pipeline
        runner.setUp(inputTextFile, inputCodeFile, additionalConfigs, outputDir);


        //TODO: (4) ArDoCoResult result = runner.run();
        ArDoCoResult result = runner.run();

        // TODO: get tracelinks
        var traceLinks = result.getSadCodeTraceLinks();
        ImmutableList<String> traceLink_string = TraceLinkUtilities.getSadCodeTraceLinksAsStringList(Lists.immutable.ofAll(traceLinks));

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
