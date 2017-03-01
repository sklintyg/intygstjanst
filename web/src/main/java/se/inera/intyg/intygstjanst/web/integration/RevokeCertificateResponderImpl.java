/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.fkparent.support.ResultTypeUtil;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.SjukfallCertificateService;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v1.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v1.RevokeCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v1.RevokeCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;

@Transactional
@SchemaValidation
public class RevokeCertificateResponderImpl implements RevokeCertificateResponderInterface {
    private static final Logger LOG = LoggerFactory.getLogger(RevokeCertificateResponderImpl.class);

    @Autowired
    private MonitoringLogService monitoringService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private SjukfallCertificateService sjukfallCertificateService;

    @Autowired
    private RecipientService recipientService;

    @Autowired
    @Qualifier("revokeCertificateClient")
    private RevokeCertificateResponderInterface externalRevokeClient;

    @Override
    public RevokeCertificateResponseType revokeCertificate(String logicalAddress, RevokeCertificateType request) {
        RevokeCertificateResponseType response = new RevokeCertificateResponseType();

        Personnummer pnr = new Personnummer(request.getPatientPersonId().getExtension());
        String certificateId = request.getIntygsId().getExtension();

        try {
            Certificate certificate = certificateService.revokeCertificate(pnr, certificateId);

            nofifyStakeholders(request, certificate);

            monitoringService.logCertificateRevoked(certificate.getId(), certificate.getType(),
                    certificate.getCareUnitId());

            response.setResult(ResultTypeUtil.okResult());
        } catch (InvalidCertificateException e) {
            // Send APPLICATION_ERROR to trigger retransmission in the client. This is because this revoke request
            // could arrive before the register request and we want to avoid race conditions.
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR,
                    "Certificate " + certificateId + " does not exist for patient."));
            LOG.warn("Certificate '{}' does not exist for patient '{}'.", certificateId, pnr.getPnrHash());
        } catch (CertificateRevokedException e) {
            response.setResult(ResultTypeUtil.infoResult("Certificate " + certificateId + " is already revoked."));
            LOG.warn("Certificate '{}' already revoked.", certificateId);
        }

        return response;
    }

    private void nofifyStakeholders(RevokeCertificateType request, Certificate certificate) {
        certificate.getStates().stream()
                .filter(entry -> CertificateState.SENT.equals(entry.getState()))
                .map(CertificateStateHistoryEntry::getTarget)
                .distinct()
                .forEach(recipient -> {
                    try {
                        externalRevokeClient.revokeCertificate(recipientService.getRecipient(recipient).getLogicalAddress(), request);
                    } catch (RecipientUnknownException e) {
                        LOG.warn("Could not find the logicalAddress to send revoke to {}", recipient);
                    }
                });

        certificateService.revokeCertificateForStatistics(certificate);
        sjukfallCertificateService.revoked(certificate);
    }
}
