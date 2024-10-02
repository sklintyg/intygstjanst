/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.xml.ws.soap.SOAPFaultException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.RecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.RevokedInformationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.StaffDTO;
import se.inera.intyg.intygstjanst.web.service.dto.UnitDTO;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@ExtendWith(MockitoExtension.class)
class CertificateEventRevokeServiceImplTest {

    @Mock
    private RevokeCertificateResponderInterface revokeCertificateResponderInterface;
    @Mock
    private RecipientService recipientService;
    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private CertificateEventRevokeServiceImpl certificateEventRevokeService;

    private static final String HSA_ID_OID = "1.2.752.129.2.1.4.1";
    private static final String PERSON_ID_OID = "1.2.752.129.2.1.3.1";
    private static final String SAMORDNING_ID_OID = "1.2.752.129.2.1.3.3";
    private static final String ARBETSPLATS_KOD_OID = "1.2.752.29.4.71";

    private static final String MESSAGE = "MESSAGE";
    private static final String REASON = "REASON";
    private static final StaffDTO STAFF = StaffDTO.builder()
        .fullName("FULL_NAME")
        .personId("PERSON_ID")
        .prescriptionCode("PRESCRIPTION_CODE")
        .build();
    private static final UnitDTO UNIT = UnitDTO.builder()
        .address("ADDRESS")
        .email("EMAIL")
        .city("CITY")
        .phoneNumber("PHONENUMBER")
        .zipCode("ZIPCODE")
        .unitId("CARE_UNIT_ID")
        .unitName("CARE_UNIT_NAME")
        .workplaceCode("WORK_PLACE_CODE")
        .build();
    private static final UnitDTO CARE_PROVIDER = UnitDTO.builder()
        .unitName("CARE_PROVIDER_NAME")
        .unitId("CARE_PROVIDER_ID")
        .build();

    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "fk7211";
    private static final String RECIPIENT_ID = "recipientId";
    private static final String RECIPIENT_NAME = "recipientName";
    private static final String LOGICAL_ADDRESS = "logicalAddress";
    private static final Recipient RECIPIENT = new Recipient(LOGICAL_ADDRESS, RECIPIENT_NAME, RECIPIENT_ID,
        CertificateRecipientType.HUVUDMOTTAGARE.name(), CERTIFICATE_TYPE, true, true);
    private static final String ENCODED_XML = Base64.getEncoder().encodeToString("xmlFromCertificateService"
        .getBytes(StandardCharsets.UTF_8));

    private GetCertificateXmlResponse xmlResponse;

    @BeforeEach
    void setUp() throws IOException {
        xmlResponse = getXmlResponse(PersonIdTypeDTO.PERSONAL_IDENTITY_NUMBER);
    }

    @Nested
    class RevokeCertificateRequest {

        @SneakyThrows
        @BeforeEach
        void setup() {
            when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
            when(revokeCertificateResponderInterface.revokeCertificate(eq(LOGICAL_ADDRESS), any(RevokeCertificateType.class)))
                .thenReturn(createResponse(ResultCodeType.OK, ""));
        }

        @Test
        void shouldIncludeMessage() {
            final var captor = ArgumentCaptor.forClass(RevokeCertificateType.class);

            certificateEventRevokeService.revoke(xmlResponse);
            verify(revokeCertificateResponderInterface).revokeCertificate(anyString(), captor.capture());

            assertEquals(MESSAGE, captor.getValue().getMeddelande());
        }

        @Test
        void shouldIncludeTimestamp() {
            final var captor = ArgumentCaptor.forClass(RevokeCertificateType.class);

            certificateEventRevokeService.revoke(xmlResponse);
            verify(revokeCertificateResponderInterface).revokeCertificate(anyString(), captor.capture());

            assertEquals(xmlResponse.getRevoked().getRevokedAt(), captor.getValue().getSkickatTidpunkt());
        }

        @Test
        void shouldIncludeCertificateId() {
            final var captor = ArgumentCaptor.forClass(RevokeCertificateType.class);

            certificateEventRevokeService.revoke(xmlResponse);
            verify(revokeCertificateResponderInterface).revokeCertificate(anyString(), captor.capture());

            assertAll(
                () -> assertEquals(xmlResponse.getCertificateId(), captor.getValue().getIntygsId().getExtension()),
                () -> assertEquals(xmlResponse.getUnit().getUnitId(), captor.getValue().getIntygsId().getRoot())
            );
        }

