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
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.validate.CertificateValidationException;
import se.inera.intyg.common.util.logging.LogMarkers;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.SubsystemCallException;
import se.inera.intyg.intygstjanst.web.integration.validator.RevokeRequestValidator;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;


public class RevokeMedicalCertificateResponderImpl implements RevokeMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeMedicalCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private SendMedicalCertificateQuestionResponderInterface sendMedicalCertificateQuestionResponderInterface;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private StatisticsService statisticsService;

    @Override
    @Transactional
    public RevokeMedicalCertificateResponseType revokeMedicalCertificate(AttributedURIType logicalAddress, RevokeMedicalCertificateRequestType request) {

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();

        final Personnummer personnummer = safeGetCivicRegistrationNumber(request);
        try {
            new RevokeRequestValidator(request.getRevoke()).validateAndCorrect();

            String certificateId = request.getRevoke().getLakarutlatande().getLakarutlatandeId();
            Certificate certificate = certificateService.revokeCertificate(personnummer, certificateId, request.getRevoke());
            monitoringLogService.logCertificateRevoked(certificate.getId(), certificate.getType(), certificate.getCareUnitId());

            getStatisticsService().revoked(certificate);

        } catch (InvalidCertificateException e) {
            // return with ERROR response if certificate was not found
            LOGGER.info("Tried to revoke certificate '" + safeGetCertificateId(request) + "' for patient '"
                    + personnummer.getPnrHash() + "' but certificate does not exist");
            response.setResult(ResultOfCallUtil.failResult("No certificate '" + safeGetCertificateId(request)
                    + "' found to revoke for patient '" + personnummer.getPnrHash() + "'."));
            return response;

        } catch (CertificateRevokedException e) {
            // return with INFO response if certificate was revoked before
            LOGGER.info("Tried to revoke certificate '" + safeGetCertificateId(request) + "' for patient '"
                    + personnummer.getPnrHash() + "' which already is revoked");
            response.setResult(ResultOfCallUtil.infoResult("Certificate '" + safeGetCertificateId(request) + "' is already revoked."));
            return response;

        } catch (CertificateValidationException e) {
            // return with ERROR response if certificate had validation errors
            LOGGER.info(LogMarkers.VALIDATION, "Validation error found for revoke certificate '" + safeGetCertificateId(request)
                    + "' issued by '" + safeGetIssuedBy(request) + "' for patient '" + personnummer.getPnrHash() + ": " + e.getMessage());
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            return response;

        } catch (SubsystemCallException e) {
            LOGGER.warn("Encountered an exception when sending a revocation to subsystem '" + e.getSubsystemId() + "'");
            response.setResult(ResultOfCallUtil.failResult("Informing subsystem '" + e.getSubsystemId() + "' about revoked certificate resulted in error"));
            return response;
        }

        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }

    protected String safeGetCertificateId(RevokeMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getRevoke() != null && request.getRevoke().getLakarutlatande() != null
                && request.getRevoke().getLakarutlatande().getLakarutlatandeId() != null) {
            return request.getRevoke().getLakarutlatande().getLakarutlatandeId();
        }
        return null;
    }

    protected Personnummer safeGetCivicRegistrationNumber(RevokeMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getRevoke().getLakarutlatande().getPatient() != null
                && request.getRevoke().getLakarutlatande().getPatient().getPersonId() != null) {
            return new Personnummer(request.getRevoke().getLakarutlatande().getPatient().getPersonId().getExtension());
        }
        return Personnummer.empty();
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

    public StatisticsService getStatisticsService() {
        return statisticsService;
    }

    public void setStatisticsService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
}
