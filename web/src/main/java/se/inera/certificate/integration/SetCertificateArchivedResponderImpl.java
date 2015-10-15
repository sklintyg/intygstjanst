package se.inera.certificate.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.certificate.integration.module.exception.InvalidCertificateException;
import se.inera.certificate.service.CertificateService;
import se.inera.intyg.common.schemas.insuranceprocess.healthreporting.utils.ResultOfCallUtil;
import se.inera.intyg.insuranceprocess.healthreporting.setcertificatearchived.rivtabp20.v1.SetCertificateArchivedResponderInterface;
import se.inera.intyg.insuranceprocess.healthreporting.setcertificatearchivedresponder.v1.SetCertificateArchivedRequestType;
import se.inera.intyg.insuranceprocess.healthreporting.setcertificatearchivedresponder.v1.SetCertificateArchivedResponseType;


/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class SetCertificateArchivedResponderImpl implements SetCertificateArchivedResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMedicalCertificateResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Override
    public SetCertificateArchivedResponseType setCertificateArchived(AttributedURIType logicalAddress, SetCertificateArchivedRequestType request) {
        LOGGER.debug("Attempting to set 'Deleted' to {} for intyg {}", request.getArchivedState(), request.getCertificateId());
        SetCertificateArchivedResponseType response = new SetCertificateArchivedResponseType();
        try {
            certificateService.setArchived(request.getCertificateId(), request.getNationalIdentityNumber(), request.getArchivedState());
            response.setResult(ResultOfCallUtil.okResult());
        } catch (InvalidCertificateException e) {
            response.setResult(ResultOfCallUtil.failResult(e.getMessage()));
        }
        return response;
    }
}
