package se.inera.certificate.integration;

import static se.inera.certificate.integration.util.ResultOfCallUtil.failResult;
import static se.inera.certificate.integration.util.ResultOfCallUtil.okResult;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.exception.InvalidCertificateIdentifierException;
import se.inera.certificate.logging.LogMarkers;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.service.CertificateService;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatus.v1.rivtabp20.SetCertificateStatusResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.setcertificatestatusresponder.v1.SetCertificateStatusResponseType;

/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class SetCertificateStatusResponderImpl implements SetCertificateStatusResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMedicalCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    public SetCertificateStatusResponseType setCertificateStatus(AttributedURIType logicalAddress, SetCertificateStatusRequestType request) {

        SetCertificateStatusResponseType response = new SetCertificateStatusResponseType();

        try {
            certificateService.setCertificateState(request.getNationalIdentityNumber(), request.getCertificateId(), request.getTarget(), CertificateState.valueOf(request.getStatus().name()), request.getTimestamp());
            response.setResult(okResult());
            LOGGER.info(LogMarkers.MONITORING, request.getCertificateId() + " set to status " + request.getStatus().name());
        } catch (InvalidCertificateIdentifierException e) {
            response.setResult(failResult(e.getMessage()));
        }

        return response;
    }
}
