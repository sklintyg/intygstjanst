/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.inera.intyg.intygstjanst.web.service.repo.RecipientRepoImpl;

@ExtendWith(MockitoExtension.class)
class RecipientServiceImplTest {

    private static final String FK_CERTIFICATE_TYPE = "fk7263";
    private static final String TS_CERTIFICATE_TYPE_BAS = "ts-bas";
    private static final String TS_CERTIFICATE_TYPE_DIABETES = "ts-diabetes";
    private static final String FK_RECIPIENT_ID = "FKASSA";
    private static final String FK_RECIPIENT_NAME = "Försäkringskassan";
    private static final String FK_RECIPIENT_LOGICALADDRESS = "FKORG";
    private static final String FK_RECIPIENT_CERTIFICATETYPES = "fk7263,luse,lisjp";
    private static final String TS_RECIPIENT_ID = "TRANSP";
    private static final String TS_RECIPIENT_NAME = "Transportstyrelsen";
    private static final String TS_RECIPIENT_LOGICALADDRESS = "tsTestAddress";
    private static final String TS_RECIPIENT_CERTIFICATETYPES = "ts-bas,ts-diabetes";

    @Mock
    private RecipientRepoImpl repo;

    @InjectMocks
    private RecipientServiceImpl service;

    private Recipient createFkRecipient() {
        return new RecipientBuilder()
            .setLogicalAddress(FK_RECIPIENT_LOGICALADDRESS)
            .setName(FK_RECIPIENT_NAME)
            .setId(FK_RECIPIENT_ID)
            .setCertificateTypes(FK_RECIPIENT_CERTIFICATETYPES)
            .setActive(true)
            .setTrusted(true)
            .build();
    }

    private Recipient createTsRecipient() {
        return new RecipientBuilder()
            .setLogicalAddress(TS_RECIPIENT_LOGICALADDRESS)
            .setName(TS_RECIPIENT_NAME)
            .setId(TS_RECIPIENT_ID)
            .setCertificateTypes(TS_RECIPIENT_CERTIFICATETYPES)
            .setActive(true)
            .setTrusted(true)
            .build();
    }

    private Recipient createHsvardRecipient() {
        return new RecipientBuilder()
            .setLogicalAddress("Meh2")
            .setName("Meh2")
            .setId("HSVARD")
            .setCertificateTypes("fk7263,ts-bas,ts-diabetes")
            .setActive(true)
            .setTrusted(true)
            .build();
    }

    private Recipient createInvanaRecipient() {
        return new RecipientBuilder()
            .setLogicalAddress("Meh")
            .setName("Meh")
            .setId("INVANA")
            .setCertificateTypes("fk7263,ts-bas,ts-diabetes")
            .setActive(true)
            .setTrusted(true)
            .build();
    }

    private Recipient createUntrustedRecipient() {
        return new RecipientBuilder()
            .setLogicalAddress("UNTRUSTED_ADDRESS")
            .setName("UNTRUSTED_NAME")
            .setId("UNTRUSTED_ID")
            .setCertificateTypes("fk7263,ts-bas,ts-diabetes")
            .setActive(true)
            .setTrusted(false)
            .build();
    }

    @BeforeEach
    void setup() {
        when(repo.getRecipientHsvard()).thenReturn(createHsvardRecipient());
        when(repo.getRecipientInvana()).thenReturn(createInvanaRecipient());
    }

    @Test
    void testListRecipientsForCerttypeFK7263() {
        when(repo.listRecipients()).thenReturn(Arrays.asList(createFkRecipient(), createTsRecipient()));
        List<Recipient> expected = Collections.singletonList(createFkRecipient());
        assertEquals(expected, service.listRecipients(new CertificateType(FK_CERTIFICATE_TYPE)));
    }

    @Test
    void testListRecipientsForCerttypeTS() {
        when(repo.listRecipients()).thenReturn(Arrays.asList(createFkRecipient(), createTsRecipient()));
        List<Recipient> expected = Collections.singletonList(createTsRecipient());

        assertEquals(expected, service.listRecipients(new CertificateType(TS_CERTIFICATE_TYPE_BAS)));
        assertEquals(expected, service.listRecipients(new CertificateType(TS_CERTIFICATE_TYPE_DIABETES)));
    }

    @Test
    void testListRecipientsWithUntrusted() {
        // Note: Order matters in the list.
        when(repo.listRecipients()).thenReturn(Arrays.asList(createUntrustedRecipient(), createFkRecipient(), createTsRecipient()));
        List<Recipient> expectedTS = Arrays.asList(createUntrustedRecipient(), createTsRecipient());
        List<Recipient> expectedFK = Arrays.asList(createUntrustedRecipient(), createFkRecipient());

        assertEquals(expectedFK, service.listRecipients(new CertificateType(FK_CERTIFICATE_TYPE)));
        assertEquals(expectedTS, service.listRecipients(new CertificateType(TS_CERTIFICATE_TYPE_BAS)));
        assertEquals(expectedTS, service.listRecipients(new CertificateType(TS_CERTIFICATE_TYPE_DIABETES)));
    }

}
