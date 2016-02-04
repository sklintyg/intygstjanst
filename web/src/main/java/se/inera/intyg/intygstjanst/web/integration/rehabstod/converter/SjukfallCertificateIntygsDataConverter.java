package se.inera.intyg.intygstjanst.web.integration.rehabstod.converter;

import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsData;

import java.util.List;

/**
 * Created by eriklupander on 2016-02-04.
 */
public interface SjukfallCertificateIntygsDataConverter {
    List<IntygsData> buildIntygsData(List<SjukfallCertificate> sjukfallCertificates);
}
