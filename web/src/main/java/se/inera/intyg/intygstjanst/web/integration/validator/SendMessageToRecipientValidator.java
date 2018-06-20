/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.persistence.model.repository.ArendeRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator.Amneskod;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SendMessageToRecipientValidator {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ArendeRepository messageRepository;

    public List<String> validate(SendMessageToRecipientType message) throws InvalidCertificateException {
        List<String> validationErrors = new ArrayList<>();

        Amneskod amne = validateAmneskod(message, validationErrors);
        validateSistaDatumForSvar(message, validationErrors);
        validateMeddelandeId(message, validationErrors);
        validatePaminnelseMeddelandeId(message, amne, validationErrors);
        validateSvarPa(message, validationErrors);
        validateCertificate(message, validationErrors);

        return validationErrors;
    }

    private Amneskod validateAmneskod(SendMessageToRecipientType message, List<String> validationErrors) {
        try {
            return Amneskod.valueOf(message.getAmne().getCode());
        } catch (Exception e) {
            validationErrors.add("Invalid amneskod");
            return null;
        }
    }

    private void validatePaminnelseMeddelandeId(SendMessageToRecipientType message, Amneskod amne, List<String> validationErrors) {
        boolean isPaminnelse = Amneskod.PAMINN.equals(amne);
        boolean paminnelseMeddelandeIdPresent = !Strings.isNullOrEmpty(message.getPaminnelseMeddelandeId());
        if (paminnelseMeddelandeIdPresent && !isPaminnelse) {
            validationErrors.add("paminnelseMeddelandeId should only be present if amne is PAMINN");
        } else if (isPaminnelse && !paminnelseMeddelandeIdPresent) {
            validationErrors.add("PAMINN should define paminnelseMeddelandeId");
        } else if (paminnelseMeddelandeIdPresent) {
            Arende res = messageRepository.findByMeddelandeId(message.getPaminnelseMeddelandeId());
            if (res == null) {
                validationErrors.add("Paminnelse Meddelande does not exist");
            }
        }
    }

    private void validateSistaDatumForSvar(SendMessageToRecipientType message, List<String> validationErrors) {
        if (message.getSistaDatumForSvar() != null && !messageIsQuestion(message)) {
            validationErrors.add("SistaDatumForSvar is only valid on Questions");
        }
    }

    private void validateMeddelandeId(SendMessageToRecipientType message, List<String> validationErrors) {
        Arende res = messageRepository.findByMeddelandeId(message.getMeddelandeId());
        if (res != null) {
            validationErrors.add("MeddelandeId is not globally unique");
        }
    }

    private void validateSvarPa(SendMessageToRecipientType message, List<String> validationErrors) {
        if (message.getSvarPa() != null) {
            String svarPaMeddelandeId = message.getSvarPa().getMeddelandeId();
            Arende res = messageRepository.findByMeddelandeId(svarPaMeddelandeId);
            if (res == null) {
                validationErrors.add("SvarPa Meddelande does not exist");
            } else if (message.getAmne().getCode() != null && !message.getAmne().getCode().equals(res.getAmne())) {
                validationErrors.add("Svar amne is not consistent with question");
            }
        }
    }

    private void validateCertificate(SendMessageToRecipientType message, List<String> validationErrors) throws InvalidCertificateException {
        Optional<Personnummer> messageCRN = Personnummer.createPersonnummer(message.getPatientPersonId().getExtension());
        Certificate certificate = certificateService.getCertificateForCare(message.getIntygsId().getExtension());
        if (!(messageCRN.isPresent() && messageCRN.get().equals(certificate.getCivicRegistrationNumber()))) {
            validationErrors.add("PatientPersonId is not consistent with certificate");
        }
    }

    private boolean messageIsQuestion(SendMessageToRecipientType message) {
        return message.getSvarPa() == null && message.getPaminnelseMeddelandeId() == null;
    }

}
