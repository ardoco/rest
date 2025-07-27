package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.ArDoCoApiResult;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.TraceLinkConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("sadSamCodeTLRService")
public class ArDoCoForSadSamCodeTLRService extends AbstractRunnerTLRService{

    public ArDoCoForSadSamCodeTLRService() {
        super(TraceLinkType.SAD_SAM_CODE);
    }

    @Override
    protected ArDoCoApiResult convertResultToJsonString(ArDoCoResult result) throws JsonProcessingException {
        List<TraceLink<SentenceEntity, CodeCompilationUnit>> sadCodeTraceLinks = result.getSadCodeTraceLinks();
        String traceLinksJson = TraceLinkConverter.convertListOfSadCodeTraceLinksToJsonString(sadCodeTraceLinks);
        return new ArDoCoApiResult(traceLinksJson);
    }
}
