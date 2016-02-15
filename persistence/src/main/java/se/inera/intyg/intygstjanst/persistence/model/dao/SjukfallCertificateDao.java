package se.inera.intyg.intygstjanst.persistence.model.dao;

import java.util.List;

/**
 * Created by eriklupander on 2016-02-02.
 */
public interface SjukfallCertificateDao {
    List<SjukfallCertificate> findActiveSjukfallCertificateForCareUnits(List<String> careUnitHsaIds);

    void store(SjukfallCertificate sjukfallCert);

    void revoke(String id);
}
