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

package se.inera.intyg.intygstjanst.web.integration.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata.CertificateMetadataBuilder;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;

@ExtendWith(MockitoExtension.class)
class CSSendMessageToRecipientValidatorTest {

    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private SendMessageToRecipientValidator sendMessageToRecipientValidator;

    @InjectMocks
    private CSSendMessageToRecipientValidator csSendMessageToRecipientValidator;

    private static final String PATIENT_ID = "191212121212";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String OTHER_PATIENT_ID = "191212121213";

    @Nested
    class ValidateCertificateExists {

        SendMessageToRecipientType message;
        List<String> validationErrors;

        @BeforeEach
        void setup() {
            validationErrors = new ArrayList<>();
            message = createMessage(PATIENT_ID);
        }

        @Test
        void shouldNotReturnValidationErrorWhenCertificateExists() {
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            final var certificateMetadata = CertificateMetadata.builder()
                .patient(patient)
                .sent(true)
                .testCertificate(false)
                .status(CertificateStatus.SIGNED)
                .build();

            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            assertTrue(validationErrors.isEmpty());
        }

        @Test
        void shouldReturnValidationErrorWhenCertificateDoesNotExists() {
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(false);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            assertEquals(1, validationErrors.size());
            assertTrue(validationErrors.contains("Certificate does not exist."));
        }

        @Test
        void shouldThrowIllegalStateIfCallFailureForCertificateExists() {
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenThrow(IllegalStateException.class);

            assertThrows(IllegalStateException.class,
                () -> csSendMessageToRecipientValidator.validate(message, validationErrors));

        }

        @Test
        void shouldThrowIllegalStateIfCallFailureForCertificateMetadata() {
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenThrow(IllegalStateException.class);

            assertThrows(IllegalStateException.class,
                () -> csSendMessageToRecipientValidator.validate(message, validationErrors));

        }

        @Test
        void shouldNotTryToFetchMetadataIfCertificateDoesNotExist() {
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(false);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            verifyNoMoreInteractions(csIntegrationService);
        }
    }


    @Nested
    class ValidatePatientId {

        CertificateMetadataBuilder certificateMetadataBuilder;
        List<String> validationErrors;

        @BeforeEach
        void setup() {
            certificateMetadataBuilder = CertificateMetadata.builder()
                .sent(true)
                .testCertificate(false)
                .status(CertificateStatus.SIGNED);

            validationErrors = new ArrayList<>();
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
        }

        @Test
        void shouldNotReturnValidationErrorWhenPatientIdsMatch() {
            final var message = createMessage(PATIENT_ID);
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            final var certificateMetadata = certificateMetadataBuilder
                .patient(patient)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            assertTrue(validationErrors.isEmpty());
        }

        @Test
        void shouldReturnValidationErrorWhenPatientIdsDoNotMatch() {
            final var message = createMessage(OTHER_PATIENT_ID);
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            final var certificateMetadata = certificateMetadataBuilder
                .patient(patient)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            assertEquals(1, validationErrors.size());
            assertTrue(validationErrors.contains("PatientId from message does not match PatientId from certificate."));
        }

        @Test
        void shouldReturnValidationErrorWhenPatientIdsFromCertificateIsMissing() {
            final var message = createMessage(OTHER_PATIENT_ID);
            final var personId = PersonId.builder().id(null).build();
            final var patient = Patient.builder().personId(personId).build();
            final var certificateMetadata = certificateMetadataBuilder
                .patient(patient)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            assertEquals(1, validationErrors.size());
            assertTrue(validationErrors.contains("PatientId from message does not match PatientId from certificate."));
        }

        @Test
        void shouldReturnValidationErrorWhenPatientIdsFromMessageIsMissing() {
            final var message = createMessage(null);
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            final var certificateMetadata = certificateMetadataBuilder
                .patient(patient)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            assertEquals(1, validationErrors.size());
            assertTrue(validationErrors.contains("PatientId from message does not match PatientId from certificate."));
        }
    }

    @Nested
    class ValidateRevoked {

        CertificateMetadataBuilder certificateMetadataBuilder;
        SendMessageToRecipientType message;
        List<String> validationErrors;

