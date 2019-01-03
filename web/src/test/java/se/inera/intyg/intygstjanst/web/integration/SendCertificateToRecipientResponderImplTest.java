/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateSenderService;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.InternalNotificationService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType.ERROR;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType.INFO;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType.OK;

@RunWith(MockitoJUnitRunner.class)
public class SendCertificateToRecipientResponderImplTest {

    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer("19121212-1212").get();
    private static final String CERTIFICATE_ID = "Intygs-id-1234567890";
    private static final String RECIPIENT_ID = "TRANSP";

    private static final String LOGICAL_ADDRESS = "Intygstjänsten";

    @Mock
    private CertificateDao certificateDao;

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @Mock
    private CertificateSenderService certificateSenderService;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private InternalNotificationService internalNotificationService;

    @InjectMocks
    private SendCertificateToRecipientResponderInterface responder = new SendCertificateToRecipientResponderImpl();

    @Test
    public void testSendCertificateToRecipient() throws Exception {

        SendCertificateToRecipientType request = createRequest();
        request.getMottagare().setCode("FKASSA");

        Certificate certificate = mock(Certificate.class);

        doReturn(certificate)
                .when(certificateService)
                .getCertificateForCare(CERTIFICATE_ID);

        doReturn("type").when(certificate).getType();
        doReturn("unit").when(certificate).getCareUnitId();

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, request);

        assertEquals(OK, response.getResult().getResultCode());

        ArgumentCaptor<SendCertificateToRecipientType.SkickatAv> captor = ArgumentCaptor.forClass(SendCertificateToRecipientType.SkickatAv.class);
        verify(internalNotificationService, times(1))
                .notifyCareIfSentByCitizen(any(Certificate.class), captor.capture());
        assertNotNull(captor.getValue().getPersonId());
        assertNull(captor.getValue().getHosPersonal());
    }

    @Test
    public void testSendCertificateToRecipientAlreadySent() throws Exception {

        Certificate certificate = mock(Certificate.class);

        doReturn(CertificateService.SendStatus.ALREADY_SENT)
                .when(certificateService)
                .sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "FKASSA");

        doReturn(certificate)
                .when(certificateService)
                .getCertificateForCare(CERTIFICATE_ID);

        SendCertificateToRecipientType request = createRequest();
        request.getMottagare().setCode("FKASSA");
        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, request);

        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'Intygs-id-1234567890' already sent to 'FKASSA'.", response.getResult().getResultText());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, "FKASSA");
    }

    @Test
    public void testSendCertificateToRecipientInvalidCertificate() throws Exception {
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID))
                .thenThrow(new InvalidCertificateException(CERTIFICATE_ID, PERSONNUMMER));

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, response.getResult().getErrorId());
        assertEquals("Unknown certificate ID: Intygs-id-1234567890", response.getResult().getResultText());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);
    }

    @Test
    public void testSendCertificateToRecipientCertificateRevoked() throws Exception {
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID))
                .thenThrow(new CertificateRevokedException(CERTIFICATE_ID));

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'Intygs-id-1234567890' has been revoked.", response.getResult().getResultText());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);
    }

    @Test
    public void testSendCertificateToRecipientRecipientUnknown() throws Exception {
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID)).thenThrow(new RecipientUnknownException(""));

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, response.getResult().getErrorId());
        assertEquals("Unknown recipient ID: TRANSP", response.getResult().getResultText());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);
    }

    @Test
    public void testSendCertificateToRecipientServerException() throws Exception {
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID)).thenThrow(new ServerException());

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.TECHNICAL_ERROR, response.getResult().getErrorId());
        assertEquals("Certificate 'Intygs-id-1234567890' couldn't be sent to recipient", response.getResult().getResultText());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, RECIPIENT_ID);
    }

    private SendCertificateToRecipientType createRequest() {

        SendCertificateToRecipientType request = new SendCertificateToRecipientType();
        request.setPatientPersonId(new PersonId());
        request.getPatientPersonId().setExtension(PERSONNUMMER.getPersonnummer());
        request.setMottagare(new Part());
        request.getMottagare().setCode(RECIPIENT_ID);
        request.setIntygsId(new IntygId());
        request.getIntygsId().setExtension(CERTIFICATE_ID);
        SendCertificateToRecipientType.SkickatAv skickatAv = new SendCertificateToRecipientType.SkickatAv();
        skickatAv.setPersonId(new PersonId());
        request.setSkickatAv(skickatAv);
        return request;
    }
}
