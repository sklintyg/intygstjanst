/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

@Repository
@EnableScheduling
public class RecipientRepoImpl implements RecipientRepo {

    private static final Logger LOG = LoggerFactory.getLogger(RecipientRepoImpl.class);

    private static final String INVANA_ID = "INVANA";
    private static final String HSVARD_ID = "HSVARD";
    private static final String FKASSA_ID = "FKASSA";
    private static final String TRANSP_ID = "TRANSP";

    protected Map<Recipient, Set<CertificateType>> certificateTypesForRecipient;
    private Map<String, Recipient> recipientMap;

    @Value("${recipient.file}")
    private String recipientFile;


    /**
     * Initial setup of the in-memory database.
     */
    @PostConstruct
    public void init() {
        recipientMap = new HashMap<>();
        certificateTypesForRecipient = new HashMap<>();
        update();
    }

    @Override
    public Recipient getRecipientForLogicalAddress(String logicalAddress) throws RecipientUnknownException {
        return recipientMap.values().stream()
            .filter(r -> r.getLogicalAddress().equals(logicalAddress))
            .findAny()
            .orElseThrow(
                () -> new RecipientUnknownException(String.format("No recipient found for logical address: %s", logicalAddress)));
    }

    @Override
    public Recipient getRecipient(String recipientId) throws RecipientUnknownException {
        return Optional.ofNullable(recipientMap.get(recipientId))
            .orElseThrow(() -> new RecipientUnknownException(String.format("No recipient found for recipient id: %s", recipientId)));
    }

    @Override
    public List<Recipient> listRecipients() {
        return Lists.newArrayList(recipientMap.values());
    }

    @Override
    public Recipient getRecipientFkassa() {
        return recipientMap.get(FKASSA_ID);
    }

    @Override
    public Recipient getRecipientInvana() {
        return recipientMap.get(INVANA_ID);
    }

    @Override
    public Recipient getRecipientHsvard() {
        return recipientMap.get(HSVARD_ID);
    }

    @Override
    public Recipient getRecipientTransp() {
        return recipientMap.get(TRANSP_ID);
    }

    @VisibleForTesting
    protected void clear() {
        recipientMap.clear();
    }

    @Scheduled(cron = "${recipients.update.cron}")
    public void update() {
        LOG.info("Performing scheduled recipient update.");
        ObjectMapper objectMapper = new ObjectMapper();

        try (FileInputStream fis = new FileInputStream(recipientFile)) {
            Recipient[] recipientArray = objectMapper
                .readValue(fis, Recipient[].class);

            Stream.of(recipientArray)
                .filter(r -> !recipientMap.containsKey(r.getId()) || !recipientMap.get(r.getId()).equals(r))
                .forEach(r -> {
                    LOG.info("Adding {} to recipient repo.", r.getId());
                    recipientMap.put(r.getId(), r);
                });

            if (!ensureRequiredRecipients()) {
                throw new ServerException(String.format(
                    "One of the required recipients: %s, %s and %s not found!", FKASSA_ID, HSVARD_ID, INVANA_ID));
            }

        } catch (IOException ie) {
            LOG.error("Scheduled recipient update failed with error {}", ie.getMessage());
            if (recipientMap.isEmpty()) {
                throw new ServerException("No recipients loaded at startup, aborting!");
            }
        }
    }

    private boolean ensureRequiredRecipients() {
        return recipientMap.containsKey(FKASSA_ID) && recipientMap.containsKey(HSVARD_ID)
            && recipientMap.containsKey(INVANA_ID) && recipientMap.containsKey(TRANSP_ID);
    }

}
