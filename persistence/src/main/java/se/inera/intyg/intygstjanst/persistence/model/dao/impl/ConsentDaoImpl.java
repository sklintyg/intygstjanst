package se.inera.certificate.model.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.model.dao.Consent;
import se.inera.certificate.model.dao.ConsentDao;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author andreaskaltenbach
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class ConsentDaoImpl implements ConsentDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void setConsent(Personnummer civicRegistrationNumber) {
        if (!hasConsent(civicRegistrationNumber)) {
            entityManager.persist(new Consent(civicRegistrationNumber));
        }
    }

    @Override
    public void revokeConsent(Personnummer civicRegistrationNumber) {
        Consent consent = findConsent(civicRegistrationNumber);
        if (consent != null) {
            entityManager.remove(consent);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean hasConsent(Personnummer civicRegistrationNumber) {
        return findConsent(civicRegistrationNumber) != null;
    }

    private Consent findConsent(Personnummer civicRegistrationNumber) {
        return entityManager.find(Consent.class, civicRegistrationNumber.getPersonnummer());
    }
}
