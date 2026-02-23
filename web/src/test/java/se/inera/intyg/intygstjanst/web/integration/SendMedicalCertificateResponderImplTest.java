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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.ERROR;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.INFO;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.OK;

import iso.v21090.dt.v1.II;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ErrorIdEnum;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.logging.HashUtility;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.builder.RecipientBuilder;
import se.inera.intyg.schemas.contract.Personnummer;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SendMedicalCertificateResponderImplTest {

    private static final String LOGICAL_ADDRESS = "HSA-1234567890";

    private static final String CERTIFICATE_ID = "Intygs-id-1234567890";
    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer("19121212-1212").orElseThrow();

    private static final String FK_RECIPIENT_ID = "FK";
    private static final String FK_RECIPIENT_NAME = "Försäkringskassan";
    private static final String FK_RECIPIENT_LOGICALADDRESS = "FKORG";
    private static final String FK_RECIPIENT_CERTIFICATETYPES = "fk7263";

    private static final String PATIENT_ID_OID = "1.2.752.129.2.1.3.1";
    private static final String HOS_PERSONAL_OID = "1.2.752.129.2.1.4.1";
    private static final String ENHET_OID = "1.2.752.129.2.1.4.1";
    private static final String ARBETSPLATS_CODE_OID = "1.2.752.29.4.71";

    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @Mock
    private RecipientService recipientService = mock(RecipientService.class);

    @Mock
    private StatisticsService statisticsService;

    @Spy
    private HashUtility hashUtility;

    @InjectMocks
    private SendMedicalCertificateResponderImpl responder;

    @BeforeEach
    void setupPrimaryRecipient() {
        ReflectionTestUtils.setField(hashUtility, "salt", "salt");
        when(recipientService.getPrimaryRecipientFkassa()).thenReturn(createFkRecipient());
    }

    @Test
    void testSendOk() throws Exception {
        Certificate certificate = createCertificate();
        when(certificateService.getCertificateForCare(CERTIFICATE_ID)).thenReturn(certificate);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), createRequest());

        assertEquals(OK, response.getResult().getResultCode());

        verify(recipientService).getPrimaryRecipientFkassa();
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verify(statisticsService).sent(
            certificate.getId(), certificate.getType(), certificate.getCareUnitId(), createFkRecipient().getId());
    }

    @Test
    void testAlreadySent() throws Exception {
        when(certificateService.getCertificateForCare(CERTIFICATE_ID)).thenReturn(createCertificate());
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID)).thenReturn(SendStatus.ALREADY_SENT);

        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), createRequest());

        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'Intygs-id-1234567890' is already sent.", response.getResult().getInfoText());

        verify(recipientService).getPrimaryRecipientFkassa();
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verifyNoInteractions(statisticsService);
    }

    @Test
    void testSendMedicalCertificateInvalidCertificate() throws Exception {
        final var pnr = hashUtility.hash(PERSONNUMMER.getPersonnummer());
        when(certificateService.getCertificateForCare(CERTIFICATE_ID))
            .thenThrow(new InvalidCertificateException(CERTIFICATE_ID, pnr));
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals(
            "No certificate 'Intygs-id-1234567890' found to send for patient "
                + "'be125ef854ae8e7083ab76ebd2d7cd748e05603e02aec5cc3afeacd57d8f5f4b'.",
            response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verifyNoInteractions(statisticsService);
    }

    @Test
    void testSendMedicalCertificateCertificateRevoked() throws Exception {
        when(certificateService.getCertificateForCare(CERTIFICATE_ID)).thenReturn(createCertificate());
        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID))
            .thenThrow(new CertificateRevokedException(CERTIFICATE_ID));

        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), createRequest());

        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'Intygs-id-1234567890' has been revoked.", response.getResult().getInfoText());

        verify(recipientService).getPrimaryRecipientFkassa();
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verifyNoInteractions(statisticsService);
    }

    @Test
    void testSendMedicalCertificateServerException() throws Exception {
        when(certificateService.getCertificateForCare(CERTIFICATE_ID)).thenReturn(createCertificate());

        when(certificateService.sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID)).thenThrow(new ServerException());

        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.APPLICATION_ERROR, response.getResult().getErrorId());
        assertEquals("Certificate couldn't be sent to recipient", response.getResult().getErrorText());

        verify(recipientService).getPrimaryRecipientFkassa();
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verifyNoInteractions(statisticsService);
    }

    @Test
    void testSendMedicalCertificateSaknadPatient() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getLakarutlatande().setPatient(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No Patient element found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verifyNoInteractions(statisticsService);
    }

    @Test
    void testSendMedicalCertificateSaknatIntygId() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getLakarutlatande().setLakarutlatandeId(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No Lakarutlatande Id found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verifyNoInteractions(statisticsService);
    }

    @Test
    void testSendMedicalCertificateTomtIntygId() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getLakarutlatande().setLakarutlatandeId("");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No Lakarutlatande Id found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verifyNoInteractions(statisticsService);
    }

    @Test
    void testSendMedicalCertificateFelaktigPatientIdKod() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getLakarutlatande().getPatient().getPersonId().setRoot("invalid");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong o.i.d. for Patient Id! Should be 1.2.752.129.2.1.3.1 or 1.2.752.129.2.1.3.3",
            response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verifyNoInteractions(statisticsService);
    }

    @Test
    void testSendMedicalCertificateFelaktigtPatientId() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getLakarutlatande().getPatient().getPersonId().setExtension("invalid");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong format for person-id! Valid format is YYYYMMDD-XXXX or YYYYMMDD+XXXX.",
            response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verifyNoInteractions(statisticsService);
    }

    @Test
    void testSendMedicalCertificatePatientIdUtanSekelsiffror() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getLakarutlatande().getPatient().getPersonId().setExtension("121212-1212");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong format for person-id! Valid format is YYYYMMDD-XXXX or YYYYMMDD+XXXX.",
            response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verifyNoInteractions(statisticsService);
    }

    @Test
    void testSendMedicalCertificatePatientIdUtanBindestreckKorrigeras() throws Exception {
        SendMedicalCertificateRequestType request = createRequest();
        request.getSend().getLakarutlatande().getPatient().getPersonId().setExtension("191212121212");

        final Certificate certificate = createCertificate();

        doReturn(certificate)
            .when(certificateService)
            .getCertificateForCare(CERTIFICATE_ID);

        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), request);

        assertEquals(OK, response.getResult().getResultCode());

        verify(recipientService).getPrimaryRecipientFkassa();
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
        verify(statisticsService).sent(
            certificate.getId(), certificate.getType(), certificate.getCareUnitId(), createFkRecipient().getId());
    }

    @Test
    void testSendMedicalCertificateSaknadSigneringstidpunkt() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getLakarutlatande().setSigneringsTidpunkt(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No signeringstidpunkt found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateSaknadVardreferens() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().setVardReferensId(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardReferens found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateSaknadAvsantTidpunkt() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().setAvsantTidpunkt(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No avsantTidpunkt found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateSaknadAdressVard() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().setAdressVard(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardAdress element found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateSaknadHosPersonal() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().setHosPersonal(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No SkapadAvHosPersonal element found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateFelaktigPersonalIdKod() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getPersonalId().setRoot("invalid");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong o.i.d. for personalId! Should be 1.2.752.129.2.1.4.1",
            response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateTomtPersonalId() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getPersonalId().setExtension("");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No personal-id found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateSaknadEnhet() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().setEnhet(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No enhet element found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateSaknadEnhetId() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getEnhet().setEnhetsId(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No enhets-id found!\n" +
            "Wrong o.i.d. for enhetsId! Should be 1.2.752.129.2.1.4.1", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateFelaktigEnhetIdKod() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setRoot("invalid");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong o.i.d. for enhetsId! Should be 1.2.752.129.2.1.4.1",
            response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateTomtEnhetId() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().setExtension("");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No enhets-id found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateSaknatEnhetnamn() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getEnhet().setEnhetsnamn(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No enhetsnamn found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateSaknadVardgivare() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getEnhet().setVardgivare(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardgivare element found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateSaknatVardgivareId() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getEnhet().getVardgivare().setVardgivareId(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardgivare-id found!\n" +
            "Wrong o.i.d. for vardgivareId! Should be 1.2.752.129.2.1.4.1", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateFelaktigVardgivareIdKod() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setRoot("invalid");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: Wrong o.i.d. for vardgivareId! Should be 1.2.752.129.2.1.4.1",
            response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateTomtVardgivareId() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getEnhet().getVardgivare().getVardgivareId().setExtension("");
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardgivare-id found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    void testSendMedicalCertificateSaknatVardgivarenamn() throws Exception {
        SendMedicalCertificateRequestType invalidRequest = createRequest();
        invalidRequest.getSend().getAdressVard().getHosPersonal().getEnhet().getVardgivare().setVardgivarnamn(null);
        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(createAttributedURIType(), invalidRequest);

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdEnum.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Validation Error(s) found: No vardgivarenamn found!", response.getResult().getErrorText());

        verify(recipientService, never()).getPrimaryRecipientFkassa();
        verify(certificateService, never()).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    private Certificate createCertificate() {
        Certificate certificate = new Certificate(CERTIFICATE_ID);
        certificate.setType(CERTIFICATE_TYPE);
        certificate.setCivicRegistrationNumber(PERSONNUMMER);
        certificate.setCareUnitId("1");
        certificate.setCareUnitName("unitName");

        return certificate;
    }

    private AttributedURIType createAttributedURIType() {
        AttributedURIType uri = new AttributedURIType();
        uri.setValue(LOGICAL_ADDRESS);
        return uri;
    }

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

    private SendMedicalCertificateRequestType createRequest() {
        SendMedicalCertificateRequestType request = new SendMedicalCertificateRequestType();
        SendType sendType = new SendType();

        VardAdresseringsType vardAdresseringsType = new VardAdresseringsType();
        HosPersonalType hosPersonal = new HosPersonalType();

        // Enhet
        EnhetType enhet = new EnhetType();
        enhet.setEnhetsnamn("enhetsnamn");
        II enhetsId = new II();
        enhetsId.setRoot(ENHET_OID);
        enhetsId.setExtension("enhetsid");
        enhet.setEnhetsId(enhetsId);
        II arbetsplatsKod = new II();
        arbetsplatsKod.setRoot(ARBETSPLATS_CODE_OID);
        arbetsplatsKod.setExtension("arbetsplatskod");
        enhet.setArbetsplatskod(arbetsplatsKod);
        VardgivareType vardGivare = new VardgivareType();
        II vardGivarId = new II();
        vardGivarId.setRoot(ENHET_OID);
        vardGivarId.setExtension("vardgivarid");
        vardGivare.setVardgivareId(vardGivarId);
        vardGivare.setVardgivarnamn("MI");
        enhet.setVardgivare(vardGivare);
        hosPersonal.setEnhet(enhet);
        hosPersonal.setFullstandigtNamn("MI");
        II personalId = new II();
        personalId.setRoot(HOS_PERSONAL_OID);
        personalId.setExtension("MI");
        hosPersonal.setPersonalId(personalId);
        hosPersonal.setFullstandigtNamn("hospersonal namn");
        vardAdresseringsType.setHosPersonal(hosPersonal);

        sendType.setAdressVard(vardAdresseringsType);
        sendType.setAvsantTidpunkt(LocalDateTime.now());
        sendType.setVardReferensId("MI");

        // Lakarutlatande
        LakarutlatandeEnkelType lakarutlatande = new LakarutlatandeEnkelType();
        lakarutlatande.setLakarutlatandeId(CERTIFICATE_ID);
        lakarutlatande.setSigneringsTidpunkt(LocalDateTime.now());
        PatientType patient = new PatientType();
        II patientIdHolder = new II();
        patientIdHolder.setRoot(PATIENT_ID_OID);
        patientIdHolder.setExtension(PERSONNUMMER.getPersonnummerWithDash());
        patient.setPersonId(patientIdHolder);
        patient.setFullstandigtNamn("patientnamn");
        lakarutlatande.setPatient(patient);

        sendType.setLakarutlatande(lakarutlatande);

        request.setSend(sendType);

        return request;
    }
}
