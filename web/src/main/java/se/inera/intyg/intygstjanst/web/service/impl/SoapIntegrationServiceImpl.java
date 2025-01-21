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

package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.logging.MdcLogConstants;
import se.inera.intyg.intygstjanst.logging.PerformanceLogging;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.service.SoapIntegrationService;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;

@Service
public class SoapIntegrationServiceImpl implements SoapIntegrationService {


    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    @Qualifier("revokeCertificateClient")
    private RevokeCertificateResponderInterface revokeCertificateResponder;

    @Autowired
    private RevokeMedicalCertificateResponderInterface revokeMedicalCertificateResponder;

    @Autowired
    @Qualifier("sendMessageToRecipientClient")
    private SendMessageToRecipientResponderInterface sendMessageToRecipientResponder;

    @Autowired
    @Qualifier("sendMessageToCareClient")
    private SendMessageToCareResponderInterface sendMessageToCareResponder;

    @Override
    @PerformanceLogging(eventAction = "send-certificate-to-recipient", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public void sendCertificateToRecipient(Certificate certificate, String logicalAddress, String recipientId)
        throws ModuleNotFoundException, ModuleException {
        moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion())
            .sendCertificateToRecipient(certificate.getOriginalCertificate().getDocument(), logicalAddress, recipientId);
    }

    @Override
    @PerformanceLogging(eventAction = "revoke-certificate-notify-recipient", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public RevokeCertificateResponseType revokeCertificate(String logicalAddress, RevokeCertificateType revokeCertificateType) {
        return revokeCertificateResponder.revokeCertificate(logicalAddress, revokeCertificateType);
    }

    @Override
    @PerformanceLogging(eventAction = "revoke-medical-certificate-notify-recipient", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public RevokeMedicalCertificateResponseType revokeMedicalCertificate(AttributedURIType logicalAddress,
        RevokeMedicalCertificateRequestType revokeMedicalCertificateRequestType) {
        return revokeMedicalCertificateResponder.revokeMedicalCertificate(logicalAddress, revokeMedicalCertificateRequestType);
    }

    @Override
    @PerformanceLogging(eventAction = "send-message-to-recipient", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public SendMessageToRecipientResponseType sendMessageToRecipient(String logicalAddress,
        SendMessageToRecipientType sendMessageToRecipientType) {
        return sendMessageToRecipientResponder.sendMessageToRecipient(logicalAddress, sendMessageToRecipientType);
    }

    @Override
    @PerformanceLogging(eventAction = "send-message-to-care", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress,
        SendMessageToCareType sendMessageToCareType) {
        return sendMessageToCareResponder.sendMessageToCare(logicalAddress, sendMessageToCareType);
    }

}
