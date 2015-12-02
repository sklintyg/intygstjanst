package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;

public interface StatisticsService {

    boolean created(Certificate certificate);

    boolean revoked(Certificate certificate);

}
