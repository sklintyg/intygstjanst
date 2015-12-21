/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.common.util.logging.HashUtility;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;

public class SendCertificateToRecipientResponderImpl implements SendCertificateToRecipientResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendCertificateToRecipientResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    public SendCertificateToRecipientResponseType sendCertificateToRecipient(String logicalAddress, SendCertificateToRecipientType request) {

        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();

        final String mottagareId = request.getMottagareId();
        final Personnummer personnummer = new Personnummer(request.getPersonId());
        try {
            // 1. Skicka certifikat till mottagaren
            CertificateService.SendStatus sendStatus = certificateService.sendCertificate(personnummer, request.getUtlatandeId(),
                    mottagareId);

            final String mottagareIdHash = HashUtility.hash(mottagareId);
            if (sendStatus == CertificateService.SendStatus.ALREADY_SENT) {
                response.setResult(ResultTypeUtil.infoResult(String.format("Certificate '%s' already sent to '%s'.", request.getUtlatandeId(),
                        mottagareId)));
                LOGGER.info("Certificate '{}' already sent to '{}'.", request.getUtlatandeId(), mottagareIdHash);
            } else {
                response.setResult(ResultTypeUtil.okResult());
                LOGGER.info("Certificate '{}' sent to '{}'.", request.getUtlatandeId(), mottagareIdHash);
            }

        } catch (InvalidCertificateException ex) {
            // return ERROR if no such certificate does exist
            LOGGER.error("Certificate '{}' does not exist for user '{}'.", request.getUtlatandeId(), personnummer.getPnrHash());
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR,
                    String.format("Unknown certificate ID: %s", request.getUtlatandeId())));
        } catch (CertificateRevokedException ex) {
            // return INFO if certificate is revoked
            LOGGER.info("Certificate '%s' has been revoked.", request.getUtlatandeId());
            response.setResult(ResultTypeUtil.infoResult(String.format("Certificate '%s' has been revoked.", request.getUtlatandeId())));
        } catch (RecipientUnknownException ex) {
            // return ERROR if recipient is unknwon
            LOGGER.error("Unknown recipient ID: {}", mottagareId);
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR,
                    String.format("Unknown recipient ID: %s", mottagareId)));
        } catch (ServerException ex) {
            Throwable cause = ex.getCause();
            String message = (cause instanceof ExternalServiceCallException) ? cause.getMessage() : ex.getMessage();
            // return ERROR if certificate couldn't be sent
            LOGGER.error("Certificate '{}' couldn't be sent to '{}': {}", request.getUtlatandeId(), mottagareId, message);
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.TECHNICAL_ERROR,
                    String.format("Certificate '%s' couldn't be sent to recipient", request.getUtlatandeId())));
        }

        return response;
    }
}
