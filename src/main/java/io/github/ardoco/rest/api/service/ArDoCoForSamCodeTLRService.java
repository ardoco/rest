package io.github.ardoco.rest.api.service;

import com.github.jsonldjava.shaded.com.google.common.io.Files;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.tlr.execution.ArDoCoForSamCodeTraceabilityLinkRecovery;
import io.github.ardoco.rest.api.api_response.ResultBag;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.exception.ArdocoException;
import io.github.ardoco.rest.api.repository.DatabaseAccessor;
import io.github.ardoco.rest.api.util.TraceLinkConverter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;

@Service("samCodeTLRService")
public class ArDoCoForSamCodeTLRService extends AbstractRunnerTLRService {

    private static final Logger logger = LogManager.getLogger(ArDoCoForSamCodeTLRService.class);

    @Autowired
    public ArDoCoForSamCodeTLRService(DatabaseAccessor databaseAccessor) {
        super(databaseAccessor, TraceLinkType.SAM_CODE);
    }



//    @Override
//    public ResultBag runPipelineAndWaitForResult(ArDoCoRunner runner, String id, List<File> inputFiles)
//            throws ArdocoException, io.github.ardoco.rest.api.exception.TimeoutException {
//
//
//
//        if (!resultIsInDatabase(id) && !resultIsOnItsWay(id)) {
//            logger.log(Level.INFO, "Start new TLR of type " + this.traceLinkType + " for: " + id);
//            CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
//                    runPipelineAsync(runner, id, inputFiles)
//            );
//            asyncTasks.put(id, future);
//            return new ResultBag(id, waitForResultHelper(id));
//        } else if (resultIsInDatabase(id)) {
//            return new ResultBag(id, getResultFromDatabase(id));
//        } else {
//            return new ResultBag(id, waitForResultHelper(id));
//        }
//
//
//
//
//
////        if (!resultIsInDatabase(id)) {
////            if (!resultIsOnItsWay(id)) {
////                logger.log(Level.INFO, "Start new TLR of type " + this.traceLinkType + " for: " + id);
////                CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
////                        runPipelineAsync(runner, id, inputFiles)
////                );
////                asyncTasks.put(id, future);
////            }
////            return new ResultBag(id, waitForResultHelper(id));
////        }
////        logger.log(Level.INFO, "Result is available for " + id);
////        return new ResultBag(id, databaseAccessor.getResult(id));
//    }

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
