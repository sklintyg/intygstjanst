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
// CHECKSTYLE:OFF LineLength

import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.OK;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.MissingModuleException;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.exception.SubsystemCallException;
import se.inera.intyg.intygstjanst.web.service.CertificateSenderService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.SoapIntegrationService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;

// CHECKSTYLE:ON LineLength

/**
 * @author andreaskaltenbach
 */
@Service
public class CertificateSenderServiceImpl implements CertificateSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateSenderServiceImpl.class);

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    SoapIntegrationService soapIntegrationService;

    @Override
    public void sendCertificate(Certificate certificate, String recipientId) {
        try {
            ModuleEntryPoint module = moduleRegistry.getModuleEntryPoint(certificate.getType());
            CertificateType certType = new CertificateType(certificate.getType());
            // Use target from parameter if present, otherwise use the default receiver from the module's entryPoint.
            String logicalAddress;

            if (recipientId == null) {
                logicalAddress = module.getDefaultRecipient();

            } else {
                Recipient recipient = recipientService.getRecipient(recipientId);
                // Check that the recipient is valid for certType and is active
                if (recipientService.listRecipients(certType).contains(recipient) && recipient.isActive()) {
                    logicalAddress = recipient.getLogicalAddress();
                } else {
                    LOGGER.error("Recipient {} is not available for certificate type {}", recipientId, certType.toString());
                    throw new ServerException(
                        String.format("Recipient %s is not available for certificate type %s", recipientId, certType.toString()));
                }
            }

            soapIntegrationService.sendCertificateToRecipient(certificate, logicalAddress, recipientId);

            monitoringLogService.logCertificateSent(certificate.getId(), certificate.getType(), certificate.getCareUnitId(), recipientId);

        } catch (ModuleNotFoundException e) {
            LOGGER.error("The module '{}' was not found - not registered in application", certificate.getType());
            throw new MissingModuleException(String.format("The module '%s' was not found - not registered in application",
                certificate.getType()), e);

        } catch (ModuleException e) {
            String message = String.format("Failed to send certificate '%s' of type '%s' to recipient '%s'", certificate.getId(),
                certificate.getType(), recipientId);
            throw new ServerException(message, e);

        } catch (RecipientUnknownException e) {
            String message = String.format("Found no matching recipient for logical adress: '%s'", recipientId);
            LOGGER.error(e.getMessage());
            throw new ServerException(message, e);
        }
    }

    @Override
    public void sendCertificateRevocation(Certificate certificate, String recipientId, RevokeType revokeData) {
        RevokeMedicalCertificateRequestType request = new RevokeMedicalCertificateRequestType();
        request.setRevoke(revokeData);

        AttributedURIType logicalAddress = getLogicalAddress(recipientId);

        final var sendResponse = soapIntegrationService.revokeMedicalCertificate(logicalAddress, request);

        if (sendResponse.getResult().getResultCode() != OK) {
            String message = "Failed to send question to '" + recipientId + "' when revoking certificate '" + certificate.getId()
                + "'. Info from recipient: " + sendResponse.getResult().getInfoText();
            LOGGER.error(message);
            throw new SubsystemCallException(recipientId, message);
        } else {
            monitoringLogService.logCertificateRevokeSent(certificate.getId(), certificate.getType(), certificate.getCareUnitId(),
                recipientId);
        }
    }

    private AttributedURIType getLogicalAddress(String recipientId) {

        try {
            Recipient recipient = recipientService.getRecipient(recipientId);

            AttributedURIType logicalAddress = new AttributedURIType();
            logicalAddress.setValue(recipient.getLogicalAddress());

            return logicalAddress;

        } catch (RecipientUnknownException rue) {
            LOGGER.error(rue.getMessage());
            throw new RuntimeException(rue);
        }
    }
}
