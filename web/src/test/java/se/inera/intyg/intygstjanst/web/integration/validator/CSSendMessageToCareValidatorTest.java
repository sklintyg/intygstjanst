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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator.ErrorCode;

@ExtendWith(MockitoExtension.class)
class CSSendMessageToCareValidatorTest {

    @Mock
    private CSIntegrationService csIntegrationService;

    @InjectMocks
    private CSSendMessageToCareValidator csSendMessageToCareValidator;

    private static final String PATIENT_ID = "191212121212";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String OTHER_PATIENT_ID = "191212121213";

    @Nested
    class ValidateCertificateExists {

        @Test
        void shouldNotReturnValidationErrorWhenCertificateExists() {
            final var validationErrors = new ArrayList<String>();
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            final var certificateMetadata = CertificateMetadata.builder()
                .patient(patient)
                .testCertificate(false)
                .status(CertificateStatus.SIGNED)
                .build();

            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, PATIENT_ID, validationErrors);
            assertTrue(validationErrors.isEmpty());
        }

        @Test
        void shouldReturnValidationErrorWhenCertificateDoesNotExists() {
            final var validationErrors = new ArrayList<String>();
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(false);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, PATIENT_ID, validationErrors);
            assertEquals(1, validationErrors.size());
            assertTrue(validationErrors.contains(ErrorCode.CERTIFICATE_NOT_FOUND_ERROR.toString()));
        }

        @Test
        void shouldThrowIllegalStateIfCallFailureForCertificateExists() {
            final var validationErrors = new ArrayList<String>();
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenThrow(IllegalStateException.class);

            assertThrows(IllegalStateException.class,
                () -> csSendMessageToCareValidator.validate(CERTIFICATE_ID, PATIENT_ID, validationErrors));

        }

        @Test
        void shouldThrowIllegalStateIfCallFailureForCertificateMetadata() {
            final var validationErrors = new ArrayList<String>();
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenThrow(IllegalStateException.class);

            assertThrows(IllegalStateException.class,
                () -> csSendMessageToCareValidator.validate(CERTIFICATE_ID, PATIENT_ID, validationErrors));

        }

        @Test
        void shouldNotTryToFetchMetadataIfCertificateDoesNotExist() {
            final var validationErrors = new ArrayList<String>();
            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(false);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, PATIENT_ID, validationErrors);
            verifyNoMoreInteractions(csIntegrationService);
        }
    }

    @Nested
    class ValidatePatientId {

        CertificateMetadataBuilder certificateMetadataBuilder;

        @BeforeEach
        void setup() {
            certificateMetadataBuilder = CertificateMetadata.builder()
                .testCertificate(false)
                .status(CertificateStatus.SIGNED);

            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
        }

        @Test
        void shouldNotReturnValidationErrorWhenPatientIdsMatch() {
            final var validationErrors = new ArrayList<String>();
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            final var certificateMetadata = certificateMetadataBuilder
                .patient(patient)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, PATIENT_ID, validationErrors);
            assertTrue(validationErrors.isEmpty());
        }

        @Test
        void shouldReturnValidationErrorWhenPatientIdsDoNotMatch() {
            final var validationErrors = new ArrayList<String>();
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            final var certificateMetadata = certificateMetadataBuilder
                .patient(patient)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, OTHER_PATIENT_ID, validationErrors);
            assertEquals(1, validationErrors.size());
            assertTrue(validationErrors.contains(ErrorCode.CIVIC_REGISTRATION_NUMBER_INCONSISTENCY_ERROR.toString()));
        }

        @Test
        void shouldReturnValidationErrorWhenPatientIdsFromCertificateIsMissing() {
            final var validationErrors = new ArrayList<String>();
            final var personId = PersonId.builder().id(null).build();
            final var patient = Patient.builder().personId(personId).build();
            final var certificateMetadata = certificateMetadataBuilder
                .patient(patient)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, OTHER_PATIENT_ID, validationErrors);
            assertEquals(1, validationErrors.size());
            assertTrue(validationErrors.contains(ErrorCode.CIVIC_REGISTRATION_NUMBER_INCONSISTENCY_ERROR.toString()));
        }

        @Test
        void shouldReturnValidationErrorWhenPatientIdsFromMessageIsMissing() {
            final var validationErrors = new ArrayList<String>();
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            final var certificateMetadata = certificateMetadataBuilder
                .patient(patient)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, null, validationErrors);
            assertEquals(1, validationErrors.size());
            assertTrue(validationErrors.contains(ErrorCode.CIVIC_REGISTRATION_NUMBER_INCONSISTENCY_ERROR.toString()));
        }
    }

    @Nested
    class ValidateRevoked {

        CertificateMetadataBuilder certificateMetadataBuilder;

        @BeforeEach
        void setup() {
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            certificateMetadataBuilder = CertificateMetadata.builder()
                .patient(patient)
                .testCertificate(false);

            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
        }

        @Test
        void shouldNotReturnValidationErrorWhenCertificateNotRevoked() {
            final var validationErrors = new ArrayList<String>();
            final var certificateMetadata = certificateMetadataBuilder
                .status(CertificateStatus.SIGNED)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, PATIENT_ID, validationErrors);
            assertTrue(validationErrors.isEmpty());
        }

        @Test
        void shouldReturnValidationErrorWhenCertificateRevoked() {
            final var validationErrors = new ArrayList<String>();
            final var certificateMetadata = certificateMetadataBuilder
                .status(CertificateStatus.REVOKED)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, PATIENT_ID, validationErrors);
            assertEquals(2, validationErrors.size());
            assertTrue(validationErrors.contains(ErrorCode.CERTIFICATE_REVOKED_ERROR.toString()));
        }
    }

    @Nested
    class ValidateTestCertificate {

        CertificateMetadataBuilder certificateMetadataBuilder;

        @BeforeEach
        void setup() {
            final var personId = PersonId.builder().id(PATIENT_ID).build();
            final var patient = Patient.builder().personId(personId).build();
            certificateMetadataBuilder = CertificateMetadata.builder()
                .patient(patient)
                .status(CertificateStatus.SIGNED);

            when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
        }

        @Test
        void shouldNotReturnValidationErrorWhenNotTestCertificate() {
            final var validationErrors = new ArrayList<String>();
            final var certificateMetadata = certificateMetadataBuilder
                .testCertificate(false)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, PATIENT_ID, validationErrors);
            assertTrue(validationErrors.isEmpty());
        }

        @Test
        void shouldReturnValidationErrorWhentestCertificate() {
            final var validationErrors = new ArrayList<String>();
            final var certificateMetadata = certificateMetadataBuilder
                .testCertificate(true)
                .build();

            when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

            csSendMessageToCareValidator.validate(CERTIFICATE_ID, PATIENT_ID, validationErrors);
            assertEquals(2, validationErrors.size());
            assertTrue(validationErrors.contains(ErrorCode.TEST_CERTIFICATE.toString()));
        }
    }

    @Test
    void shouldAckumulateValidationErrorsIfMultiple() {
        final var validationErrors = new ArrayList<String>();
        validationErrors.add("VALIDATION_ERROR_ALREADY_PRESENT");

        final var personId = PersonId.builder().id(PATIENT_ID).build();
        final var patient = Patient.builder().personId(personId).build();
        final var certificateMetadata = CertificateMetadata.builder()
            .patient(patient)
            .testCertificate(true)
            .status(CertificateStatus.REVOKED)
            .build();

        when(csIntegrationService.certificateExists(CERTIFICATE_ID)).thenReturn(true);
        when(csIntegrationService.getCertificateMetadata(CERTIFICATE_ID)).thenReturn(certificateMetadata);

        csSendMessageToCareValidator.validate(CERTIFICATE_ID, OTHER_PATIENT_ID, validationErrors);
        assertEquals(6, validationErrors.size());
    }

}
