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
package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import java.time.LocalDateTime;
import java.util.Collections;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.service.CertificateSenderService;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.SjukfallCertificateService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.schemas.contract.Personnummer;

@RunWith(MockitoJUnitRunner.class)
public class RevokeMedicalCertificateResponderImplTest {

    private static final String CERTIFICATE_ID = "intygs-id-1234567890";
    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer("19121212-1212").get();
    private static final String FKASSA = "FKASSA";
    private static final String TRANSP = "TRANSP";

    private static final AttributedURIType ADDRESS = new AttributedURIType();

    @Mock
    private CertificateSenderService certificateSenderService;

    @Mock
    private MonitoringLogService monitoringLogService;

    @Mock
    private CertificateService certificateService;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private SjukfallCertificateService sjukfallCertificateService;

    @Mock
    private RecipientService recipientService;

    @InjectMocks
    private RevokeMedicalCertificateResponderInterface responder = new RevokeMedicalCertificateResponderImpl();

    private RevokeMedicalCertificateRequestType cachedRevokeRequest;

    private RevokeMedicalCertificateRequestType revokeRequest() throws Exception {
        if (cachedRevokeRequest == null) {
            JAXBContext jaxbContext = JAXBContext.newInstance(RevokeMedicalCertificateRequestType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<RevokeMedicalCertificateRequestType> request = unmarshaller.unmarshal(
                new StreamSource(
                    new ClassPathResource("revoke-medical-certificate/revoke-medical-certificate-request.xml").getInputStream()),
                RevokeMedicalCertificateRequestType.class);
            cachedRevokeRequest = request.getValue();
        }
        return cachedRevokeRequest;
    }

    private Recipient createRecipientForForsakringskassan() {
        return new Recipient("logicalAddress", FKASSA, FKASSA, "HUVUDMOTTAGARE",
            "certificateTypes", true, true);
    }

    @Test
    public void testRevokeCertificateWhichWasAlreadySentToTransportstyrelsen() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID);
        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(TRANSP, CertificateState.SENT, LocalDateTime.now());
        certificate.setStates(Collections.singletonList(historyEntry));

        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        when(recipientService.getPrimaryRecipientFkassa()).thenReturn(createRecipientForForsakringskassan());

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        verify(certificateSenderService).sendCertificateRevocation(certificate, TRANSP, revokeRequest().getRevoke());

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
        Mockito.verify(certificateService, Mockito.times(1)).revokeCertificateForStatistics(certificate);
        Mockito.verify(sjukfallCertificateService, Mockito.only()).revoked(certificate);
    }

    @Test
    public void testRevokeCertificateWhichWasNotSentToTransportstyrelsen() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID);

        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        verify(certificateSenderService, Mockito.never()).sendCertificateRevocation(certificate, TRANSP, revokeRequest().getRevoke());

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
        Mockito.verify(certificateService, times(1)).revokeCertificateForStatistics(certificate);
        Mockito.verify(sjukfallCertificateService, Mockito.only()).revoked(certificate);
    }

    @Test
    public void testRevokeCertificateIsNotSentToForsakringskassan() throws Exception {

        Certificate certificate = new Certificate(CERTIFICATE_ID);
        CertificateStateHistoryEntry historyEntry = new CertificateStateHistoryEntry(FKASSA, CertificateState.SENT, LocalDateTime.now());
        certificate.setStates(Collections.singletonList(historyEntry));

        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(certificate);
        when(recipientService.getPrimaryRecipientFkassa()).thenReturn(createRecipientForForsakringskassan());

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        verify(certificateSenderService, Mockito.never()).sendCertificateRevocation(certificate, FKASSA, revokeRequest().getRevoke());

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
        Mockito.verify(certificateService, Mockito.times(1)).revokeCertificateForStatistics(certificate);
        Mockito.verify(sjukfallCertificateService, Mockito.only()).revoked(certificate);
    }

    @Test
    public void testRevokeUnknownCertificate() throws Exception {
        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID))
            .thenThrow(new InvalidCertificateException(CERTIFICATE_ID, PERSONNUMMER));

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals("No certificate 'intygs-id-1234567890' found to revoke for patient '" + PERSONNUMMER.getPersonnummerHash() + "'.",
            response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
    }

    @Test
    public void testRevokeAlreadyRevokedCertificate() throws Exception {
        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenThrow(new CertificateRevokedException(CERTIFICATE_ID));

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, revokeRequest());

        assertEquals(ResultCodeEnum.INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'intygs-id-1234567890' is already revoked.", response.getResult().getInfoText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknadPatient() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getLakarutlatande().setPatient(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No Patient element found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknatIntygId() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getLakarutlatande().setLakarutlatandeId(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No Lakarutlatande Id found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateTomtIntygId() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getLakarutlatande().setLakarutlatandeId("");
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No Lakarutlatande Id found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateFelaktigPatientIdKod() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getLakarutlatande().getPatient().getPersonId().setRoot("invalid");
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong o.i.d. for Patient Id! Should be 1.2.752.129.2.1.3.1 or 1.2.752.129.2.1.3.3",
            response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateFelaktigPatientId() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getLakarutlatande().getPatient().getPersonId().setExtension("invalid");
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong format for person-id! Valid format is YYYYMMDD-XXXX or YYYYMMDD+XXXX.",
            response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificatePatientIdUtanSekelsiffror() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getLakarutlatande().getPatient().getPersonId().setExtension("121212-1212");
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong format for person-id! Valid format is YYYYMMDD-XXXX or YYYYMMDD+XXXX.",
            response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificatePatientIdUtanBindestreckKorrigeras() throws Exception {
        RevokeMedicalCertificateRequestType request = revokeRequest();
        request.getRevoke().getLakarutlatande().getPatient().getPersonId().setExtension("191212121212");
        when(certificateService.revokeCertificate(PERSONNUMMER, CERTIFICATE_ID)).thenReturn(new Certificate(CERTIFICATE_ID));

        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, request);

        assertEquals(ResultCodeEnum.OK, response.getResult().getResultCode());
    }

    // INTYG-4086: Namn ej oblig.
