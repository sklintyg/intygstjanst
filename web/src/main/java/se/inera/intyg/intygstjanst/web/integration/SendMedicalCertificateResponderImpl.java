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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.rivtabp20.v1.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.exception.ExternalServiceCallException;
import se.inera.intyg.common.support.validate.CertificateValidationException;
import se.inera.intyg.common.util.logging.LogMarkers;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.integration.validator.SendCertificateRequestValidator;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.schemas.contract.Personnummer;

public class SendMedicalCertificateResponderImpl implements SendMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMedicalCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private RecipientService recipientService;

    @Override
    public SendMedicalCertificateResponseType sendMedicalCertificate(AttributedURIType logicalAddress,
            SendMedicalCertificateRequestType request) {

        SendMedicalCertificateResponseType response = new SendMedicalCertificateResponseType();

        Personnummer personnummer = safeGetCivicRegistrationNumber(request);
        try {
            new SendCertificateRequestValidator(request.getSend()).validateAndCorrect();

            String certificateId = request.getSend().getLakarutlatande().getLakarutlatandeId();

            // Comment 2015-02-13 by Magnus Ekstrand:
            // Lookup recipient based on the certificate type. This works if and only if a certificate
            // type only have one recipient. If in a future a certificate type can be sent to multiple
            // recipients, other mechanisms must be implemented to determine correct recipient.
            Certificate certificate = certificateService.getCertificateForCare(certificateId);
            Recipient recipient = lookupRecipient(certificate);

            // Send certificate to recipient
            SendStatus status = certificateService.sendCertificate(personnummer, certificateId, recipient.getId());

            if (status == SendStatus.ALREADY_SENT) {
                response.setResult(ResultOfCallUtil.infoResult("Certificate '" + certificateId + "' is already sent."));
                LOGGER.info(certificateId + " already sent to" + recipient.getId());
            } else {
                response.setResult(ResultOfCallUtil.okResult());
                LOGGER.info(certificateId + " sent to " + recipient.getId());
            }

            return response;

        } catch (InvalidCertificateException e) {
            // return with ERROR response if certificate was not found
            LOGGER.info("Tried to send certificate '" + safeGetCertificateId(request) + "' for patient '"
                    + personnummer.getPnrHash() + "' but certificate does not exist");
            response.setResult(ResultOfCallUtil.failResult("No certificate '" + safeGetCertificateId(request)
                    + "' found to send for patient '" + personnummer.getPnrHash() + "'."));
            return response;

        } catch (CertificateRevokedException e) {
            // return with INFO response if certificate was revoked before
            LOGGER.info("Tried to send certificate '" + safeGetCertificateId(request) + "' for patient '"
                    + personnummer.getPnrHash() + "' which is revoked");
            response.setResult(ResultOfCallUtil.infoResult("Certificate '" + safeGetCertificateId(request) + "' has been revoked."));
            return response;

        } catch (CertificateValidationException e) {
            LOGGER.error(LogMarkers.VALIDATION, "Validation error found for send certificate '" + safeGetCertificateId(request)
                    + "' issued by '" + safeGetIssuedBy(request) + "' for patient '" + personnummer.getPnrHash()
                    + ": " + e.getMessage());
            // return with ERROR response if certificate had validation errors
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
        }

    }

    private Recipient lookupRecipient(Certificate certificate) throws RecipientUnknownException {
        return lookupRecipient(new CertificateType(certificate.getType()));
    }

    private Recipient lookupRecipient(CertificateType certificateType) throws RecipientUnknownException {

        List<Recipient> recipients = recipientService.listRecipients(certificateType);

        if (recipients.size() == 1) {
            return recipients.get(0);
        }

        String errorMsg = "";

        if (recipients.isEmpty()) {
            errorMsg = String.format("No recipient was found for certificate of type %s. Maybe this is a missed configuration.",
                    certificateType.getCertificateTypeId());
        }

        if (recipients.size() > 1) {
            errorMsg = String.format(
                    "Multiple recipients were found for certificate of type %s. Unable to decide recipient. "
                            + "Maybe this is a missed configuration.",
                    certificateType.getCertificateTypeId());
        }

        LOGGER.error(LogMarkers.VALIDATION, errorMsg);
        throw new RecipientUnknownException(errorMsg);
    }

    private String safeGetCertificateId(SendMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getSend() != null && request.getSend().getLakarutlatande() != null
                && request.getSend().getLakarutlatande().getLakarutlatandeId() != null) {
            return request.getSend().getLakarutlatande().getLakarutlatandeId();
        }
        return null;
    }

    private Personnummer safeGetCivicRegistrationNumber(SendMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getSend().getLakarutlatande().getPatient() != null
                && request.getSend().getLakarutlatande().getPatient().getPersonId() != null) {
            return new Personnummer(request.getSend().getLakarutlatande().getPatient().getPersonId().getExtension());
        }
        return Personnummer.empty();
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
}
