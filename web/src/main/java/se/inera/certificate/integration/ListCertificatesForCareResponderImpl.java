package se.inera.certificate.integration;

import java.util.List;

import org.apache.cxf.annotations.SchemaValidation;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.inera.certificate.integration.converter.ModelConverter;
import se.inera.certificate.integration.util.ResultTypeUtil;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.service.CertificateService;

/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class ListCertificatesForCareResponderImpl implements ListCertificatesForCareResponderInterface {

    @Autowired
    private CertificateService certificateService;

    @Override
    public ListCertificatesForCareResponseType listCertificatesForCare(String logicalAddress, ListCertificatesForCareType parameters) {
        ListCertificatesForCareResponseType response = new ListCertificatesForCareResponseType();

        List<Certificate> certificates = certificateService.listCertificatesForCare(
                parameters.getNationalIdentityNumber(), parameters.getCareUnit());
        for (Certificate certificate : certificates) {
            response.getMeta().add(ModelConverter.toCertificateMetaType(certificate));
        }
        response.setResult(ResultTypeUtil.okResult());

        return response;
    }
}
