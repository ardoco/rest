package io.github.ardoco.rest.api.service;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.repository.DatabaseAccessor;
import io.github.ardoco.rest.api.util.TraceLinkConverter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service("sadCodeTLRService")
public class ArDoCoForSadCodeTLRService extends AbstractRunnerTLRService {

    private static final Logger logger = LogManager.getLogger(ArDoCoForSadCodeTLRService.class);

    @Autowired
    public ArDoCoForSadCodeTLRService(DatabaseAccessor databaseAccessor) {
        super(databaseAccessor, TraceLinkType.SAD_CODE);
    }

    protected String runPipelineAsync(ArDoCoRunner runner, String id, List<File> inputFiles) {
        String traceLinkJson;
        try {
            // Run the pipeline
            logger.log(Level.INFO, "Starting Pipeline...");
            ArDoCoResult result = runner.run();

            // Convert and store result as JSON
            logger.log(Level.DEBUG, "Converting found TraceLinks...");
            List<SadCodeTraceLink> traceLinks = result.getSadCodeTraceLinks();
            TraceLinkConverter converter = new TraceLinkConverter();
            traceLinkJson = converter.convertListOfSadCodeTraceLinksToJSONString(traceLinks);

            logger.log(Level.INFO, "Saving found TraceLinks...");
            databaseAccessor.saveResult(id, traceLinkJson);

        } catch (Exception e) {
            String message = "Error occurred while running the pipeline asynchronously for ID " + id + ": " + e.getMessage();
            logger.error(message, e);
            databaseAccessor.saveResult(id, ERROR_PREFIX + message);
            return null;
        } finally {
            asyncTasks.remove(id);
            for (File file : inputFiles) {
                file.delete();
            }
        }
        return traceLinkJson;
    }
}
