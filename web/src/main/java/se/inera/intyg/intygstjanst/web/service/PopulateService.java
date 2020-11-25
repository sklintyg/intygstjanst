package se.inera.intyg.intygstjanst.web.service;

import java.util.List;
import java.util.Map;

/**
 * Service with the purpose of supplying the population solution with the business logic required to do the actual processing
 */
public interface PopulateService {

    /**
     * @param batchSize The number of ids to process in each batch
     * @return Map of the jobs with their corresponding list of ids
     */
    Map<String, List<String>> getListsOfIdsToProcess(Integer batchSize);

    /**
     * @param jobName Name of the job the id belongs to
     * @param id The id to process, probably a certificateId
     */
    void processId(String jobName, String id);
}
