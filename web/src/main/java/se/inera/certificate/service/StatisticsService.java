package se.inera.certificate.service;

import se.inera.certificate.model.dao.Certificate;

public interface StatisticsService {

    boolean created(Certificate certificate);

    boolean revoked(Certificate certificate);

}
