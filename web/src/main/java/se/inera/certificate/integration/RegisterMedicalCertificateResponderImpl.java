package se.inera.certificate.integration;

import static se.inera.certificate.integration.util.ResultOfCallUtil.infoResult;
import static se.inera.certificate.integration.util.ResultOfCallUtil.okResult;
import intyg.registreraintyg._1.RegistreraIntygResponderInterface;

import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.common.v1.Utlatande;
import se.inera.certificate.integration.converter.LakarutlatandeTypeToUtlatandeConverter;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.v3.rivtabp20.RegisterMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType;

/**
 * @author andreaskaltenbach
 */
public class RegisterMedicalCertificateResponderImpl implements RegisterMedicalCertificateResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterMedicalCertificateResponderImpl.class);

    @Autowired
    private RegistreraIntygResponderInterface registreraIntygResponder;

    @Autowired
    private CertificateService certificateService;

    @Override
    public RegisterMedicalCertificateResponseType registerMedicalCertificate(AttributedURIType logicalAddress, RegisterMedicalCertificateType request) {

        RegisterMedicalCertificateResponseType response = new RegisterMedicalCertificateResponseType();
        Utlatande utlatande = LakarutlatandeTypeToUtlatandeConverter.convert(request.getLakarutlatande());

        try {
            registreraIntygResponder.registreraIntyg(new Holder<>(utlatande));
            certificateService.storeOriginalCertificate(request);
            response.setResult(okResult());
        } catch (DataIntegrityViolationException e) {
            response.setResult(infoResult("Certificate already exists"));
            String certificateId = request.getLakarutlatande().getLakarutlatandeId();
            String issuedBy =  request.getLakarutlatande().getSkapadAvHosPersonal().getEnhet().getEnhetsId().getExtension();
            LOG.warn(LogMarkers.VALIDATION, "Validation warning for intyg " + certificateId +
                    " issued by " + issuedBy +": Certificate already exists - ignored.");
        }
        return response;
    }
}
