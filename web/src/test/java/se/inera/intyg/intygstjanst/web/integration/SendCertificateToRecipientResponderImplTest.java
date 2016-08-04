/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType.ERROR;
import static se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType.INFO;
import static se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType.OK;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateSenderService;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;

@RunWith( MockitoJUnitRunner.class )
public class SendCertificateToRecipientResponderImplTest {

    private static final Personnummer PERSONNUMMER = new Personnummer("19121212-1212");
    private static final String CERTIFICATE_ID = "Intygs-id-1234567890";
    private static final String RECIPIENT_ID = "TRANSP";

    private static final String LOGICAL_ADDRESS = "Intygstjänsten";

    @Mock
    private CertificateDao certificateDao;

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @Mock
    private CertificateSenderService certificateSenderService;

    @InjectMocks
    private SendCertificateToRecipientResponderInterface responder = new SendCertificateToRecipientResponderImpl();

    @Test
    public void testSendCertificateToRecipient() throws Exception {
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "TS")).thenReturn(CertificateService.SendStatus.OK);
        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(OK, response.getResult().getResultCode());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "TS");
    }

    @Test
    public void testSendCertificateToRecipientAlreadySent() throws Exception {
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "FK")).thenReturn(CertificateService.SendStatus.ALREADY_SENT);

        SendCertificateToRecipientType request = createRequest();
        request.getMottagare().setCode("FKASSA");
        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, request);

        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'Intygs-id-1234567890' already sent to 'FK'.", response.getResult().getResultText());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "FK");
    }

    @Test
    public void testSendCertificateToRecipientInvalidCertificate() throws Exception {
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "TS")).thenThrow(new InvalidCertificateException(CERTIFICATE_ID, PERSONNUMMER));

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, response.getResult().getErrorId());
        assertEquals("Unknown certificate ID: Intygs-id-1234567890", response.getResult().getResultText());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "TS");
    }

    @Test
    public void testSendCertificateToRecipientCertificateRevoked() throws Exception {
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "TS")).thenThrow(new CertificateRevokedException(CERTIFICATE_ID));

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'Intygs-id-1234567890' has been revoked.", response.getResult().getResultText());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "TS");
    }

    @Test
    public void testSendCertificateToRecipientRecipientUnknown() throws Exception {
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "TS")).thenThrow(new RecipientUnknownException(""));

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, response.getResult().getErrorId());
        assertEquals("Unknown recipient ID: TS", response.getResult().getResultText());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "TS");
    }

    @Test
    public void testSendCertificateToRecipientServerException() throws Exception {
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "TS")).thenThrow(new ServerException());

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.TECHNICAL_ERROR, response.getResult().getErrorId());
        assertEquals("Certificate 'Intygs-id-1234567890' couldn't be sent to recipient", response.getResult().getResultText());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "TS");
    }

    private SendCertificateToRecipientType createRequest() {

        SendCertificateToRecipientType request = new SendCertificateToRecipientType();
        request.setPatientPersonId(new PersonId());
        request.getPatientPersonId().setExtension(PERSONNUMMER.getPersonnummer());
        request.setMottagare(new Part());
        request.getMottagare().setCode(RECIPIENT_ID);
        request.setIntygsId(new IntygId());
        request.getIntygsId().setExtension(CERTIFICATE_ID);

        return request;
    }
}
