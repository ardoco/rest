/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.TraceLinkConverter;

/**
 * Service for handling trace links of type SAM_CODE in the ArDoCo API.
 * This service extends the AbstractRunnerTLRService to provide functionality
 * specific to SAM_CODE trace links.
 */
@Service("ArCoTLService")
public class ArCoTLService extends AbstractRunnerTLRService {

    /**
     * Constructor for the ArCoTLService.
     */
    public ArCoTLService() {
        super(TraceLinkType.SAM_CODE);
    }

    @Override
    protected ArDoCoApiResult convertResultToJsonString(ArdocoResult result) throws JsonProcessingException {
        ImmutableList<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> traceLinks = result.getSamCodeTraceLinks();
        String traceLinkJson = TraceLinkConverter.convertListOfSamCodeTraceLinksToJsonString(traceLinks);
        return new ArDoCoApiResult(traceLinkJson);
    }
}
