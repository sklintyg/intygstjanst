package se.inera.certificate.integration;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.integration.converter.MetaDataResolver;
import se.inera.certificate.logging.HashUtility;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.MonitoringLogService;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;

import java.util.List;


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

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public ListCertificatesForCareResponseType listCertificatesForCare(String logicalAddress, ListCertificatesForCareType parameters) {
        ListCertificatesForCareResponseType response = new ListCertificatesForCareResponseType();

        try {
            List<Certificate> certificates = certificateService.listCertificatesForCare(
                    parameters.getPersonId(), parameters.getEnhet());
            for (Certificate certificate : certificates) {
                // If the certificate is deleted by the care giver it is not returned. Note that both revoked and
                // archived certificates are returned
                if (!certificate.isDeletedByCareGiver()) {
                    response.getMeta().add(metaDataResolver.toClinicalProcessCertificateMetaType(certificate));
                }
            }
            response.setResult(ResultTypeUtil.okResult());
            monitoringLogService.logCertificateListedByCare(HashUtility.hash(parameters.getPersonId()));
        } catch (ModuleNotFoundException | ModuleException e) {
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "Module error when processing certificates"));
            LOGGER.error(e.getMessage());
        }

        return response;
    }
}
