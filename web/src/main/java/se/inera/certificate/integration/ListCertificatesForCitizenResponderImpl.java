package se.inera.certificate.integration;

import static se.inera.certificate.integration.util.ResultTypeUtil.errorResult;
import static se.inera.certificate.integration.util.ResultTypeUtil.infoResult;
import static se.inera.certificate.integration.util.ResultTypeUtil.okResult;

import java.util.List;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.integration.converter.MetaDataResolver;
import se.inera.certificate.integration.module.exception.ModuleNotFoundException;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.service.CertificateService;

@SchemaValidation
public class ListCertificatesForCitizenResponderImpl implements ListCertificatesForCitizenResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCertificatesForCitizenResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MetaDataResolver metaDataResolver;

    @Override
    public ListCertificatesForCitizenResponseType listCertificatesForCitizen(String logicalAddress, ListCertificatesForCitizenType parameters) {
        ListCertificatesForCitizenResponseType response = new ListCertificatesForCitizenResponseType();

        try {
            List<Certificate> certificates = certificateService.listCertificates(
                    parameters.getNationalIdentityNumber(), parameters.getCertificateType(), parameters.getFromDate(), parameters.getToDate());
            for (Certificate certificate : certificates) {
                if (parameters.getCertificateType().isEmpty() || !(certificate.getDeleted() || certificate.isRevoked())) {
                    response.getMeta().add(metaDataResolver.toClinicalProcessCertificateMetaType(certificate));
                }
            }
            response.setResult(okResult());

        } catch (ModuleNotFoundException | ModuleException e) {
            response.setResult(errorResult(ErrorIdType.APPLICATION_ERROR, "Module error when processing certificates"));
            LOGGER.error(e.getMessage());

        } catch (MissingConsentException ex) {
            response.setResult(infoResult("NOCONSENT"));
        }

        return response;
    }
}
