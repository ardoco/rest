package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.TraceLinkConverter;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service("sadSamTLRService")
public class ArDoCoForSadSamTLRService extends AbstractRunnerTLRService {


    public ArDoCoForSadSamTLRService() {
        super(TraceLinkType.SAD_SAM);
    }

    @Override
    protected String convertResultToJsonString(ArDoCoResult result) throws JsonProcessingException {
        ImmutableList<TraceLink<SentenceEntity, ArchitectureEntity>> traceLinksImmutable = result.getAllTraceLinks();
        List<TraceLink<SentenceEntity, ArchitectureEntity>> traceLinks = traceLinksImmutable.toList();
        return  TraceLinkConverter.convertListOfSadSamTraceLinksToJsonString(traceLinks);
    }
}