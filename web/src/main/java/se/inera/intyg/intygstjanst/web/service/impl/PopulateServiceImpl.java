package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.PopulateService;

@Service
public class PopulateServiceImpl implements PopulateService {

    @Override
    public Map<String, List<String>> getListsOfIdsToProcess(Integer batchSize) {
        return null;
    }

    @Override
    public void processIds(String jobName, String id) {

    }
}
