package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.InconsistencyConverter;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.TraceLinkConverter;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("InconsistencyService")
public class InconsistencyService extends AbstractRunnerTLRService {

    public InconsistencyService() {
        super(TraceLinkType.SAD_SAM);
    }

    @Override
    protected ArDoCoApiResult convertResultToJsonString(ArDoCoResult result) throws JsonProcessingException {
        // traceLinks
        ImmutableList<TraceLink<SentenceEntity, ArchitectureEntity>> traceLinksImmutable = result.getAllTraceLinks();
        List<TraceLink<SentenceEntity, ArchitectureEntity>> traceLinks = traceLinksImmutable.toList();
        String traceLinksJson = TraceLinkConverter.convertListOfSadSamTraceLinksToJsonString(traceLinks);

        // inconsistencies
        ImmutableList<Inconsistency> inconsistencies = result.getAllInconsistencies();
        String inconsistenciesJson = InconsistencyConverter.convertInconsistencyToJsonString(inconsistencies);

        return new ArDoCoApiResult(traceLinksJson, inconsistenciesJson);
    }
}
