package se.inera.intyg.intygstjanst.web.service.repo.model;

import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;

import java.util.List;

public interface CitizenCertificateConverter {
    CitizenCertificate get(Certificate certificate, List<Relation> relations);
}
