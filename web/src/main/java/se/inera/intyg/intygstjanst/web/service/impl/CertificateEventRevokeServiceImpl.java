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
import se.inera.intyg.intygstjanst.web.service.CertificateEventRevokeService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.service.dto.RevokedInformationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.StaffDTO;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

@Service
@Slf4j
public class CertificateEventRevokeServiceImpl implements CertificateEventRevokeService {

    private static final String HSA_ID_OID = "1.2.752.129.2.1.4.1";

    private final RevokeCertificateResponderInterface revokeCertificateResponderInterface;
    private final RecipientService recipientService;
    private final MonitoringLogService monitoringLogService;

    public CertificateEventRevokeServiceImpl(
        @Qualifier("revokeCertificateClient") RevokeCertificateResponderInterface revokeCertificateResponderInterface,
        RecipientService recipientService, MonitoringLogService monitoringLogService) {
        this.revokeCertificateResponderInterface = revokeCertificateResponderInterface;
        this.recipientService = recipientService;
        this.monitoringLogService = monitoringLogService;
    }

    @Override
    public void revoke(GetCertificateXmlResponse xmlResponse, String decodedXml) {
        try {
            final var logicalAddress = recipientService.getRecipient(xmlResponse.getRecipient().getId()).getLogicalAddress();
            final var request = getRequest(xmlResponse.getRevoked(), xmlResponse.getUnitId(), decodedXml);
            final var wsResponse = revokeCertificateResponderInterface.revokeCertificate(logicalAddress, request);
            handleResponse(wsResponse, xmlResponse);
        } catch (SOAPFaultException | RecipientUnknownException e) {
            throw new IllegalStateException(e);
        }
    }

    private static RevokeCertificateType getRequest(RevokedInformationDTO revokedInformation, String unitId,
        String decodedXml) {
        final var jaxbElement = XmlMarshallerHelper.unmarshal(decodedXml);
        final var request = (RevokeCertificateType) jaxbElement.getValue();
        request.setSkickatTidpunkt(revokedInformation.getRevokedAt());
        request.setMeddelande(revokedInformation.getMessage());
        updateRevokedBy(request.getSkickatAv(), revokedInformation.getRevokedBy(), unitId);

        return request;
    }

    private static void updateRevokedBy(HosPersonal staff, StaffDTO revokedBy, String unitId) {
        final var hsaId = new HsaId();
        final var unit = new Enhet();
        final var unitHsaId = new HsaId();

        unitHsaId.setRoot(unitId);
        unit.setEnhetsId(unitHsaId);

        hsaId.setExtension(revokedBy.getPersonId());
        hsaId.setRoot(HSA_ID_OID);

        staff.setFullstandigtNamn(revokedBy.getFullName());
        staff.setForskrivarkod(revokedBy.getPrescriptionCode());
        staff.setEnhet(unit);
        staff.setPersonalId(hsaId);
    }

    private void handleResponse(RevokeCertificateResponseType wsResponse, GetCertificateXmlResponse xmlResponse) {
        final var certificateId = xmlResponse.getCertificateId();
        final var certificateType = xmlResponse.getCertificateType();
        final var recipient = xmlResponse.getRecipient().getId();
        final var unit = xmlResponse.getUnitId();

        if (wsResponse.getResult() == null) {
            throw new IllegalStateException(getResultNullMessage(certificateId, certificateType, recipient));
        }

        final var result = wsResponse.getResult().getResultCode();
        if (result == ResultCodeType.OK) {
            monitoringLogService.logCertificateRevoked(certificateId, certificateType, unit);
            return;
        }

        final var message = wsResponse.getResult().getResultText();
        if (result == ResultCodeType.INFO) {
            log.info(getInfoMessage(certificateId, certificateType, recipient, message));
            monitoringLogService.logCertificateRevoked(certificateId, certificateType, unit);
            return;
        }
        if (result == ResultCodeType.ERROR) {
            throw new IllegalStateException(getErrorMessage(certificateId, certificateType, recipient, message));
        }
    }

    private String getResultNullMessage(String certificateId, String certificateType, String recipient) {
        return String.format(
            "Revoke certificate received null result for certificate '%s' of type '%s' sent to recipient '%s'.",
            certificateId, certificateType, recipient
        );
    }

    private String getInfoMessage(String certificateId, String certificateType, String recipient, String message) {
        return String.format(
            "ResultCode INFO received when revoking certificate '%s' of type '%s' to recipient '%s'. Info message: '%s'.",
            certificateId, certificateType, recipient, message
        );
    }

    private String getErrorMessage(String certificateId, String certificateType, String recipient, String message) {
        return String.format(
            "ResultCode ERROR received when revoking certificate '%s' of type '%s' to recipient '%s'. Error message: '%s'.",
            certificateId, certificateType, recipient, message
        );
    }
}
