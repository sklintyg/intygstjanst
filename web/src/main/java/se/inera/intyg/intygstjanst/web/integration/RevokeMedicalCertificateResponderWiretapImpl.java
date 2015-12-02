package se.inera.certificate.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.certificate.integration.validator.RevokeRequestValidator;
import se.inera.intyg.common.util.logging.LogMarkers;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.certificate.service.CertificateService;
import se.inera.intyg.common.support.validate.CertificateValidationException;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;


@Transactional
public class RevokeMedicalCertificateResponderWiretapImpl extends RevokeMedicalCertificateResponderImpl implements
        RevokeMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeMedicalCertificateResponderWiretapImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    public RevokeMedicalCertificateResponseType revokeMedicalCertificate(AttributedURIType logicalAddress,
            RevokeMedicalCertificateRequestType request) {

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();

        final Personnummer personnummer = safeGetCivicRegistrationNumber(request);
        try {
            new RevokeRequestValidator(request.getRevoke()).validateAndCorrect();

            String certificateId = request.getRevoke().getLakarutlatande().getLakarutlatandeId();

            Certificate certificate = certificateService.revokeCertificate(personnummer, certificateId, null);
            LOGGER.info(certificateId + " revoked");
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
        }

        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }

}
