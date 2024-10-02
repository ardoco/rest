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

@Service("sadCodeTLRService")
public class ArDoCoForSadCodeTLRService extends AbstractRunnerTLRService {

    @Autowired
    public ArDoCoForSadCodeTLRService(DatabaseAccessor databaseAccessor) {
        super(databaseAccessor, TraceLinkType.SAD_CODE);
    }

    @Override
    protected String convertResultToJSONString(ArDoCoResult result) throws JsonProcessingException {
        List<SadCodeTraceLink> traceLinks = result.getSadCodeTraceLinks();
        TraceLinkConverter converter = new TraceLinkConverter();
        return converter.convertListOfSadCodeTraceLinksToJSONString(traceLinks);
    }
}
