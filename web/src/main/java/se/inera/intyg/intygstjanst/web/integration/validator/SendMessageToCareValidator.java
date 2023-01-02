/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.v3.MeddelandeReferens;

@Component
public class SendMessageToCareValidator {

    public enum Amneskod {
        KOMPLT,
        AVSTMN,
        KONTKT,
        OVRIGT,
        PAMINN
    }

    public enum ErrorCode {
        SUBJECT_CONSISTENCY_ERROR,
        MESSAGE_TYPE_CONSISTENCY_ERROR,
        SUBJECT_NOT_SUPPORTED_ERROR,
        CERTIFICATE_NOT_FOUND_ERROR,
        CIVIC_REGISTRATION_NUMBER_INCONSISTENCY_ERROR,
        REFERENCED_MESSAGE_NOT_FOUND_ERROR,
        KOMPLETTERING_INCONSISTENCY_ERROR,
        PAMINNELSE_ID_INCONSISTENCY_ERROR,
        MEDDELANDE_ID_NOT_UNIQUE_ERROR,
        TEST_CERTIFICATE
    }

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private ArendeRepository messageRepository;

    public List<String> validateSendMessageToCare(SendMessageToCareType sendMessageToCareType) {
        List<String> validationErrors = new ArrayList<>();
        String personnummeer = sendMessageToCareType.getPatientPersonId().getExtension();

        validateSkickatAv(sendMessageToCareType.getSkickatAv().getPart().getCode(), validationErrors);
        validateMeddelandeId(sendMessageToCareType.getMeddelandeId(), validationErrors);
        validateMessageSubject(sendMessageToCareType.getAmne().getCode(), validationErrors);
        validateThatCertificateExists(sendMessageToCareType.getIntygsId().getExtension(), personnummeer, validationErrors);
        validateConsistencyForQuestionVsAnswer(sendMessageToCareType, validationErrors);
        validatePaminnelse(sendMessageToCareType, validationErrors);
        validateConsistencyOfSubject(sendMessageToCareType, validationErrors);
        validateConsistencyForKomplettering(sendMessageToCareType, validationErrors);
        validateTestCertificate(sendMessageToCareType.getIntygsId().getExtension(), validationErrors);

        return validationErrors;
    }

    @VisibleForTesting
    void validateSkickatAv(String code, List<String> validationErrors) {
        try {
            recipientService.getRecipient(code);
        } catch (RecipientUnknownException | NullPointerException e) {
            validationErrors.add("SkickatAv part code " + code + " is not valid");
        }
    }

    @VisibleForTesting
    void validateMeddelandeId(String meddelandeId, List<String> validationErrors) {
        if (Strings.isNullOrEmpty(meddelandeId) || messageRepository.findByMeddelandeId(meddelandeId) != null) {
            validationErrors.add(ErrorCode.MEDDELANDE_ID_NOT_UNIQUE_ERROR.toString());
            validationErrors.add("Meddelande-id is not a GUID");
        }
    }

    @VisibleForTesting
    void validateMessageSubject(String subject, List<String> validationErrors) {
        try {
            Amneskod.valueOf(subject);
        } catch (Exception e) {
            validationErrors.add(ErrorCode.SUBJECT_NOT_SUPPORTED_ERROR.toString());
            validationErrors.add(" The supplied certificate subject is invalid. "
                + "Supported subjects are " + Stream.of(Amneskod.values()).map(Amneskod::name).collect(Collectors.joining(", ")));
        }
    }

    @VisibleForTesting
    void validateConsistencyForQuestionVsAnswer(SendMessageToCareType sendMessageToCareType, List<String> validationErrors) {
        LocalDate lastDayOfReply = sendMessageToCareType.getSistaDatumForSvar();
        if (lastDayOfReply != null && messageIsAnAnswer(sendMessageToCareType)) {
            validationErrors.add(ErrorCode.MESSAGE_TYPE_CONSISTENCY_ERROR.toString());
            validationErrors
                .add(" Message concerning certificate id " + sendMessageToCareType.getIntygsId().getExtension()
                    + " is an answer to a question"
                    + "and should not specify a last date of reply 'sistaSvarsDatum'.");
        }
    }

