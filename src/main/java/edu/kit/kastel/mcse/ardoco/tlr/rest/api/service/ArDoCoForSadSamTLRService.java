package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.TraceLinkConverter;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.stereotype.Service;

/**
 * Service for handling trace links of type SAD_SAM in the ArDoCo API.
 * This service extends the AbstractRunnerTLRService to provide functionality
 * specific to SAD_SAM trace links.
 */
@Service("sadSamTLRService")
public class ArDoCoForSadSamTLRService extends AbstractRunnerTLRService {

    /** Constructor for the ArDoCoForSadSamTLRService. */
    public ArDoCoForSadSamTLRService() {
        super(TraceLinkType.SAD_SAM);
    }

    @Override
    protected ArDoCoApiResult convertResultToJsonString(ArDoCoResult result) throws JsonProcessingException {
        ImmutableList<TraceLink<SentenceEntity, ModelEntity>> traceLinks = result.getArchitectureTraceLinks();
        String traceLinksJson = TraceLinkConverter.convertListOfSadSamTraceLinksToJsonString(traceLinks);
        return new ArDoCoApiResult(traceLinksJson);
    }
}