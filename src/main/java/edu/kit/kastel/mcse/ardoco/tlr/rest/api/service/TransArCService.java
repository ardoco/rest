/* Licensed under MIT 2025. */
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
 * Service for handling trace links of type SAD_CODE in the ArDoCo API.
 * This service extends the AbstractRunnerTLRService to provide functionality
 * specific to SAD_CODE trace links.
 */
@Service("transArCService")
public class TransArCService extends AbstractRunnerTLRService {

    /**
     * Constructor for the TransArCService.
     */
    public TransArCService() {
        super(TraceLinkType.SAD_SAM_CODE);
    }

    @Override
    protected ArDoCoApiResult convertResultToJsonString(ArDoCoResult result) throws JsonProcessingException {
        ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks = result.getSadCodeTraceLinks();
        String traceLinksJson = TraceLinkConverter.convertListOfSadCodeTraceLinksToJsonString(traceLinks);
        return new ArDoCoApiResult(traceLinksJson);
    }
}
