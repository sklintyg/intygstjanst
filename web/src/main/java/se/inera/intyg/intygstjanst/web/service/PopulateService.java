package se.inera.intyg.intygstjanst.web.service;

import java.util.List;
import java.util.Map;

public interface PopulateService {

    Map<String, List<String>> getListsOfIdsToProcess(Integer batchSize);

    void processIds(String jobName, String id);
}
