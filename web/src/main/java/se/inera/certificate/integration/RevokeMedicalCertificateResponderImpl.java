package se.inera.certificate.integration;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.exception.SubsystemCallException;
import se.inera.certificate.integration.module.exception.CertificateRevokedException;
import se.inera.certificate.integration.module.exception.InvalidCertificateException;
import se.inera.certificate.integration.validator.RevokeRequestValidator;
import se.inera.certificate.logging.HashUtility;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.MonitoringLogService;
import se.inera.certificate.service.StatisticsService;
import se.inera.certificate.validate.CertificateValidationException;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;


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

        try {
            new RevokeRequestValidator(request.getRevoke()).validateAndCorrect();

            String certificateId = request.getRevoke().getLakarutlatande().getLakarutlatandeId();
            Personnummer civicRegistrationNumber = new Personnummer(request.getRevoke().getLakarutlatande().getPatient().getPersonId()
                    .getExtension());

            Certificate certificate = certificateService.revokeCertificate(civicRegistrationNumber, certificateId, request.getRevoke());
            monitoringLogService.logCertificateRevoked(certificate.getId(), certificate.getType(), certificate.getCareUnitId());

            getStatisticsService().revoked(certificate);

        } catch (InvalidCertificateException e) {
            // return with ERROR response if certificate was not found
            LOGGER.info("Tried to revoke certificate '" + safeGetCertificateId(request) + "' for patient '"
                    + HashUtility.hash(safeGetCivicRegistrationNumber(request)) + "' but certificate does not exist");
            response.setResult(ResultOfCallUtil.failResult("No certificate '" + safeGetCertificateId(request)
                    + "' found to revoke for patient '" + safeGetCivicRegistrationNumber(request) + "'."));
            return response;

        } catch (CertificateRevokedException e) {
            // return with INFO response if certificate was revoked before
            LOGGER.info("Tried to revoke certificate '" + safeGetCertificateId(request) + "' for patient '"
                    + HashUtility.hash(safeGetCivicRegistrationNumber(request)) + "' which already is revoked");
            response.setResult(ResultOfCallUtil.infoResult("Certificate '" + safeGetCertificateId(request) + "' is already revoked."));
            return response;

        } catch (CertificateValidationException e) {
            // return with ERROR response if certificate had validation errors
            LOGGER.info(LogMarkers.VALIDATION, "Validation error found for revoke certificate '" + safeGetCertificateId(request)
                    + "' issued by '" + safeGetIssuedBy(request) + "' for patient '" + HashUtility.hash(safeGetCivicRegistrationNumber(request)) + ": " + e.getMessage());
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

    protected String safeGetCivicRegistrationNumber(RevokeMedicalCertificateRequestType request) {
        // Initialize log context info if available
        if (request.getRevoke().getLakarutlatande().getPatient() != null
                && request.getRevoke().getLakarutlatande().getPatient().getPersonId() != null) {
            return request.getRevoke().getLakarutlatande().getPatient().getPersonId().getExtension();
        }
        return null;
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
