package io.github.ardoco.rest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.repository.DatabaseAccessor;
import io.github.ardoco.rest.api.util.TraceLinkConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("sadSamCodeTLRService")
public class ArDoCoForSadSamCodeTLRService extends AbstractRunnerTLRService{

    @Autowired
    public ArDoCoForSadSamCodeTLRService(DatabaseAccessor databaseAccessor) {
        super(databaseAccessor, TraceLinkType.SAD_SAM_CODE);
    }

    @Override
    protected String convertResultToJSONString(ArDoCoResult result) throws JsonProcessingException {
        List<SadCodeTraceLink> sadCodeTraceLinks = result.getSadCodeTraceLinks();
        return TraceLinkConverter.convertListOfSadCodeTraceLinksToJSONString(sadCodeTraceLinks);
    }
}
