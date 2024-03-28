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

import javax.xml.ws.soap.SOAPFaultException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.CertificateEventSendService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.dto.GetCertificateXmlResponse;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@Service
@Slf4j
public class CertificateEventSendServiceImpl implements CertificateEventSendService {

    private final RegisterCertificateResponderInterface registerCertificateResponderInterface;
    private final RecipientService recipientService;
    private final MonitoringLogService monitoringLogService;

    public CertificateEventSendServiceImpl(
        @Qualifier("registerCertificateClient") RegisterCertificateResponderInterface registerCertificateResponderInterface,
        RecipientService recipientService, MonitoringLogService monitoringLogService) {
        this.registerCertificateResponderInterface = registerCertificateResponderInterface;
        this.recipientService = recipientService;
        this.monitoringLogService = monitoringLogService;
    }

    @Override
    public void send(GetCertificateXmlResponse xmlResponse, String xml) {
        try {
            final var logicalAddress = recipientService.getRecipient(xmlResponse.getRecipient().getId()).getLogicalAddress();
            final var element = XmlMarshallerHelper.unmarshal(xml);
            final var request = (RegisterCertificateType) element.getValue();
            final var wsResponse = registerCertificateResponderInterface.registerCertificate(logicalAddress, request);
            handleResponse(wsResponse, xmlResponse);
        } catch (SOAPFaultException | RecipientUnknownException e) {
            throw new IllegalStateException(e);
        }
    }

    private void handleResponse(RegisterCertificateResponseType wsResponse, GetCertificateXmlResponse xmlResponse) {
        final var certificateId = xmlResponse.getCertificateId();
        final var certificateType = xmlResponse.getCertificateType();
        final var recipient = xmlResponse.getRecipient().getId();
        final var unit = xmlResponse.getUnit().getId();

        if (wsResponse.getResult() == null) {
            throw new IllegalStateException(getResultNullMessage(certificateId, certificateType, recipient));
        }

        final var result = wsResponse.getResult().getResultCode();
        if (result == ResultCodeType.OK) {
            monitoringLogService.logCertificateSent(certificateId, certificateType, unit, recipient);
            return;
        }

        final var message = wsResponse.getResult().getResultText();
        if (result == ResultCodeType.INFO) {
            log.info(getInfoMessage(certificateId, certificateType, recipient, message));
            monitoringLogService.logCertificateSent(certificateId, certificateType, unit, recipient);
            return;
        }
        if (result == ResultCodeType.ERROR) {
            throw new IllegalStateException(getErrorMessage(certificateId, certificateType, recipient, message));
        }
    }

    private String getResultNullMessage(String certificateId, String certificateType, String recipient) {
        return String.format("Send certificate received null result for certificate '%s' of type '%s' sent to recipient '%s'.",
            certificateId, certificateType, recipient);
    }

    private String getInfoMessage(String certificateId, String certificateType, String recipient, String message) {
        return String.format("ResultCode INFO received when sending certificate '%s' of type '%s' to recipient '%s'. Info message: '%s'.",
            certificateId, certificateType, recipient, message);
    }

    private String getErrorMessage(String certificateId, String certificateType, String recipient, String message) {
        return String.format("ResultCode ERROR received when sending certificate '%s' of type '%s' to recipient '%s'. Error message: '%s'.",
            certificateId, certificateType, recipient, message);
    }
}
