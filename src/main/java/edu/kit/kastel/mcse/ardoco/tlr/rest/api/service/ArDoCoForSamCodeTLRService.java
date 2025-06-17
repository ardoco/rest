package edu.kit.kastel.mcse.ardoco.tlr.rest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.api_response.TraceLinkType;
import edu.kit.kastel.mcse.ardoco.tlr.rest.api.converter.TraceLinkConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("samCodeTLRService")
public class ArDoCoForSamCodeTLRService extends AbstractRunnerTLRService {

    public ArDoCoForSamCodeTLRService() {
        super(TraceLinkType.SAM_CODE);
    }

    @Override
    protected String convertResultToJsonString(ArDoCoResult result) throws JsonProcessingException {
        List<TraceLink<ArchitectureEntity, CodeCompilationUnit>> traceLinks = result.getSamCodeTraceLinks();
        return TraceLinkConverter.convertListOfSamCodeTraceLinksToJsonString(traceLinks);
    }
}
