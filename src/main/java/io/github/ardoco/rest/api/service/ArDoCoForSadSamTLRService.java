package io.github.ardoco.rest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import io.github.ardoco.rest.api.api_response.TraceLinkType;
import io.github.ardoco.rest.api.repository.DatabaseAccessor;
import io.github.ardoco.rest.api.util.TraceLinkConverter;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service("sadSamTLRService")
public class ArDoCoForSadSamTLRService extends AbstractRunnerTLRService {

    @Autowired
    public ArDoCoForSadSamTLRService(DatabaseAccessor databaseAccessor) {
        super(databaseAccessor, TraceLinkType.SAD_SAM);
    }

    @Override
    protected String convertResultToJSONString(ArDoCoResult result) throws JsonProcessingException {
        ImmutableList<SadSamTraceLink> traceLinksImmutable = result.getAllTraceLinks();
        List<SadSamTraceLink> traceLinks = traceLinksImmutable.toSortedList(Comparator.comparingInt(SadSamTraceLink::getSentenceNumber));
        return  TraceLinkConverter.convertListOfSadSamTraceLinksToJSONString(traceLinks);
    }
}