        @Test
        void shouldIncludeStaff() {
            final var captor = ArgumentCaptor.forClass(RevokeCertificateType.class);

            certificateEventRevokeService.revoke(xmlResponse);
            verify(revokeCertificateResponderInterface).revokeCertificate(anyString(), captor.capture());

            assertAll(
                () -> assertEquals(xmlResponse.getRevoked().getRevokedBy().getFullName(),
                    captor.getValue().getSkickatAv().getFullstandigtNamn()),
                () -> assertEquals(xmlResponse.getRevoked().getRevokedBy().getPersonId(),
                    captor.getValue().getSkickatAv().getPersonalId().getExtension()),
                () -> assertEquals(HSA_ID_OID,
                    captor.getValue().getSkickatAv().getPersonalId().getRoot()),
                () -> assertEquals(xmlResponse.getRevoked().getRevokedBy().getPrescriptionCode(),
                    captor.getValue().getSkickatAv().getForskrivarkod())
            );
        }

        @Test
        void shouldIncludeStaffUnit() {
            final var captor = ArgumentCaptor.forClass(RevokeCertificateType.class);

            certificateEventRevokeService.revoke(xmlResponse);
            verify(revokeCertificateResponderInterface).revokeCertificate(anyString(), captor.capture());

            final var convertedUnit = captor.getValue().getSkickatAv().getEnhet();
            assertAll(
                () -> assertEquals(UNIT.getUnitName(), convertedUnit.getEnhetsnamn()),
                () -> assertEquals(UNIT.getUnitId(), convertedUnit.getEnhetsId().getExtension()),
                () -> assertEquals(HSA_ID_OID, convertedUnit.getEnhetsId().getRoot()),
                () -> assertEquals(UNIT.getAddress(), convertedUnit.getPostadress()),
                () -> assertEquals(UNIT.getEmail(), convertedUnit.getEpost()),
                () -> assertEquals(UNIT.getCity(), convertedUnit.getPostort()),
                () -> assertEquals(UNIT.getPhoneNumber(), convertedUnit.getTelefonnummer()),
                () -> assertEquals(UNIT.getWorkplaceCode(), convertedUnit.getArbetsplatskod().getExtension()),
                () -> assertEquals(ARBETSPLATS_KOD_OID, convertedUnit.getArbetsplatskod().getRoot())
            );
        }

        @Test
        void shouldIncludeStaffCareProvider() {
            final var captor = ArgumentCaptor.forClass(RevokeCertificateType.class);

            certificateEventRevokeService.revoke(xmlResponse);
            verify(revokeCertificateResponderInterface).revokeCertificate(anyString(), captor.capture());

            final var convertedUnit = captor.getValue().getSkickatAv().getEnhet().getVardgivare();
            assertAll(
                () -> assertEquals(CARE_PROVIDER.getUnitName(), convertedUnit.getVardgivarnamn()),
                () -> assertEquals(CARE_PROVIDER.getUnitId(), convertedUnit.getVardgivareId().getExtension()),
                () -> assertEquals(HSA_ID_OID, convertedUnit.getVardgivareId().getRoot())
            );
        }

        @Test
        void shouldIncludePatientIdPersonNumberType() {
            final var captor = ArgumentCaptor.forClass(RevokeCertificateType.class);

            certificateEventRevokeService.revoke(xmlResponse);
            verify(revokeCertificateResponderInterface).revokeCertificate(anyString(), captor.capture());

            final var convertedPatientId = captor.getValue().getPatientPersonId();
            assertAll(
                () -> assertEquals(xmlResponse.getPatientId().getId(), convertedPatientId.getExtension()),
                () -> assertEquals(PERSON_ID_OID, convertedPatientId.getRoot())
            );
        }

        @Test
        void shouldIncludePatientIdCoordinationNumber() {
            xmlResponse = getXmlResponse(PersonIdTypeDTO.COORDINATION_NUMBER);
            final var captor = ArgumentCaptor.forClass(RevokeCertificateType.class);

            certificateEventRevokeService.revoke(xmlResponse);
            verify(revokeCertificateResponderInterface).revokeCertificate(anyString(), captor.capture());

            final var convertedPatientId = captor.getValue().getPatientPersonId();
            assertAll(
                () -> assertEquals(xmlResponse.getPatientId().getId(), convertedPatientId.getExtension()),
                () -> assertEquals(SAMORDNING_ID_OID, convertedPatientId.getRoot())
            );
        }
    }

    @Test
    void shouldNotThrowIfResultCodeOk() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
        when(revokeCertificateResponderInterface.revokeCertificate(eq(LOGICAL_ADDRESS), any(RevokeCertificateType.class)))
            .thenReturn(createResponse(ResultCodeType.OK, ""));

