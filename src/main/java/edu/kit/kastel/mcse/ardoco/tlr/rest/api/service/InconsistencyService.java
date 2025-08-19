/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.InconsistencyConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.TraceLinkConverter;

import java.util.List;

@Service("InconsistencyService")
public class InconsistencyService extends AbstractRunnerTLRService {

    public InconsistencyService() {
        super(TraceLinkType.SAD_SAM);
    }

    @Override
    protected ArDoCoApiResult convertResultToJsonString(ArDoCoResult result) throws JsonProcessingException {
        // traceLinks
//        ImmutableList<TraceLink<SentenceEntity, ModelEntity>> traceLinks = result.getArchitectureTraceLinks();
        ImmutableSet<TraceLink<SentenceEntity, ModelEntity>> traceLinks = result.getTraceLinksForModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES);
        String traceLinksJson = "";
        if (!traceLinks.isEmpty()) {
            traceLinksJson = TraceLinkConverter.convertListOfSadSamTraceLinksToJsonString(traceLinks.toImmutableList());
        }

        // inconsistencies
        ImmutableList<Inconsistency> inconsistencies = result.getAllInconsistencies();
        String inconsistenciesJson = InconsistencyConverter.convertInconsistencyToJsonString(inconsistencies);

        return new ArDoCoApiResult(traceLinksJson, inconsistenciesJson);
    }
}
