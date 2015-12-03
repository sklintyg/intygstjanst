package se.inera.intyg.intygstjanst.web.integration;

import java.util.List;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.integration.converter.MetaDataResolver;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.listcertificatesforcare.v1.ListCertificatesForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;


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
            final Personnummer personnummer = new Personnummer(parameters.getPersonId());
            List<Certificate> certificates = certificateService.listCertificatesForCare(
                    personnummer, parameters.getEnhet());
            for (Certificate certificate : certificates) {
                // If the certificate is deleted by the care giver it is not returned. Note that both revoked and
                // archived certificates are returned
                if (!certificate.isDeletedByCareGiver()) {
                    response.getMeta().add(metaDataResolver.toClinicalProcessCertificateMetaType(certificate));
                }
            }
            response.setResult(ResultTypeUtil.okResult());
            monitoringLogService.logCertificateListedByCare(personnummer);
        } catch (ModuleNotFoundException | ModuleException e) {
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "Module error when processing certificates"));
            LOGGER.error(e.getMessage());
        }

        return response;
    }
}
