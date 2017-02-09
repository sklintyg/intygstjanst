/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;

import se.inera.intyg.common.support.modules.support.api.dto.TransportModelVersion;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.*;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;

public class RecipientServiceImpl implements RecipientService, InitializingBean {

    private List<Recipient> recipientList;
    private Map<Recipient, Set<CertificateType>> certificateTypesForRecipient;
    private Properties recipients;

    /**
     * Keeps track of the TransportModelVersion supported by a certain combination of Logical Address and
     * CertificateType.
     */
    private Map<RecipientCertificateType, TransportModelVersion> supportedTransportModelVersion;

    public RecipientServiceImpl() {
        recipientList = new ArrayList<>();
        certificateTypesForRecipient = new HashMap<>();
        supportedTransportModelVersion = new HashMap<>();
    }

    public Properties getRecipients() {
        return recipients;
    }

    public void setRecipients(Properties recipients) {
        this.recipients = recipients;
    }

    @Override
    public Recipient getRecipientForLogicalAddress(String logicalAddress) throws RecipientUnknownException {
        return recipientList.stream()
                .filter(r -> r.getLogicalAddress().equals(logicalAddress))
                .findFirst()
                .orElseThrow(
                        () -> new RecipientUnknownException(String.format("No recipient found for logical address: %s", logicalAddress)));
    }

    @Override
    public Recipient getRecipient(String recipientId) throws RecipientUnknownException {
        return recipientList.stream()
                .filter(r -> r.getId().equals(recipientId))
                .findFirst()
                .orElseThrow(() -> new RecipientUnknownException(String.format("No recipient found for recipient id: %s", recipientId)));
    }

    @Override
    public List<Recipient> listRecipients() {
        return recipientList;
    }

    @Override
    public List<Recipient> listRecipients(CertificateType certificateType) {
        return recipientList.stream()
                .filter(r -> r.getCertificateTypes().contains(certificateType.getCertificateTypeId()))
                .collect(Collectors.toList());
    }

    @Override
    public Set<CertificateType> listCertificateTypes(Recipient recipient) {
        return certificateTypesForRecipient.get(recipient);
    }

    @Override
    public TransportModelVersion getVersion(String logicalAddress, String certificateType) throws RecipientUnknownException {
        String recipientId = getRecipientForLogicalAddress(logicalAddress).getId();
        return supportedTransportModelVersion.get(new RecipientCertificateType(recipientId, certificateType));
    }

    @Override
    public void afterPropertiesSet() {
        HashMap<String, RecipientBuilder> recipientMap = new HashMap<>();

        for (String key : recipients.stringPropertyNames()) {
            String value = recipients.getProperty(key);
            String[] keyParts = key.split("\\.");
            switch (keyParts[0]) {
            case "recipient":
                String id = keyParts[1];
                if (recipientMap.get(id) == null) {
                    recipientMap.put(id, new RecipientBuilder().setId(id));
                }
                if ("name".equals(keyParts[2])) {
                    recipientMap.get(id).setName(value);
                } else if ("logicalAddress".equals(keyParts[2])) {
                    recipientMap.get(id).setLogicalAddress(value);
                } else if ("certificateType".equals(keyParts[2])) {
                    recipientMap.get(id).setCertificateTypes(value);
                }
                break;
            case "recipient-transport-model-version":
                String recipientId = keyParts[1];
                String certType = keyParts[2];
                supportedTransportModelVersion.put(new RecipientCertificateType(recipientId, certType),
                        TransportModelVersion.valueOf(value));
                break;
            default:
            }
        }

        for (RecipientBuilder builder : recipientMap.values()) {
            recipientList.add(builder.build());
        }
    }
}
