package se.inera.certificate.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.util.ResultOfCallUtil;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.CertificateService.SendStatus;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;

public class SendMedicalCertificateResponderImpl implements SendMedicalCertificateResponderInterface {

    @Autowired
    private CertificateService certificateService;

    @Override
    public SendMedicalCertificateResponseType sendMedicalCertificate(AttributedURIType logicalAddress, SendMedicalCertificateRequestType request) {
        SendMedicalCertificateResponseType response = new SendMedicalCertificateResponseType();

        String certificateId = request.getSend().getLakarutlatande().getLakarutlatandeId();
        String civicRegistrationNumber = request.getSend().getLakarutlatande().getPatient().getPersonId().getExtension();

        SendStatus status = certificateService.sendCertificate(civicRegistrationNumber, certificateId, "FK");
        if (status == SendStatus.ALREADY_SENT) {
            response.setResult(ResultOfCallUtil.infoResult("Certificate '"+ certificateId + "' is already sent."));
        } else {
            response.setResult(ResultOfCallUtil.okResult());
        }

        return response;
    }
}
