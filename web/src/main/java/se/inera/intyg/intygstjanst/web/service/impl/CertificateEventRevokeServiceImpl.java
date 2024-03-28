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
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.CertificateEventRevokeService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.StaffDTO;
import se.inera.intyg.intygstjanst.web.service.dto.UnitDTO;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.revokeCertificate.v2.RevokeCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.ArbetsplatsKod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Vardgivare;

@Service
@Slf4j
public class CertificateEventRevokeServiceImpl implements CertificateEventRevokeService {

    private static final String HSA_ID_OID = "1.2.752.129.2.1.4.1";
    private static final String PERSON_ID_OID = "1.2.752.129.2.1.3.1";
    private static final String SAMORDNING_ID_OID = "1.2.752.129.2.1.3.3";
    private static final String ARBETSPLATS_KOD_OID = "1.2.752.29.4.71";

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
    public void revoke(GetCertificateXmlResponse xmlResponse) {
        try {
            final var logicalAddress = recipientService.getRecipient(xmlResponse.getRecipient().getId()).getLogicalAddress();
            final var request = getRequest(xmlResponse);
            final var wsResponse = revokeCertificateResponderInterface.revokeCertificate(logicalAddress, request);
            handleResponse(wsResponse, xmlResponse);
        } catch (SOAPFaultException | RecipientUnknownException e) {
            throw new IllegalStateException(e);
        }
    }

    private static RevokeCertificateType getRequest(GetCertificateXmlResponse response) {
        final var revokedInformation = response.getRevoked();

        final var request = new RevokeCertificateType();
        final var id = getCertificateId(response.getCertificateId(), response.getUnit().getUnitId());
        request.setSkickatTidpunkt(revokedInformation.getRevokedAt());
        request.setMeddelande(revokedInformation.getMessage());
        request.setSkickatAv(
            getRevokedBy(revokedInformation.getRevokedBy(), response.getUnit(), response.getCareProvider())
        );
        request.setIntygsId(id);
        request.setPatientPersonId(getPatientId(response.getPatientId()));
        return request;
    }

    private static PersonId getPatientId(PersonIdDTO patientId) {
        final var personId = new PersonId();

        personId.setRoot(patientId.getType() == PersonIdTypeDTO.PERSONAL_IDENTITY_NUMBER ? PERSON_ID_OID : SAMORDNING_ID_OID);
        personId.setExtension(patientId.getId());

        return personId;
    }

    private static IntygId getCertificateId(String certificateId, String unitId) {
        final var id = new IntygId();

        id.setRoot(unitId);
        id.setExtension(certificateId);

        return id;
    }

    private static HosPersonal getRevokedBy(StaffDTO revokedBy, UnitDTO unit, UnitDTO careProvider) {
        final var staff = new HosPersonal();

        final var hsaId = new HsaId();
        hsaId.setRoot(HSA_ID_OID);
        hsaId.setExtension(revokedBy.getPersonId());

        staff.setFullstandigtNamn(revokedBy.getFullName());
        staff.setForskrivarkod(revokedBy.getPrescriptionCode());
        staff.setEnhet(getUnit(unit, careProvider));
        staff.setPersonalId(hsaId);
        return staff;
    }

    private static Enhet getUnit(UnitDTO unit, UnitDTO careProvider) {
        final var enhet = new Enhet();

        enhet.setEnhetsnamn(unit.getUnitName());
        enhet.setEpost(unit.getEmail());
        enhet.setPostadress(unit.getAddress());
        enhet.setPostnummer(unit.getZipCode());
        enhet.setPostort(unit.getCity());
        enhet.setTelefonnummer(unit.getPhoneNumber());
        enhet.setVardgivare(getCareProvider(careProvider));

        final var workplaceCode = new ArbetsplatsKod();
        workplaceCode.setRoot(ARBETSPLATS_KOD_OID);
        workplaceCode.setExtension(unit.getWorkplaceCode());
        enhet.setArbetsplatskod(workplaceCode);

        final var unitHsaId = new HsaId();
        unitHsaId.setRoot(HSA_ID_OID);
        unitHsaId.setExtension(unit.getUnitId());
        enhet.setEnhetsId(unitHsaId);

        return enhet;
    }

    private static Vardgivare getCareProvider(UnitDTO careProvider) {
        final var vardgivare = new Vardgivare();

        final var id = new HsaId();
        id.setRoot(HSA_ID_OID);
        id.setExtension(careProvider.getUnitId());

        vardgivare.setVardgivarnamn(careProvider.getUnitName());
        vardgivare.setVardgivareId(id);

        return vardgivare;
    }

    private void handleResponse(RevokeCertificateResponseType wsResponse, GetCertificateXmlResponse xmlResponse) {
        final var certificateId = xmlResponse.getCertificateId();
        final var certificateType = xmlResponse.getCertificateType();
        final var recipient = xmlResponse.getRecipient().getId();
        final var unit = xmlResponse.getUnit().getUnitId();

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
