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
package se.inera.intyg.intygstjanst.web.integration;

import static com.google.common.base.Preconditions.checkNotNull;

import jakarta.xml.bind.JAXBException;
import java.util.List;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.integration.converter.ArendeConverter;
import se.inera.intyg.intygstjanst.web.integration.validator.SendMessageToRecipientValidator;
import se.inera.intyg.intygstjanst.web.service.ArendeService;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@SchemaValidation
public class SendMessageToRecipientResponderImpl implements SendMessageToRecipientResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(SendMessageToRecipientResponderImpl.class);

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private SendMessageToRecipientValidator validator;

    @Autowired
    private ArendeService arendeService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    @Qualifier("sendMessageToRecipientClient")
    private SendMessageToRecipientResponderInterface sendMessageToRecipientResponder;

    @Override
    @PrometheusTimeMethod
    public SendMessageToRecipientResponseType sendMessageToRecipient(String logicalAddress, SendMessageToRecipientType parameters) {
        checkNotNull(parameters);

        LOG.debug("Send message to recipient request received. logicalAddress={} messageId={}", logicalAddress,
            parameters.getMeddelandeId());

        SendMessageToRecipientResponseType response;

        try {
            List<String> validationErrors = validator.validate(parameters);
            if (!validationErrors.isEmpty()) {
                LOG.warn("Invalid parameters: ", validationErrors.toString());
                SendMessageToRecipientResponseType resp = new SendMessageToRecipientResponseType();
                resp.setResult(ResultTypeUtil.errorResult(ErrorIdType.VALIDATION_ERROR, validationErrors.toString()));
                return resp;
            }

            if (certificateService.isTestCertificate(parameters.getIntygsId().getExtension())) {
                response = new SendMessageToRecipientResponseType();
                response.setResult(ResultTypeUtil.okResult());
            } else {
                response = sendMessageToRecipientResponder.sendMessageToRecipient(parameters.getLogiskAdressMottagare(), parameters);
            }

            if (response.getResult().getResultCode() != ResultCodeType.ERROR) {
                monitoringLog.logSendMessageToRecipient(parameters.getMeddelandeId(), parameters.getLogiskAdressMottagare());
                try {
                    // try saving message in db, but always return response from recipient
                    arendeService.processIncomingMessage(ArendeConverter.convertSendMessageToRecipient(parameters));
                } catch (JAXBException e) {
                    LOG.error("JAXB error in SendMessageToRecipientResponder: {}", e.getMessage());
                } catch (Exception e) {
                    LOG.error("Exception caught when saving messageToRecipient in db: {}", e.getMessage());
                }
            }

        } catch (InvalidCertificateException e) {
            SendMessageToRecipientResponseType resp = new SendMessageToRecipientResponseType();
            resp.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "Intyg does not exist"));
            return resp;
        }

        return response;
    }
}