//    @Test
//    public void testRevokeMedicalCertificateSaknatPatientnamn() throws Exception {
//        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
//        invalidRequest.getRevoke().getLakarutlatande().getPatient().setFullstandigtNamn(null);
//        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);
//
//        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
//        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
//        assertEquals("Validation Error(s) found: No Patient fullstandigtNamn elements found or set!", response.getResult().getErrorText());
//        Mockito.verifyZeroInteractions(statisticsService);
//        Mockito.verifyZeroInteractions(sjukfallCertificateService);
//        Mockito.verifyZeroInteractions(certificateService);
//    }

    @Test
    public void testRevokeMedicalCertificateSaknatSigneringsdatum() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getLakarutlatande().setSigneringsTidpunkt(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No signeringstidpunkt found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknadVardreferens() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().setVardReferensId(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardReferens found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknatAvsantTidpunkt() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().setAvsantTidpunkt(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No avsantTidpunkt found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknatAdressVard() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().setAdressVard(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardAdress element found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknadHosPersonal() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().setHosPersonal(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No SkapadAvHosPersonal element found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateFelaktigPersonalIdKod() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getPersonalId().setRoot("invalid");
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong o.i.d. for personalId! Should be 1.2.752.129.2.1.4.1",
            response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateTomtPersonalId() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getPersonalId().setExtension("");
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No personal-id found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknadEnhet() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().setEnhet(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No enhet element found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknadEnhetId() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getEnhet().setEnhetsId(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No enhets-id found!\n" +
            "Wrong o.i.d. for enhetsId! Should be 1.2.752.129.2.1.4.1", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateFelaktigEnhetIdKod() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setRoot("invalid");
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong o.i.d. for enhetsId! Should be 1.2.752.129.2.1.4.1",
            response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateTomtEnhetId() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setExtension("");
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No enhets-id found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknatEnhetnamn() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getEnhet().setEnhetsnamn(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No enhetsnamn found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknadVardgivare() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getEnhet().setVardgivare(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardgivare element found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknatVardgivareId() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getEnhet().getVardgivare().setVardgivareId(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardgivare-id found!\n" +
            "Wrong o.i.d. for vardgivareId! Should be 1.2.752.129.2.1.4.1", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateFelaktigVardgivareIdKod() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setRoot("invalid");
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong o.i.d. for vardgivareId! Should be 1.2.752.129.2.1.4.1",
            response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateTomtVardgivareId() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setExtension("");
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardgivare-id found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }

    @Test
    public void testRevokeMedicalCertificateSaknatVardgivarenamn() throws Exception {
        RevokeMedicalCertificateRequestType invalidRequest = revokeRequest();
        invalidRequest.getRevoke().getAdressVard().getHosPersonal().getEnhet().getVardgivare().setVardgivarnamn(null);
        RevokeMedicalCertificateResponseType response = responder.revokeMedicalCertificate(ADDRESS, invalidRequest);

        assertEquals(ResultCodeEnum.ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardgivarenamn found!", response.getResult().getErrorText());
        Mockito.verifyNoInteractions(statisticsService);
        Mockito.verifyNoInteractions(sjukfallCertificateService);
        Mockito.verifyNoInteractions(certificateService);
    }
}
