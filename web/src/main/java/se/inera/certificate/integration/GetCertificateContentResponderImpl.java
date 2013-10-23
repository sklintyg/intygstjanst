package se.inera.certificate.integration;

import java.util.List;

import static se.inera.certificate.integration.util.ResultOfCallUtil.failResult;
import static se.inera.certificate.integration.util.ResultOfCallUtil.infoResult;
import static se.inera.certificate.integration.util.ResultOfCallUtil.okResult;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.integration.converter.CertificateStateHistoryEntryConverter;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.certificate.v1.CertificateStatusType;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentResponseType;

/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class GetCertificateContentResponderImpl implements GetCertificateContentResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(GetCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    public GetCertificateContentResponseType getCertificateContent(AttributedURIType logicalAddress, GetCertificateContentRequestType request) {

        GetCertificateContentResponseType response = new GetCertificateContentResponseType();

        Certificate certificate;
        try {
            certificate = certificateService.getCertificate(request.getNationalIdentityNumber(), request.getCertificateId());
        } catch (MissingConsentException ex) {
            // return ERROR if user has not given consent
            LOG.info("Tried to get certificate '" + request.getCertificateId() + "' but user '" + request.getNationalIdentityNumber() + "' has not given consent.");
            response.setResult(failResult(String.format("Missing consent for patient %s", request.getNationalIdentityNumber())));
            return response;
        }

        if (certificate == null) {
            // return ERROR if no such certificate does exist
            LOG.info("Tried to get certificate '" + request.getCertificateId() + "' but no such certificate does exist for user '" + request.getNationalIdentityNumber() + "'.");
            response.setResult(failResult(String.format("Unknown certificate ID: %s", request.getCertificateId())));
            return response;
        }

        if (certificate.isRevoked()) {
            // return INFO if certificate is revoked
            LOG.info("Tried to get certificate '" + request.getCertificateId() + "' but certificate has been revoked'.");
            response.setResult(infoResult("Certificate '" + request.getCertificateId() + "' has been revoked"));
            return response;
        }

        attachCertificateDocument(certificate, response);
        response.setResult(okResult());
        return response;

    }

    private void attachCertificateDocument(Certificate certificate, GetCertificateContentResponseType response) {
        // extract certificate states from certificate meta data
        List<CertificateStatusType> states = CertificateStateHistoryEntryConverter.toCertificateStatusType(certificate.getStates());

        response.setCertificate(certificate.getDocument());
        response.getStatuses().addAll(states);
    }
}
