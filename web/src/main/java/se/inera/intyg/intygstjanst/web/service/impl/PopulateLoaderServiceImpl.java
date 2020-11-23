/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.intygstjanst.persistence.model.dao.PopulateProcessedRepository;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.PopulateLoaderService;
import se.inera.intyg.intygstjanst.web.service.PopulateService;

@Service
public class PopulateLoaderServiceImpl implements PopulateLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(PopulateLoaderServiceImpl.class);
    private static final String POPULATE_SHEDLOCK_JOB_NAME = "PopulateLoaderService.run";
    private static final int NR_OF_BATCHES = 32;

    @Autowired
    MonitoringLogService monitoringLogService;

    @Autowired
    PopulateService service;

    @Autowired
    PopulateProcessedRepository processedRepository;

    @Autowired
    @Qualifier("jmsPopulateTemplate")
    private JmsTemplate jmsTemplate;

    @Value("${populate.loader.batchsize:10000}")
    private Integer batchSize;

    @Value("${populate.loader.queueName}")
    private String internalPopulateLoaderQueue;

    @Override
    @Scheduled(cron = "${populate.loader.cron:-}")
    @SchedulerLock(name = POPULATE_SHEDLOCK_JOB_NAME)
    public void populate() {
        if (countPendingMessages() == 0) {
            LOG.info("No IDs on queue: Starting populate loader run with batch size: " + batchSize + " and splitting into "
                + NR_OF_BATCHES + " batches.");
            var idLists = service.getListsOfIdsToProcess(batchSize);
            for (Map.Entry<String, List<String>> entry : idLists.entrySet()) {
                LOG.debug("Putting ids on queue: " + String.join(",", entry.getValue()));
                if (idLists.size() > 0) {
                    var size = idLists.size() < NR_OF_BATCHES ? NR_OF_BATCHES : (idLists.size() / NR_OF_BATCHES);
                    chunked(entry.getValue().stream(), size).forEach(id -> putIdsOnQueue(entry.getKey(), id));
                }
            }
        }
    }

    private static <T> Stream<List<T>> chunked(Stream<T> stream, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);

        return stream.collect(Collectors.groupingBy(x -> index.getAndIncrement() / chunkSize))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue);
    }

    private int countPendingMessages() {
        Integer totalPendingMessages = this.jmsTemplate
            .browse(internalPopulateLoaderQueue, (session, browser) -> Collections.list(browser.getEnumeration()).size());

        return totalPendingMessages == null ? 0 : totalPendingMessages;
    }

    @Transactional
    public void putIdsOnQueue(String jobName, List<String> idList) {
        processedRepository.saveBatch(jobName, idList);
        var success = send(session -> {
            var message = session.createObjectMessage((ArrayList<String>) idList);
            message.setStringProperty("name", jobName);
            return message;
        });
        if (!success) {
            throw new RuntimeException("Could not send message to queue");
        }
        LOG.debug("Put ids on queue: " + String.join(",", idList) + " : " + success);
    }

    private boolean send(final MessageCreator messageCreator) {
        try {
            jmsTemplate.send(internalPopulateLoaderQueue, messageCreator);
            return true;
        } catch (JmsException e) {
            LOG.error("Failure sending ids to certificate event loader queue.", e);
            return false;
        }
    }


}
