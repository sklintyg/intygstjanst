package se.inera.certificate.integration;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

/**
 * @author andreaskaltenbach
 */
public class RegisterMedicalCertificateResponderWiretapImpl extends RegisterMedicalCertificateResponderImpl implements RegisterMedicalCertificateResponderInterface {

    @Autowired
    private CertificateService certificateService;

    @Override
    public RegisterMedicalCertificateResponseType registerMedicalCertificate(AttributedURIType logicalAddress, RegisterMedicalCertificateType request) {
        RegisterMedicalCertificateResponseType response =  super.registerMedicalCertificate(logicalAddress, request);

        String personnummer = request.getLakarutlatande().getPatient().getPersonId().getExtension();
        String certificateId = request.getLakarutlatande().getLakarutlatandeId();

        certificateService.setCertificateState(personnummer, certificateId, "FK", CertificateState.SENT, new LocalDateTime());

        return response;
    }
}