    @VisibleForTesting
    void validateConsistencyOfSubject(SendMessageToCareType sendMessageToCareType, List<String> validationErrors) {
        MeddelandeReferens meddelandeReferens = sendMessageToCareType.getSvarPa();
        if (meddelandeReferens != null) {
            String meddelandeId = meddelandeReferens.getMeddelandeId();
            Arende res = messageRepository.findByMeddelandeId(meddelandeId);

            if (res == null) {
                validationErrors.add(ErrorCode.REFERENCED_MESSAGE_NOT_FOUND_ERROR.toString());
                return;
            }
            String amne = res.getAmne();
            if (!sendMessageToCareType.getAmne().getCode().equals(amne) && !isPaminnelse(sendMessageToCareType)) {
                validationErrors.add(ErrorCode.SUBJECT_CONSISTENCY_ERROR.toString());
                validationErrors.add(" Message with meddelandeId " + meddelandeId + " referenced by reply message with id "
                    + sendMessageToCareType.getMeddelandeId()
                    + " regarding certificate with id " + sendMessageToCareType.getIntygsId().getExtension());
            }
        }

    }

    @VisibleForTesting
    void validateConsistencyForKomplettering(SendMessageToCareType sendMessageToCareType, List<String> validationErrors) {
        if (!sendMessageToCareType.getAmne().getCode().equals(Amneskod.KOMPLT.toString()) && hasKomplettering(sendMessageToCareType)) {
            validationErrors.add(ErrorCode.KOMPLETTERING_INCONSISTENCY_ERROR.toString());
        }
    }

    @VisibleForTesting
    void validateThatCertificateExists(String certificateId, String civicRegistrationNumber, List<String> validationErrors) {
        Certificate certificate;
        try {
            certificate = certificateService.getCertificateForCare(certificateId);
            if (certificate == null) {
                validationErrors.add(ErrorCode.CERTIFICATE_NOT_FOUND_ERROR.toString());
                return;
            }
            Optional<Personnummer> suppliedCivicRegistrationNumber = Personnummer.createPersonnummer(civicRegistrationNumber);
            if (!(suppliedCivicRegistrationNumber.isPresent()
                && suppliedCivicRegistrationNumber.get().equals(certificate.getCivicRegistrationNumber()))) {
                validationErrors.add(ErrorCode.CIVIC_REGISTRATION_NUMBER_INCONSISTENCY_ERROR.toString());
            }
        } catch (InvalidCertificateException e) {
            validationErrors.add(e.getMessage());
        }
    }

    @VisibleForTesting
    void validateTestCertificate(String certificateId, List<String> validationErrors) {
        try {
            if (certificateService.isTestCertificate(certificateId)) {
                validationErrors.add(ErrorCode.TEST_CERTIFICATE.toString());
                validationErrors.add(" The supplied certificate is invalid. Messages cannot be sent for test certificates.");
            }
        } catch (InvalidCertificateException e) {
            validationErrors.add(e.getMessage());
        }
    }

    @VisibleForTesting
    void validatePaminnelse(SendMessageToCareType message, List<String> validationErrors) {
        boolean paminnelseSubjectMissing = !message.getAmne().getCode().equals(Amneskod.PAMINN.toString())
            && message.getPaminnelseMeddelandeId() != null;
        boolean paminnelseIdMissing = message.getAmne().getCode().equals(Amneskod.PAMINN.toString())
            && message.getPaminnelseMeddelandeId() == null;
        if (paminnelseSubjectMissing || paminnelseIdMissing) {
            validationErrors.add(ErrorCode.PAMINNELSE_ID_INCONSISTENCY_ERROR.toString());
        }

        if (isPaminnelse(message)) {
            Arende res = messageRepository.findByMeddelandeId(message.getPaminnelseMeddelandeId());

            if (res == null) {
                validationErrors.add(ErrorCode.REFERENCED_MESSAGE_NOT_FOUND_ERROR.toString());
                return;
            }
        }
    }

    private boolean isPaminnelse(SendMessageToCareType sendMessageToCareType) {
        return sendMessageToCareType.getAmne().getCode().equals(Amneskod.PAMINN.toString())
            && sendMessageToCareType.getPaminnelseMeddelandeId() != null;
    }

    private boolean messageIsAnAnswer(SendMessageToCareType sendMessageToCareType) {
        return sendMessageToCareType.getSvarPa() != null;
    }

    private boolean hasKomplettering(SendMessageToCareType sendMessageToCareType) {
        List<Komplettering> komplettering = sendMessageToCareType.getKomplettering();
        return komplettering != null && !komplettering.isEmpty();
    }
}
