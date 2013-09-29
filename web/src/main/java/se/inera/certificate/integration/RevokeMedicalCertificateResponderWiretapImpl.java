package se.inera.certificate.integration;

import static se.inera.certificate.integration.util.ResultOfCallUtil.failResult;
import static se.inera.certificate.integration.util.ResultOfCallUtil.infoResult;
import static se.inera.certificate.integration.util.ResultOfCallUtil.okResult;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.v1.rivtabp20.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;

@Transactional
@SchemaValidation
public class RevokeMedicalCertificateResponderWiretapImpl extends RevokeMedicalCertificateResponderImpl implements RevokeMedicalCertificateResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(RevokeMedicalCertificateResponderWiretapImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    public RevokeMedicalCertificateResponseType revokeMedicalCertificate(AttributedURIType logicalAddress, RevokeMedicalCertificateRequestType request) {

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();

        String certificateId = request.getRevoke().getLakarutlatande().getLakarutlatandeId();
        String civicRegistrationNumber = request.getRevoke().getLakarutlatande().getPatient().getPersonId().getExtension();

        try {
            certificateService.revokeCertificate(civicRegistrationNumber, certificateId);
        } catch (InvalidCertificateException e) {
            // return with ERROR response if certificate was not found
            LOG.info("Tried to revoke certificate '" + certificateId + "' for patient '" + civicRegistrationNumber + "' but certificate does not exist");
            response.setResult(failResult("No certificate '" + certificateId + "' found to revoke for patient '" + civicRegistrationNumber + "'."));
            return response;
        } catch (CertificateRevokedException e) {
            // return with INFO response if certificate was revoked before
            LOG.info("Tried to revoke certificate '" + certificateId + "' for patient '" + civicRegistrationNumber + "' which already is revoked");
            response.setResult(infoResult("Certificate '" + certificateId + "' is already revoked."));
            return response;
        }

        response.setResult(okResult());
        return response;
    }

}
