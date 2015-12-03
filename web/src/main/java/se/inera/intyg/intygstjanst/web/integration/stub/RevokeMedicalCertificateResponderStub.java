package se.inera.intyg.intygstjanst.web.integration.stub;


import javax.xml.ws.WebServiceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.common.support.validate.CertificateValidationException;
import se.inera.intyg.intygstjanst.web.integration.validator.RevokeRequestValidator;
import se.inera.intyg.intygstyper.fk7263.integration.stub.FkMedicalCertificatesStore;


@Transactional
@WebServiceProvider(
        targetNamespace = "urn:riv:insuranceprocess:healthreporting:RevokeMedicalCertificate:1:rivtabp20",
        serviceName = "RevokeMedicalCertificateResponderService",
        wsdlLocation = "classpath:interactions/RevokeMedicalCertificateInteraction/RevokeMedicalCertificateInteraction_1.0_rivtabp20.wsdl"
)
public class RevokeMedicalCertificateResponderStub implements RevokeMedicalCertificateResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevokeMedicalCertificateResponderStub.class);

    @Autowired
    private FkMedicalCertificatesStore fkMedicalCertificatesStore;

    @Override
    public RevokeMedicalCertificateResponseType revokeMedicalCertificate(AttributedURIType logicalAddress,
            RevokeMedicalCertificateRequestType request) {

        RevokeMedicalCertificateResponseType response = new RevokeMedicalCertificateResponseType();

        try {
            new RevokeRequestValidator(request.getRevoke()).validateAndCorrect();
            String id = request.getRevoke().getLakarutlatande().getLakarutlatandeId();
            String meddelande = request.getRevoke().getMeddelande();

            LOGGER.info("STUB Received revocation concerning certificate with id: " + id);
            fkMedicalCertificatesStore.makulera(id, meddelande);

        } catch (CertificateValidationException e) {
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
            return response;
        }

        response.setResult(ResultOfCallUtil.okResult());
        return response;
    }
}
