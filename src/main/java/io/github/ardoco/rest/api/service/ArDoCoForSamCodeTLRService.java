package io.github.ardoco.rest.api.service;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
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

@Service("samCodeTLRService")
public class ArDoCoForSamCodeTLRService extends AbstractRunnerTLRService {

    private static final Logger logger = LogManager.getLogger(ArDoCoForSamCodeTLRService.class);

    @Autowired
    public ArDoCoForSamCodeTLRService(DatabaseAccessor databaseAccessor) {
        super(databaseAccessor, TraceLinkType.SAM_CODE);
    }

    protected String runPipelineAsync(ArDoCoRunner runner, String id, List<File> inputFiles) {
        String traceLinkJson;
        try {
            // Run the pipeline
            logger.log(Level.INFO, "Starting Pipeline...");
            ArDoCoResult result = runner.run();

            // Convert and store result as JSON
            logger.log(Level.DEBUG, "Converting found TraceLinks...");
            List<SamCodeTraceLink> traceLinks = result.getSamCodeTraceLinks();
            TraceLinkConverter converter = new TraceLinkConverter();
            traceLinkJson = converter.convertListOfSamCodeTraceLinksToJSONString(traceLinks);

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
