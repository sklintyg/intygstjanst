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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.repo.RecipientRepoImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecipientServiceImplTest {

    @Mock
    RecipientRepoImpl repo;

    @InjectMocks
    RecipientServiceImpl service;

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

    private Recipient createFkRecipient() {
        return new Recipient(FK_RECIPIENT_LOGICALADDRESS,
                FK_RECIPIENT_NAME,
                FK_RECIPIENT_ID,
                FK_RECIPIENT_CERTIFICATETYPES,
                true);
    }

    private Recipient createTsRecipient() {
        return new Recipient(TS_RECIPIENT_LOGICALADDRESS,
                TS_RECIPIENT_NAME,
                TS_RECIPIENT_ID,
                TS_RECIPIENT_CERTIFICATETYPES,
                true);
    }

    private Recipient createHsvardRecipient() {
        return new Recipient("Meh2",
                "Meh2",
                "HSVARD",
                "fk7263,ts-bas,ts-diabetes",
                true);
    }

    private Recipient createInvanaRecipient() {
        return new Recipient("Meh",
                "Meh",
                "INVANA",
                "fk7263,ts-bas,ts-diabetes",
                true);
    }


    @Before
    public void setup() throws RecipientUnknownException {
        when(repo.getRecipientForLogicalAddress(Mockito.eq(FK_RECIPIENT_LOGICALADDRESS)))
                .thenReturn(createFkRecipient());
        when(repo.getRecipientForLogicalAddress(Mockito.eq(TS_RECIPIENT_LOGICALADDRESS)))
                .thenReturn(createTsRecipient());
        when(repo.getRecipientForLogicalAddress(Mockito.startsWith("ERROR")))
                .thenThrow(RecipientUnknownException.class);
        when(repo.getRecipientHsvard()).thenReturn(createHsvardRecipient());
        when(repo.getRecipientInvana()).thenReturn(createInvanaRecipient());

    }

    @Test
    public void testListRecipientsForCerttypeFK7263() throws RecipientUnknownException {
        when(repo.listRecipients()).thenReturn(Arrays.asList(createFkRecipient(), createTsRecipient()));
        List<Recipient> expected = Arrays.asList(createFkRecipient());
        assertEquals(expected, service.listRecipients(new CertificateType(FK_CERTIFICATE_TYPE)));
    }

    @Test
    public void testListRecipientsForCerttypeTS() throws RecipientUnknownException {
        when(repo.listRecipients()).thenReturn(Arrays.asList(createFkRecipient(), createTsRecipient()));
        List<Recipient> expected = Arrays.asList(createTsRecipient());

        assertEquals(expected, service.listRecipients(new CertificateType(TS_CERTIFICATE_TYPE_BAS)));
        assertEquals(expected, service.listRecipients(new CertificateType(TS_CERTIFICATE_TYPE_DIABETES)));
    }

}
