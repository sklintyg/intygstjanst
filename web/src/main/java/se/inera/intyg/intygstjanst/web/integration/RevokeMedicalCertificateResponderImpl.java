/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.validate.CertificateValidationException;
import se.inera.intyg.infra.monitoring.logging.LogMarkers;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.exception.SubsystemCallException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.integration.validator.RevokeRequestValidator;
import se.inera.intyg.intygstjanst.web.service.*;
import se.inera.intyg.schemas.contract.Personnummer;

public class RevokeMedicalCertificateResponderImpl implements RevokeMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeMedicalCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private CertificateSenderService senderService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    protected StatisticsService statisticsService;

    @Autowired
    protected SjukfallCertificateService sjukfallCertificateService;

    @Autowired
    private RecipientService recipientService;

    @Transactional
    @Override
    @PrometheusTimeMethod
    public RevokeMedicalCertificateResponseType revokeMedicalCertificate(AttributedURIType logicalAddress,
        RevokeMedicalCertificateRequestType request) {

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();

        final Optional<Personnummer> personnummer = safeGetCivicRegistrationNumber(request);

        final String certId = safeGetCertificateId(request);
        final String pnrHash = getPersonnummerHash(personnummer, request);

        try {
            new RevokeRequestValidator(request.getRevoke()).validateAndCorrect();

            Certificate certificate = certificateService.revokeCertificate(personnummer.orElse(null), certId);

            if (!certificate.isTestCertificate()) {
                certificate.getStates().stream()
                    .filter(entry -> CertificateState.SENT.equals(entry.getState()))
                    .map(CertificateStateHistoryEntry::getTarget)
                    .distinct()
                    .filter(this::shallRevokeBeSentToRecipient)
                    .forEach(recipient -> senderService.sendCertificateRevocation(certificate, recipient, request.getRevoke()));
            }

            certificateService.revokeCertificateForStatistics(certificate);
            sjukfallCertificateService.revoked(certificate);
            monitoringLogService.logCertificateRevoked(certificate.getId(), certificate.getType(), certificate.getCareUnitId());

        } catch (InvalidCertificateException e) {
            // return with ERROR response if certificate was not found
            LOGGER.info("Tried to revoke certificate '" + certId + "' for patient '"
                + pnrHash + "' but certificate does not exist");
            response.setResult(ResultOfCallUtil.failResult("No certificate '" + certId
                + "' found to revoke for patient '" + pnrHash + "'."));
            return response;

        } catch (CertificateRevokedException e) {
            // return with INFO response if certificate was revoked before
            LOGGER.info("Tried to revoke certificate '" + certId + "' for patient '"
                + pnrHash + "' which already is revoked");
            response.setResult(ResultOfCallUtil.infoResult("Certificate '" + certId + "' is already revoked."));
            return response;

        } catch (CertificateValidationException e) {
            // return with ERROR response if certificate had validation errors
            LOGGER.info(LogMarkers.VALIDATION, "Validation error found for revoke certificate '" + certId
                + "' issued by '" + safeGetIssuedBy(request) + "' for patient '" + pnrHash + ": " + e.getMessage());
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            return response;

        } catch (SubsystemCallException e) {
            LOGGER.warn("Encountered an exception when sending a revocation to subsystem '" + e.getSubsystemId() + "'");
            response.setResult(
                ResultOfCallUtil
                    .failResult("Informing subsystem '" + e.getSubsystemId() + "' about revoked certificate resulted in error"));
            return response;
        } catch (TestCertificateException e) {
            LOGGER.error("Failed to revoke test certificate '{}' because '{}", certId, e.getMessage());
            response.setResult(ResultOfCallUtil.applicationErrorResult(
                "Failed to revoke test certificate due to following error: " + e.getMessage()));
        }

        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }

    private boolean shallRevokeBeSentToRecipient(String recipient) {
        return !recipient.equals(recipientService.getPrimaryRecipientFkassa().getId());
    }

    protected String safeGetCertificateId(RevokeMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getRevoke() != null && request.getRevoke().getLakarutlatande() != null
            && request.getRevoke().getLakarutlatande().getLakarutlatandeId() != null) {
            return request.getRevoke().getLakarutlatande().getLakarutlatandeId();
        }
        return null;
    }

    protected Optional<Personnummer> safeGetCivicRegistrationNumber(RevokeMedicalCertificateRequestType request) {
        final String patientId = getPatientId(getPatient(request));
        if (StringUtils.isNotEmpty(patientId)) {
            return Personnummer.createPersonnummer(patientId);
        }
        return Optional.empty();
    }

    protected String safeGetIssuedBy(RevokeMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getRevoke().getAdressVard() != null
            && request.getRevoke().getAdressVard().getHosPersonal() != null
            && request.getRevoke().getAdressVard().getHosPersonal().getEnhet() != null
            && request.getRevoke().getAdressVard().getHosPersonal().getEnhet().getEnhetsId() != null) {
            return request.getRevoke().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().getExtension();
        }
        return null;
    }

    private PatientType getPatient(RevokeMedicalCertificateRequestType request) {
        LakarutlatandeEnkelType utlatande = request.getRevoke().getLakarutlatande();
        if (utlatande != null) {
            return utlatande.getPatient();
        }
        return null;
    }

    private String getPatientId(PatientType patient) {
        if (patient != null && patient.getPersonId() != null) {
            return patient.getPersonId().getExtension();
        }
        return null;
    }

    private String getPersonnummerHash(Optional<Personnummer> personnummer, RevokeMedicalCertificateRequestType request) {
        if (personnummer.isPresent()) {
            return personnummer.get().getPersonnummerHash();
        }

        // Return the personalId in the request, it's invalid personalId so it's okey to return it
        return getPatientId(getPatient(request));
    }

}
