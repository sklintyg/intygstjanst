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

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;

@Service
@Slf4j
@RequiredArgsConstructor
public class CSSendMessageToRecipientValidator {

    private final CSIntegrationService csIntegrationService;
    private final SendMessageToRecipientValidator sendMessageToRecipientValidator;

    public boolean validate(SendMessageToRecipientType message, List<String> validationErrors) {
        sendMessageToRecipientValidator.csValidate(message, validationErrors);

        final var certificateId = message.getIntygsId().getExtension();
        final var certificateExists = csIntegrationService.certificateExists(certificateId);
        validateCertificateExists(certificateExists, validationErrors);

        if (Boolean.TRUE.equals(certificateExists)) {
            final var patientId = message.getPatientPersonId().getExtension();
            final var certificateMetadata = csIntegrationService.getCertificateMetadata(certificateId);
            validatePatientId(certificateMetadata, patientId, validationErrors);
            validateSent(certificateMetadata, validationErrors);
            validateRevoked(certificateMetadata, validationErrors);
            return isTestCertificate(certificateMetadata);
        }

        return false;
    }

    private void validateCertificateExists(boolean certificateExists, List<String> validationErrors) {
        if (Boolean.FALSE.equals(certificateExists)) {
            validationErrors.add("Certificate does not exist");
        }
    }

    private void validatePatientId(CertificateMetadata certificateMetadata, String patientId, List<String> validationErrors) {
        final var patientIdFromMessage = Personnummer.createPersonnummer(patientId);
        final var patientIdFromCertificate = Personnummer.createPersonnummer(certificateMetadata.getPatient().getPersonId().getId());

        if (patientIdFromMessage.isEmpty() || patientIdFromCertificate.isEmpty()
            || !patientIdFromMessage.get().equals(patientIdFromCertificate.get())) {
            validationErrors.add("PatientId from message does not match PatientId from certificate");
        }
    }

    private void validateSent(CertificateMetadata certificateMetadata, List<String> validationErrors) {
        if (!certificateMetadata.isSent()) {
            validationErrors.add("Certificate has not been sent to recipient.");
        }
    }

    private void validateRevoked(CertificateMetadata certificateMetadata, List<String> validationErrors) {
        if (certificateMetadata.getStatus() == CertificateStatus.REVOKED) {
            validationErrors.add("Certificate is revoked. Messages cannot be sent for revoked certificates.");
        }
    }

    private boolean isTestCertificate(CertificateMetadata certificateMetadata) {
        return certificateMetadata.isTestCertificate();
    }

}
