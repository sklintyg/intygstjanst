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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.rivtabp20.v1.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.common.support.validate.CertificateValidationException;
import se.inera.intyg.infra.monitoring.logging.LogMarkers;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.integration.validator.SendCertificateRequestValidator;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.schemas.contract.Personnummer;

public class SendMedicalCertificateResponderImpl implements SendMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMedicalCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private RecipientService recipientService;

    @Autowired
    private StatisticsService statisticsService;

    @Override
    @PrometheusTimeMethod
    public SendMedicalCertificateResponseType sendMedicalCertificate(
        final AttributedURIType logicalAddress, final SendMedicalCertificateRequestType request) {

        SendMedicalCertificateResponseType response = new SendMedicalCertificateResponseType();

        final String certId = safeGetCertificateId(request);
        Optional<Personnummer> personnummer = safeGetCivicRegistrationNumber(request);
        String personnummerHash = getPersonnummerHash(personnummer, request);

        try {
            new SendCertificateRequestValidator(request.getSend()).validateAndCorrect();

            // Try fetching the certificate to ensure it exists and is correct.
            final Certificate certificate = certificateService.getCertificateForCare(certId);
            final String certType = certificate.getType();
            final String careUnit = certificate.getCareUnitId();

            if (!personnummer.isPresent()) {
                personnummer = Optional.ofNullable(certificate.getCivicRegistrationNumber());
            }

            // According to the TKB, SendMedicalCertificate is only used to send certificates to Försäkringskassan.
            Recipient recipient = recipientService.getPrimaryRecipientFkassa();

            // Send certificate to recipient
            SendStatus status = certificateService.sendCertificate(personnummer.orElse(null), certId, recipient.getId());

            if (status == SendStatus.ALREADY_SENT) {
                LOGGER.info(certId + " already sent to" + recipient.getId());
                response.setResult(ResultOfCallUtil.infoResult("Certificate '" + certId + "' is already sent."));
            } else {
                LOGGER.info(certId + " sent to " + recipient.getId());
                response.setResult(ResultOfCallUtil.okResult());
                statisticsService.sent(certId, certType, careUnit, recipient.getId());
            }

            return response;

        } catch (InvalidCertificateException e) {
            // return with ERROR response if certificate was not found

            LOGGER.info(String
                .format("Tried to send certificate '%s' for patient '%s' but certificate does not exist", certId, personnummerHash));

            response.setResult(ResultOfCallUtil
                .failResult(String.format("No certificate '%s' found to send for patient '%s'.", certId, personnummerHash)));

            return response;

        } catch (CertificateRevokedException e) {
            // return with INFO response if certificate was revoked before
            LOGGER.info(String
                .format("Tried to send certificate '%s' for patient '%s' which is revoked", certId, personnummerHash));

            response.setResult(ResultOfCallUtil
                .infoResult(String.format("Certificate '%s' has been revoked.", certId)));

            return response;

        } catch (CertificateValidationException e) {
            // return with ERROR response if certificate had validation errors
            final String issuedBy = safeGetIssuedBy(request);

            LOGGER.error(LogMarkers.VALIDATION,
                String.format("Validation error found for send certificate '%s' issued by '%s' for patient '%s': %s",
                    certId, issuedBy, personnummerHash, e.getMessage()));

            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            return response;

        } catch (RecipientUnknownException e) {
            LOGGER.error("Unknown recipient");
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            return response;

        } catch (ServerException ex) {
            Throwable cause = ex.getCause();
            String message = cause instanceof ExternalServiceCallException ? cause.getMessage() : ex.getMessage();
            // return ERROR if certificate couldn't be sent
            LOGGER.error("Certificate '{}' couldn't be sent: {}", safeGetCertificateId(request), message);
            response.setResult(ResultOfCallUtil.applicationErrorResult("Certificate couldn't be sent to recipient"));
            return response;
        } catch (TestCertificateException ex) {
            LOGGER.error("Certificate '{}' couldn't be sent to recipient because it is a test certificate", certId);
            response.setResult(ResultOfCallUtil.failResult(
                String.format("Certificate '%s' couldn't be sent to recipient because it is a test certificate", certId)));
            return response;
        }
    }

    private String safeGetCertificateId(SendMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getSend() != null && request.getSend().getLakarutlatande() != null
            && request.getSend().getLakarutlatande().getLakarutlatandeId() != null) {
            return request.getSend().getLakarutlatande().getLakarutlatandeId();
        }
        return null;
    }

    protected Optional<Personnummer> safeGetCivicRegistrationNumber(SendMedicalCertificateRequestType request) {
        final String patientId = getPatientId(getPatient(request));
        if (StringUtils.isNotEmpty(patientId)) {
            return Personnummer.createPersonnummer(patientId);
        }
        return Optional.empty();
    }

    private String safeGetIssuedBy(SendMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getSend().getAdressVard() != null
            && request.getSend().getAdressVard().getHosPersonal() != null
            && request.getSend().getAdressVard().getHosPersonal().getEnhet() != null
            && request.getSend().getAdressVard().getHosPersonal().getEnhet().getEnhetsId() != null) {
            return request.getSend().getAdressVard().getHosPersonal().getEnhet().getEnhetsId().getExtension();
        }
        return null;
    }

    private PatientType getPatient(SendMedicalCertificateRequestType request) {
        LakarutlatandeEnkelType utlatande = request.getSend().getLakarutlatande();
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

    private String getPersonnummerHash(Optional<Personnummer> personnummer, SendMedicalCertificateRequestType request) {
        return personnummer.map(Personnummer::getPersonnummerHash).orElseGet(() -> getPatientId(getPatient(request)));
    }

}