        @BeforeEach
        void setup() {
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            certificateMetadataBuilder = CertificateMetadata.builder()
                .patient(patient)
                .sent(true)
                .testCertificate(false);

            message = createMessage(PATIENT_ID);
            validationErrors = new ArrayList<>();
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
        }

        @Test
        void shouldNotReturnValidationErrorWhenCertificateNotRevoked() {
            final var certificateMetadata = certificateMetadataBuilder
                .status(CertificateStatus.SIGNED)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            assertTrue(validationErrors.isEmpty());
        }

        @Test
        void shouldReturnValidationErrorWhenCertificateRevoked() {
            final var certificateMetadata = certificateMetadataBuilder
                .status(CertificateStatus.REVOKED)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            assertEquals(1, validationErrors.size());
            assertTrue(validationErrors.contains("Certificate is revoked. Messages cannot be sent for revoked certificates."));
        }
    }


    @Nested
    class ValidateSend {

        CertificateMetadataBuilder certificateMetadataBuilder;
        SendMessageToRecipientType message;
        List<String> validationErrors;

        @BeforeEach
        void setup() {
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            certificateMetadataBuilder = CertificateMetadata.builder()
                .patient(patient)
                .status(CertificateStatus.SIGNED)
                .testCertificate(false);

            message = createMessage(PATIENT_ID);
            validationErrors = new ArrayList<>();
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
        }

        @Test
        void shouldNotReturnValidationErrorWhenSent() {
            final var certificateMetadata = certificateMetadataBuilder
                .sent(true)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            assertTrue(validationErrors.isEmpty());
        }

        @Test
        void shouldReturnValidationErrorWhenNotSent() {
            final var certificateMetadata = certificateMetadataBuilder
                .sent(false)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToRecipientValidator.validate(message, validationErrors);
            assertEquals(1, validationErrors.size());
            assertTrue(validationErrors.contains("Certificate has not been sent to recipient."));
        }
    }

    @Nested
    class TestCertificate {
        CertificateMetadataBuilder certificateMetadataBuilder;
        SendMessageToRecipientType message;
        List<String> validationErrors;

        @BeforeEach
        void setup() {
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            certificateMetadataBuilder = CertificateMetadata.builder()
                .patient(patient)
                .status(CertificateStatus.SIGNED)
                .sent(true);

            message = createMessage(PATIENT_ID);
            validationErrors = new ArrayList<>();
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
        }

        @Test
        void shouldNotReturnFalseIfNotTestCertificate() {
            final var certificateMetadata = certificateMetadataBuilder
                .testCertificate(false)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);
            assertFalse(csSendMessageToRecipientValidator.validate(message, validationErrors));
        }

        @Test
        void shouldNotReturnTrueIfNotTestCertificate() {
            final var certificateMetadata = certificateMetadataBuilder
                .testCertificate(true)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);
            assertTrue(csSendMessageToRecipientValidator.validate(message, validationErrors));
        }
    }

    @Test
    void shouldAckumulateValidationErrorsIfMultiple() {
        final var validationErrors = new ArrayList<String>();
        final var message = createMessage(OTHER_PATIENT_ID);
        final var personId = PersonId.builder().id(PATIENT_ID).build();
        final var patient = Patient.builder().personId(personId).build();
        final var certificateMetadata = CertificateMetadata.builder()
            .patient(patient)
            .testCertificate(true)
            .sent(false)
            .status(CertificateStatus.REVOKED)
            .build();

        doAnswer(i -> {
            final List<String> errors = i.getArgument(1);
            errors.add("VALIDATION_ERROR_FROM_METHOD_CALL");
            return null;
        }).when(sendMessageToRecipientValidator).csValidate(any(SendMessageToRecipientType.class), anyList());
        when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
        when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

        csSendMessageToRecipientValidator.validate(message, validationErrors);
        assertEquals(4, validationErrors.size());
    }

    private static SendMessageToRecipientType createMessage(String patientId) {
        final var personId = new se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId();
        personId.setExtension(patientId);

        final var certId = new IntygId();
        certId.setExtension(CERTIFICATE_ID);

        final var message = new SendMessageToRecipientType();
        message.setIntygsId(certId);
        message.setPatientPersonId(personId);
        return message;
    }

}