        assertDoesNotThrow(() -> certificateEventRevokeService.revoke(xmlResponse));
    }

    @Test
    void shouldMonitorLogIfResultCodeOk() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
        when(revokeCertificateResponderInterface.revokeCertificate(eq(LOGICAL_ADDRESS), any(RevokeCertificateType.class)))
            .thenReturn(createResponse(ResultCodeType.OK, ""));

        certificateEventRevokeService.revoke(xmlResponse);

        verify(monitoringLogService).logCertificateRevoked(
            xmlResponse.getCertificateId(),
            xmlResponse.getCertificateType(),
            xmlResponse.getUnit().getUnitId()
        );
    }

    @Test
    void shouldNotThrowIfResultCodeInfo() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
        when(revokeCertificateResponderInterface.revokeCertificate(eq(LOGICAL_ADDRESS), any(RevokeCertificateType.class)))
            .thenReturn(createResponse(ResultCodeType.INFO, "infoText"));

        assertDoesNotThrow(() -> certificateEventRevokeService.revoke(xmlResponse));
    }

    @Test
    void shouldMonitorLogIfResultCodeInfo() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
        when(revokeCertificateResponderInterface.revokeCertificate(eq(LOGICAL_ADDRESS), any(RevokeCertificateType.class)))
            .thenReturn(createResponse(ResultCodeType.INFO, "infoText"));

        certificateEventRevokeService.revoke(xmlResponse);

        verify(monitoringLogService).logCertificateRevoked(
            xmlResponse.getCertificateId(),
            xmlResponse.getCertificateType(),
            xmlResponse.getUnit().getUnitId()
        );
    }

    @Test
    void shouldThrowIllegalStateExceptionIfResultCodeError() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
        when(revokeCertificateResponderInterface.revokeCertificate(eq(LOGICAL_ADDRESS), any(RevokeCertificateType.class)))
            .thenReturn(createResponse(ResultCodeType.ERROR, "errorText"));

        assertThrows(IllegalStateException.class, () -> certificateEventRevokeService.revoke(xmlResponse));
    }

    @Test
    void shouldIncludeSendCallParametersInExceptionMessageWhenResultIsNull() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
        when(revokeCertificateResponderInterface.revokeCertificate(eq(LOGICAL_ADDRESS), any(RevokeCertificateType.class)))
            .thenReturn(createResponse(null));

        final var e = assertThrows(IllegalStateException.class, () -> certificateEventRevokeService.revoke(xmlResponse));
        assertAll(
            () -> assertTrue(e.getMessage().contains(CERTIFICATE_ID)),
            () -> assertTrue(e.getMessage().contains(CERTIFICATE_TYPE)),
            () -> assertTrue(e.getMessage().contains(RECIPIENT_ID))
        );
    }

    @Test
    void shouldIncludeSendCallParametersInExceptionMessageWhenResultIsError() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
        when(revokeCertificateResponderInterface.revokeCertificate(eq(LOGICAL_ADDRESS), any(RevokeCertificateType.class)))
            .thenReturn(createResponse(ResultCodeType.ERROR, ERROR_MESSAGE));

        final var e = assertThrows(IllegalStateException.class, () -> certificateEventRevokeService.revoke(xmlResponse));
        assertAll(
            () -> assertTrue(e.getMessage().contains(CERTIFICATE_ID)),
            () -> assertTrue(e.getMessage().contains(CERTIFICATE_TYPE)),
            () -> assertTrue(e.getMessage().contains(RECIPIENT_ID)),
            () -> assertTrue(e.getMessage().contains(ERROR_MESSAGE))
        );
    }

    @Test
    void shouldThrowIllegalStateExceptionIfRecipientUnknownException() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenThrow(RecipientUnknownException.class);
        assertThrows(IllegalStateException.class, () -> certificateEventRevokeService.revoke(xmlResponse));
    }

    @Test
    void shouldThrowIllegalStateExceptionIfSoapFault() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenThrow(SOAPFaultException.class);
        assertThrows(IllegalStateException.class, () -> certificateEventRevokeService.revoke(xmlResponse));
    }

    private RevokeCertificateResponseType createResponse(ResultCodeType resultCode, String resultText) {
        final var resultType = new ResultType();
        resultType.setResultCode(resultCode);
        resultType.setResultText(resultText);
        return createResponse(resultType);
    }

    private RevokeCertificateResponseType createResponse(ResultType resultType) {
        final var responseType = new RevokeCertificateResponseType();
        responseType.setResult(resultType);
        return responseType;
    }

    private static GetCertificateXmlResponse getXmlResponse(PersonIdTypeDTO personIdType) {
        return GetCertificateXmlResponse.builder()
            .certificateId(CERTIFICATE_ID)
            .certificateType(CERTIFICATE_TYPE)
            .xml(ENCODED_XML)
            .recipient(RecipientDTO.builder()
                .id(RECIPIENT_ID)
                .build())
            .revoked(
                RevokedInformationDTO.builder()
                    .revokedAt(LocalDateTime.now())
                    .revokedBy(STAFF)
                    .message(MESSAGE)
                    .reason(REASON)
                    .build()
            )
            .unit(UNIT)
            .careProvider(
                CARE_PROVIDER
            )
            .patientId(
                PersonIdDTO.builder()
                    .id("PERSON_ID")
                    .type(personIdType)
                    .build()
            )
            .build();
    }

}
