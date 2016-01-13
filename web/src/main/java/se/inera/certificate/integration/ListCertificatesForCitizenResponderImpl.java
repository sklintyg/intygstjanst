package se.inera.certificate.integration;

import java.util.List;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.integration.converter.MetaDataResolver;
import se.inera.certificate.integration.module.exception.MissingConsentException;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.dto.Personnummer;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.certificate.service.CertificateService;
import se.inera.certificate.service.MonitoringLogService;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.listcertificatesforcitizen.v1.ListCertificatesForCitizenType;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;


@SchemaValidation
public class ListCertificatesForCitizenResponderImpl implements ListCertificatesForCitizenResponderInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListCertificatesForCitizenResponderImpl.class);

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private MetaDataResolver metaDataResolver;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public ListCertificatesForCitizenResponseType listCertificatesForCitizen(String logicalAddress, ListCertificatesForCitizenType parameters) {
        ListCertificatesForCitizenResponseType response = new ListCertificatesForCitizenResponseType();

        try {
            final Personnummer personnummer = new Personnummer(parameters.getPersonId());
            List<Certificate> certificates = certificateService.listCertificatesForCitizen(
                    personnummer, parameters.getUtlatandeTyp(), parameters.getFranDatum(), parameters.getTillDatum());
            for (Certificate certificate : certificates) {
                // Note that we return certificates that are deleted by the care giver (isDeletedByCareGiver) but not
                // revoked or archived certificates.
                if (parameters.getUtlatandeTyp().isEmpty() || !(certificate.getDeleted() || certificate.isRevoked())) {
                    response.getMeta().add(metaDataResolver.toClinicalProcessCertificateMetaType(certificate));
                }
            }
            response.setResult(ResultTypeUtil.okResult());
            monitoringLogService.logCertificateListedByCitizen(personnummer);
        } catch (ModuleNotFoundException | ModuleException e) {
            response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "Module error when processing certificates"));
            LOGGER.error(e.getMessage());

        } catch (MissingConsentException ex) {
            response.setResult(ResultTypeUtil.infoResult("NOCONSENT"));
        }

        return response;
    }
}
