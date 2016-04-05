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
package se.inera.intyg.intygstjanst.web.integration.validator;

import java.util.*;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.common.util.StringUtil;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.dto.InvalidPersonNummerException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.v2.MeddelandeReferens;

import com.google.common.annotations.VisibleForTesting;

@Component
public class SendMessageToCareValidator {
    public enum Amneskod {
        KOMPLT, ARBTID, AVSTMN, KONTKT, OVRIGT, PAMINN
    };

    public enum ErrorCode {
        SUBJECT_CONSISTENCY_ERROR, MESSAGE_TYPE_CONSISTENCY_ERROR, SUBJECT_NOT_SUPPORTED_ERROR, CERTIFICATE_NOT_FOUND_ERROR, CIVIC_REGISTRATION_NUMBER_INCONSISTENCY_ERROR, REFERENCED_MESSAGE_NOT_FOUND_ERROR, KOMPLETTERING_INCONSISTENCY_ERROR, PAMINNELSE_ID_INCONSISTENCY_ERROR, MEDDELANDE_ID_NOT_UNIQUE_ERROR
    }

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ArendeRepository messageRepository;

    public List<String> validateSendMessageToCare(SendMessageToCareType sendMessageToCareType) {
        List<String> validationErrors = new ArrayList<String>();
        String personnummeer = sendMessageToCareType.getPatientPersonId().getExtension();

        validateMeddelandeId(sendMessageToCareType.getMeddelandeId(), validationErrors);
        validateMessageSubject(sendMessageToCareType.getAmne().getCode(), validationErrors);
        validateThatCertificateExists(sendMessageToCareType.getIntygsId().getExtension(), personnummeer, validationErrors);
        validateConsistencyForQuestionVsAnswer(sendMessageToCareType, validationErrors);
        validatePaminnelseIdConsistency(sendMessageToCareType, validationErrors);
        validateConsistencyOfSubject(sendMessageToCareType, validationErrors);
        validateConsistencyForKomplettering(sendMessageToCareType, validationErrors);
        return validationErrors;

    }

    @VisibleForTesting
    void validateMeddelandeId(String meddelandeId, List<String> validationErrors) {
        if (StringUtil.isNullOrEmpty(meddelandeId) || !messageRepository.findByMeddelandeId(meddelandeId).isEmpty()) {
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
                    + "Supported subjects are KOMPLETTERING_AV_LAKARINTYG, MAKULERING_AV_LAKARINTYG, AVSTAMNINGSMOTE, KONTAKT, ARBETSTIDSFORLAGGNING, PAMINNELSE, OVRIGT");
        }
    }

    @VisibleForTesting
    void validateConsistencyForQuestionVsAnswer(SendMessageToCareType sendMessageToCareType, List<String> validationErrors) {
        LocalDate lastDayOfReply = sendMessageToCareType.getSistaDatumForSvar();
        if (lastDayOfReply != null && messageIsAnAnswer(sendMessageToCareType)) {
            validationErrors.add(ErrorCode.MESSAGE_TYPE_CONSISTENCY_ERROR.toString());
            validationErrors
                    .add(" Message concerning certificate id " + sendMessageToCareType.getIntygsId().getExtension() + " is an answer to a question"
                            + "and should not specify a last date of reply 'sistaSvarsDatum'.");
        }
    }

    @VisibleForTesting
    void validateConsistencyOfSubject(SendMessageToCareType sendMessageToCareType, List<String> validationErrors) {
        MeddelandeReferens meddelandeReferens = sendMessageToCareType.getSvarPa();
        if (meddelandeReferens != null) {
            String meddelandeId = meddelandeReferens.getMeddelandeId();
            List<Arende> res = messageRepository.findByMeddelandeId(meddelandeId);

            if (res == null || res.isEmpty()) {
                validationErrors.add(ErrorCode.REFERENCED_MESSAGE_NOT_FOUND_ERROR.toString());
                return;
            }
            String amne = res.get(0).getAmne();
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
            String foundCivicRegistrationNumber = certificate.getCivicRegistrationNumber().getNormalizedPnr();
            String suppliedCivicRegistrationNumber = new Personnummer(civicRegistrationNumber).getNormalizedPnr();
            if (!foundCivicRegistrationNumber.equals(suppliedCivicRegistrationNumber)) {
                validationErrors.add(ErrorCode.CIVIC_REGISTRATION_NUMBER_INCONSISTENCY_ERROR.toString());
            }
        } catch (InvalidCertificateException e) {
            validationErrors.add(e.getMessage());
        } catch (InvalidPersonNummerException e) {
            validationErrors.add(e.getMessage());
        }
    }

    @VisibleForTesting
    void validatePaminnelseIdConsistency(SendMessageToCareType message, List<String> validationErrors) {
        boolean paminnelseSubjectMissing = (!message.getAmne().getCode().equals(Amneskod.PAMINN.toString())) && (message.getPaminnelseMeddelandeId() != null);
        boolean paminnelseIdMissing = message.getAmne().getCode().equals(Amneskod.PAMINN.toString()) && (message.getPaminnelseMeddelandeId() == null);
        if (paminnelseSubjectMissing || paminnelseIdMissing) {
            validationErrors.add(ErrorCode.PAMINNELSE_ID_INCONSISTENCY_ERROR.toString());
        }
    }

    private boolean isPaminnelse(SendMessageToCareType sendMessageToCareType) {
        return sendMessageToCareType.getAmne().getCode().equals(Amneskod.PAMINN.toString()) && (sendMessageToCareType.getPaminnelseMeddelandeId() != null);
    }

    private boolean messageIsAnAnswer(SendMessageToCareType sendMessageToCareType) {
        MeddelandeReferens meddelandeReferens = sendMessageToCareType.getSvarPa();
        return (meddelandeReferens != null);
    }

    private boolean hasKomplettering(SendMessageToCareType sendMessageToCareType) {
        List<Komplettering> komplettering = sendMessageToCareType.getKomplettering();
        return komplettering != null && !komplettering.isEmpty();
    }
}
