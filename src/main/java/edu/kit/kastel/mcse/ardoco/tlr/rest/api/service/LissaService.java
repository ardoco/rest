/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.llm.util.EnvironmentProvider;
import edu.kit.kastel.mcse.ardoco.llm.util.MapEnvironment;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.LissaTraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.exception.ArdocoException;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.lissa.LissaEvaluationConfigFactory;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository.CurrentlyRunningRequestsRepository;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.repository.DatabaseAccessor;
import edu.kit.kastel.sdq.lissa.ratlr.Evaluation;
import edu.kit.kastel.sdq.lissa.ratlr.configuration.EvaluationConfiguration;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.Artifact.ArtifactType;
import edu.kit.kastel.sdq.lissa.ratlr.knowledge.TraceLink;

@Service("LissaService")
public class LissaService extends AbstractService {

    private static final Logger logger = LoggerFactory.getLogger(LissaService.class);
    private static final String ERROR_PREFIX = "Error: ";

    // Serializes all LiSSA evaluation runs process-wide, since LiSSA's Environment/CacheManager
    // are static singletons, not request-scoped.
    private static final ReentrantLock EXECUTION_LOCK = new ReentrantLock();

    @Autowired
    private CurrentlyRunningRequestsRepository currentlyRunningRequestsRepository;

    @Autowired
    private DatabaseAccessor databaseAccessor;

    @Autowired
    private LissaEvaluationConfigFactory configFactory;

    private final ObjectMapper mapper = new ObjectMapper();

    public Optional<ArDoCoApiResult> runPipeline(List<File> sourceFiles, ArtifactType sourceType, List<File> targetFiles, ArtifactType targetType, String id,
            @Nullable String additionalConfigsJson, @Nullable String apiKeysJson) {
        EnvironmentProvider environment = parseEnvironment(apiKeysJson);
        if (!resultIsInDatabase(id) && !resultIsOnItsWay(id)) {
            logger.info("Start new TLR of type {} for {}", TraceLinkType.LISSA, id);
            CompletableFuture<ArDoCoApiResult> future = CompletableFuture.supplyAsync(() -> runPipelineAsync(sourceFiles, sourceType, targetFiles, targetType,
                    id, additionalConfigsJson, environment));
            currentlyRunningRequestsRepository.addRequest(id, future);
        } else if (resultIsInDatabase(id)) {
            return Optional.of(getResultFromDatabase(id));
        }
        return Optional.empty();
    }

    private ArDoCoApiResult runPipelineAsync(List<File> sourceFiles, ArtifactType sourceType, List<File> targetFiles, ArtifactType targetType, String id,
            @Nullable String additionalConfigsJson, EnvironmentProvider environment) {
        ArDoCoApiResult traceLinkJson;
        Path workingDir = null;
        try {
            workingDir = Files.createTempDirectory("lissa-" + id);
            EvaluationConfiguration config = configFactory.build(sourceFiles, sourceType, targetFiles, targetType, workingDir, additionalConfigsJson);
            logger.info("Using the following config: {}", config);

            //TODO Remove log
            logger.info("Using the following env: {}", environment);
            logger.info(environment.getenv("CACHE_HIERARCHY"));

            Set<TraceLink> traceLinks;
            logger.info("Running LiSSA evaluation in-process...");
            traceLinks = new Evaluation(config, environment).run();
            logger.info("LiSSA evaluation results: {}", traceLinks);
            List<LissaTraceLink> mapped = traceLinks.stream().map(tl -> new LissaTraceLink(tl.sourceId(), tl.targetId())).toList();
            traceLinkJson = new ArDoCoApiResult(mapper.writeValueAsString(mapped));
            databaseAccessor.saveResult(id, traceLinkJson.buildJsonString());

        } catch (Exception e) {
            String message = String.format("Error occurred while running LiSSA asynchronously for ID %s : %s", id, e.getMessage());
            logger.error(message, e);
            databaseAccessor.saveResult(id, ERROR_PREFIX + message);
            throw new ArdocoException(message, e);
        } finally {
            currentlyRunningRequestsRepository.removeRequest(id);
            for (File file : sourceFiles)
                file.delete();
            for (File file : targetFiles)
                file.delete();
            deleteQuietly(workingDir);
        }
        return traceLinkJson;
    }

    private void deleteQuietly(Path dir) {
        if (dir == null)
            return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception e) {
            logger.warn("Failed to clean up LiSSA working directory {}: {}", dir, e.getMessage());
        }
    }

    /**
     * Parses a JSON object mapping environment variable names to string values, e.g. {@code {"OPENAI_API_KEY": "sk-..."}}.
     *
     * @param input The JSON object, or null/blank for no variables.
     * @return A map environment of the key-value pairs.
     * @throws IllegalArgumentException if the input is not a JSON object with string values.
     */
    private EnvironmentProvider parseEnvironment(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return new MapEnvironment(Map.of());
        }
        try {
            Map<String, String> values = mapper.readValue(input, new TypeReference<Map<String, String>>() {
            });
            return new MapEnvironment(values == null ? Map.of() : values);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("apiKeys must be a JSON object mapping variable names to string values, e.g. {\"OPENAI_API_KEY\": \"sk-...\"}",
                    e);
        }
    }
}
