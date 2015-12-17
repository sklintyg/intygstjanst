/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.common.support.modules.support.api.dto.TransportModelVersion;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class RecipientServiceImplTest {

    RecipientServiceImpl service;

    private static final String FK_CERTIFICATE_TYPE = "fk7263";
    private static final String TS_CERTIFICATE_TYPE_BAS = "ts-bas";
    private static final String TS_CERTIFICATE_TYPE_DIABETES = "ts-diabetes";

    private static final String FK_RECIPIENT_ID = "FK";
    private static final String FK_RECIPIENT_NAME = "Försäkringskassan";
    private static final String FK_RECIPIENT_LOGICALADDRESS = "FKORG";
    private static final String FK_RECIPIENT_CERTIFICATETYPES = "fk7263";

    private static final String TS_RECIPIENT_ID = "TS";
    private static final String TS_RECIPIENT_NAME = "Transportstyrelsen";
    private static final String TS_RECIPIENT_LOGICALADDRESS = "tsTestAddress";
    private static final String TS_RECIPIENT_CERTIFICATETYPES = "ts-bas,ts-diabetes";

    private Recipient createFkRecipient() {
        return new Recipient(FK_RECIPIENT_LOGICALADDRESS,
                FK_RECIPIENT_NAME,
                FK_RECIPIENT_ID,
                FK_RECIPIENT_CERTIFICATETYPES);

    }

    private Recipient createTsRecipient() {
        return new Recipient(TS_RECIPIENT_LOGICALADDRESS,
                TS_RECIPIENT_NAME,
                TS_RECIPIENT_ID,
                TS_RECIPIENT_CERTIFICATETYPES);

    }

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("recipient.properties"));

        service = new RecipientServiceImpl();
        service.setRecipients(properties);
        service.afterPropertiesSet();
    }

    @Test
    public void testListRecipients() {
        assertTrue("Got null!", service.listRecipients().size() > 0);
    }
    
    @Test
    public void testListRecipientsForCerttypeFK7263() throws RecipientUnknownException {
        List<Recipient> expected = Arrays.asList(createFkRecipient());
        
        assertEquals(expected, service.listRecipients(new CertificateType(FK_CERTIFICATE_TYPE)));
    }

    @Test
    public void testListRecipientsForCerttypeTS() throws RecipientUnknownException {
        List<Recipient> expected = Arrays.asList(createTsRecipient());
        
        assertEquals(expected, service.listRecipients(new CertificateType(TS_CERTIFICATE_TYPE_BAS)));
        assertEquals(expected, service.listRecipients(new CertificateType(TS_CERTIFICATE_TYPE_DIABETES)));
    }

    @Test
    public void testGetTransportModelVersionForFK7263() throws RecipientUnknownException {
        assertEquals(TransportModelVersion.LEGACY_LAKARUTLATANDE, service.getVersion("FKORG", "fk7263"));
    }

    @Test
    public void testGetTransportModelVersionForTsBas() throws RecipientUnknownException {
        assertEquals(TransportModelVersion.UTLATANDE_V1, service.getVersion("tsTestAddress", "ts-bas"));
    }
    
    @Test(expected = RecipientUnknownException.class)
    public void testUnknownRecipient() throws RecipientUnknownException {
        service.getVersion("F K", "fk7263");
    }
}
