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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToCareValidator.Amneskod;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.SendMessageToRecipientType;

@Component
public class SendMessageToRecipientValidator {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ArendeRepository messageRepository;

    public List<String> validate(SendMessageToRecipientType message) {
        List<String> validationErrors = new ArrayList<String>();

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
        boolean paminnelseMeddelandeIdPresent = StringUtils.isNotEmpty(message.getPaminnelseMeddelandeId());
        if (paminnelseMeddelandeIdPresent && !isPaminnelse) {
            validationErrors.add("paminnelseMeddelandeId should only be present if amne is PAMINN");
        } else if (isPaminnelse && !paminnelseMeddelandeIdPresent) {
            validationErrors.add("PAMINN should define paminnelseMeddelandeId");
        } else if (paminnelseMeddelandeIdPresent) {
            List<Arende> res = messageRepository.findByMeddelandeId(message.getPaminnelseMeddelandeId());
            if (CollectionUtils.isEmpty(res)) {
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
        List<Arende> res = messageRepository.findByMeddelandeId(message.getMeddelandeId());
        if (CollectionUtils.isNotEmpty(res)) {
            validationErrors.add("MeddelandeId is not globally unique");
        }
    }

    private void validateSvarPa(SendMessageToRecipientType message, List<String> validationErrors) {
        if (message.getSvarPa() != null) {
            String svarPaMeddelandeId = message.getSvarPa().getMeddelandeId();
            List<Arende> res = messageRepository.findByMeddelandeId(svarPaMeddelandeId);
            if (CollectionUtils.isEmpty(res)) {
                validationErrors.add("SvarPa Meddelande does not exist");
            } else if (message.getAmne().getCode() != null && !message.getAmne().getCode().equals(res.get(0).getAmne())) {
                validationErrors.add("Svar amne is not consistent with question");
            }
        }
    }

    private void validateCertificate(SendMessageToRecipientType message, List<String> validationErrors) {
        try {
            Personnummer messageCRN = new Personnummer(message.getPatientPersonId().getExtension());
            Certificate certificate = certificateService.getCertificateForCare(message.getIntygsId().getExtension());
            if (!messageCRN.equals(certificate.getCivicRegistrationNumber())) {
                validationErrors.add("PatientPersonId is not consistent with certificate");
            }
        } catch (InvalidCertificateException e) {
            validationErrors.add("Intyg does not exist");
        }
    }

    private boolean messageIsQuestion(SendMessageToRecipientType message) {
        return (message.getSvarPa() == null && message.getPaminnelseMeddelandeId() == null);
    }

}