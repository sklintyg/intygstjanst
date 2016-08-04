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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.exception.SubsystemCallException;
import se.inera.intyg.intygstjanst.web.service.*;

@RunWith(MockitoJUnitRunner.class)
public class RevokeMedicalCertificateResponderImplTest {

    protected static final String CERTIFICATE_ID = "intygs-id-1234567890";
    protected static final Personnummer PERSONNUMMER = new Personnummer("19121212-1212");
    protected static final String TARGET = "FK";

    protected static final AttributedURIType ADDRESS = new AttributedURIType();

    @Mock
    protected CertificateSenderService certificateSenderService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    protected CertificateService certificateService;

    @Mock
    protected StatisticsService statisticsService;

    @Mock
    protected SjukfallCertificateService sjukfallCertificateService;

    @InjectMocks
    protected RevokeMedicalCertificateResponderInterface responder = new RevokeMedicalCertificateResponderImpl();

    protected RevokeMedicalCertificateRequestType revokeRequest() throws Exception {
        // read request from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RevokeMedicalCertificateRequestType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<RevokeMedicalCertificateRequestType> request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("revoke-medical-certificate/revoke-medical-certificate-request.xml").getInputStream()), RevokeMedicalCertificateRequestType.class);
        return request.getValue();
    }

    @Test
    public void testRevokeCertificateWhichWasAlreadySentToForsakringskassan() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID);
        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(TARGET, CertificateState.SENT, new LocalDateTime());
        certificate.setStates(Collections.singletonList(historyEntry));

        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        verify(certificateSenderService).sendCertificateRevocation(certificate, TARGET, revokeRequest().getRevoke());

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
        Mockito.verify(statisticsService, Mockito.only()).revoked(certificate);
        Mockito.verify(sjukfallCertificateService, Mockito.only()).revoked(certificate);
    }

    @Test
    public void testRevokeCertificateWithForsakringskassanReturningError() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID);
        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(TARGET, CertificateState.SENT, new LocalDateTime());
        certificate.setStates(Collections.singletonList(historyEntry));

        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        doThrow(new SubsystemCallException(TARGET)).when(certificateSenderService).sendCertificateRevocation(certificate, TARGET,
                revokeRequest().getRevoke());

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());
        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        Mockito.verifyZeroInteractions(statisticsService);
        Mockito.verifyZeroInteractions(sjukfallCertificateService);
    }

    @Test
    public void testRevokeCertificateWhichWasNotSentToForsakringskassan() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID);

        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
        Mockito.verify(statisticsService, Mockito.only()).revoked(certificate);
        Mockito.verify(sjukfallCertificateService, Mockito.only()).revoked(certificate);
    }

    @Test
    public void testRevokeUnknownCertificate() throws Exception {
        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenThrow(new InvalidCertificateException(CERTIFICATE_ID, PERSONNUMMER));

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals("No certificate 'intygs-id-1234567890' found to revoke for patient '" + PERSONNUMMER.getPnrHash() + "'.", response.getResult().getErrorText());
        Mockito.verifyZeroInteractions(statisticsService);
        Mockito.verifyZeroInteractions(sjukfallCertificateService);
    }

    @Test
    public void testRevokeAlreadyRevokedCertificate() throws Exception {
        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenThrow(new CertificateRevokedException(CERTIFICATE_ID));

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        assertEquals(ResultCodeEnum.INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'intygs-id-1234567890' is already revoked.", response.getResult().getInfoText());
        Mockito.verifyZeroInteractions(statisticsService);
        Mockito.verifyZeroInteractions(sjukfallCertificateService);
    }

    @Test
    public void testRevokeMedicalCertificateValidationError() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getLakarutlatande().setPatient(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No Patient element found!", response.getResult().getErrorText());
        Mockito.verifyZeroInteractions(statisticsService);
        Mockito.verifyZeroInteractions(sjukfallCertificateService);
        Mockito.verifyZeroInteractions(certificateService);
    }
}
