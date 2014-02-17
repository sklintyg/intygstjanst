package se.inera.certificate.integration;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.model.CertificateState;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum;

/**
 * @author andreaskaltenbach
 */
public class RegisterMedicalCertificateResponderWiretapImpl extends RegisterMedicalCertificateLegacyResponderProvider implements RegisterMedicalCertificateResponderInterface {

    @Autowired
    private CertificateService certificateService;

    @Override
    public RegisterMedicalCertificateResponseType registerMedicalCertificate(AttributedURIType logicalAddress,
            RegisterMedicalCertificateType registerMedicalCertificate) {
        RegisterMedicalCertificateResponseType response = super.registerMedicalCertificate(logicalAddress,
                registerMedicalCertificate);

        if (response.getResult().getResultCode() != ResultCodeEnum.ERROR) {
            setSendStatus(registerMedicalCertificate);
        }

        return response;
    }

    private void setSendStatus(RegisterMedicalCertificateType request) {
        // extract personnummer & certificate ID and explicitly set status SENT for Försäkringskassan
        String personnummer = request.getLakarutlatande().getPatient().getPersonId().getExtension();
        String certificateId = request.getLakarutlatande().getLakarutlatandeId();
        certificateService.setCertificateState(personnummer, certificateId, "FK", CertificateState.SENT,
                new LocalDateTime());
    }
}
