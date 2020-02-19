/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatus.rivtabp20.v1.SetCertificateStatusResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusResponseType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class SetCertificateStatusResponderImpl implements SetCertificateStatusResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetCertificateStatusResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private RecipientService recipientService;

    @Override
    @PrometheusTimeMethod
    public SetCertificateStatusResponseType setCertificateStatus(AttributedURIType logicalAddress,
        SetCertificateStatusRequestType request) {

        SetCertificateStatusResponseType response = new SetCertificateStatusResponseType();

        String target = request.getTarget();
        // We need to translate FK->FKASSA as Försäkringskassan are still sending statuses with FK as target
        if ("FK".equals(target)) {
            target = recipientService.getPrimaryRecipientFkassa().getId();
        }

        try {
            target = recipientService.getRecipient(target).getId();

            certificateService.setCertificateState(
                createPnr(request.getNationalIdentityNumber()),
                request.getCertificateId(),
                target,
                CertificateState.valueOf(request.getStatus().name()),
                request.getTimestamp());

            response.setResult(ResultOfCallUtil.okResult());
            monitoringLogService.logCertificateStatusChanged(request.getCertificateId(), request.getStatus().name());

        } catch (RecipientUnknownException | InvalidCertificateException e) {
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
        } catch (TestCertificateException e) {
            LOGGER.error("Certificate '{}' couldn't be sent to recipient because it is a test certificate", request.getCertificateId());
            response.setResult(ResultOfCallUtil.failResult(
                "Cannot set the certificate to SENT as it is flagged as a test certificate"));
        }

        return response;
    }

    private Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new IllegalArgumentException("Could not parse passed personnummer"));
    }

}
