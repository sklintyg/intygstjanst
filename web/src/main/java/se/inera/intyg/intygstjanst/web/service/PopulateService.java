/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
