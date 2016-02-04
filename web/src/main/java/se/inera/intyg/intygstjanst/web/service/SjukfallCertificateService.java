package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;

/**
 * Created by eriklupander on 2016-02-03.
 */
public interface SjukfallCertificateService {

    boolean created(Certificate certificate);

    boolean revoked(Certificate certificate);

}
