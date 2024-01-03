/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.intygstjanst.persistence.model.dao.PopulateFailures;
import se.inera.intyg.intygstjanst.persistence.model.dao.PopulateFailuresRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.PopulateProcessedRepository;

/**
 * The part of the population solution that consumes batches from the queue and triggers the actual processing
 */
@Service
public class PopulateConsumerService implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(PopulateConsumerService.class);

    @Autowired
    PopulateService service;

    @Autowired
    PopulateFailuresRepository failuresRepository;

    @Autowired
    PopulateProcessedRepository processedRepository;

    @Override
    @JmsListener(destination = "${populate.loader.queueName}")
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objMessage = (ObjectMessage) message;
                var jobName = objMessage.getStringProperty("name");
                var list = (ArrayList<String>) objMessage.getObject();
                processIds(jobName, list);
            }
        } catch (Exception e) {
            LOG.error("Could not process populate loader message: {}", e.getMessage());
        }
    }

    public void processIds(String jobName, List<String> idList) {
        LOG.debug("Processing job: " + jobName);
        List<String> failedIds = new ArrayList<>();

        idList.forEach(id -> {
            try {
                LOG.debug("Processing id: " + id);
                var processedId = processedRepository.findByPopulateIdAndJobName(id, jobName);
                if (processedId.isPresent()) {
                    processId(jobName, id);
                }
            } catch (Exception e) {
                processFailedId(jobName, id, e, failedIds);
            }
        });

        if (failedIds.size() < idList.size()) {
            LOG.info("Populate Consumer successfully finished processing {} ids out of batch with size {} for job {}",
                (idList.size() - failedIds.size()), idList.size(), jobName);
        }

        if (failedIds.size() > 0) {
            LOG.warn("Populate Consumer failed during processing of {} ids out of batch with size {} for job {}. These failed: {}",
                failedIds.size(), idList.size(), jobName, String.join(", ", failedIds));
        }
    }

    @Transactional
    public void processFailedId(String jobName, String id, Exception e, List<String> failed) {
        addToPopulateFailures(jobName, id, e);
        processedRepository.deleteByPopulateIdAndJobName(id, jobName);
        failed.add(id);
    }

    @Transactional
    public void processId(String jobName, String id) {
        service.processId(jobName, id);
        processedRepository.deleteByPopulateIdAndJobName(id, jobName);
        LOG.debug("Finished processing " + id);
    }

    private void addToPopulateFailures(String jobName, String id, Exception e) {
        var populateFailure = new PopulateFailures();
        populateFailure.setJobName(jobName);
        populateFailure.setPopulateId(id);
        populateFailure.setException(e.toString());
        populateFailure.setTimestamp(LocalDateTime.now());
        failuresRepository.save(populateFailure);
    }
}
