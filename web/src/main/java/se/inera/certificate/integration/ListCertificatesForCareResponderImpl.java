package se.inera.certificate.integration;

import static se.inera.certificate.integration.util.ResultTypeUtil.errorResult;

import java.util.List;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.integration.converter.MetaDataResolver;
import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.integration.util.ResultTypeUtil;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.service.CertificateService;

/**
 * @author andreaskaltenbach
 */
@SchemaValidation
public class ListCertificatesForCareResponderImpl implements ListCertificatesForCareResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCertificatesForCareResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MetaDataResolver metaDataResolver;

    @Override
    public ListCertificatesForCareResponseType listCertificatesForCare(String logicalAddress, ListCertificatesForCareType parameters) {
        ListCertificatesForCareResponseType response = new ListCertificatesForCareResponseType();

        try {
            List<Certificate> certificates = certificateService.listCertificatesForCare(
                    parameters.getNationalIdentityNumber(), parameters.getCareUnit());
            for (Certificate certificate : certificates) {
                response.getMeta().add(metaDataResolver.toClinicalProcessCertificateMetaType(certificate));
            }
            response.setResult(ResultTypeUtil.okResult());

        } catch (ModuleNotFoundException | ModuleException e) {
            response.setResult(errorResult(ErrorIdType.APPLICATION_ERROR, "Module error when processing certificates"));
            LOGGER.error(e.getMessage());
        }

        return response;
    }
}
