package se.inera.certificate.model.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.model.dao.Consent;
import se.inera.certificate.model.dao.ConsentDao;

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
    public void setConsent(String civicRegistrationNumber) {
        if (!hasConsent(civicRegistrationNumber)) {
            entityManager.persist(new Consent(civicRegistrationNumber));
        }
    }

    @Override
    public void revokeConsent(String civicRegistrationNumber) {
        Consent consent = findConsent(civicRegistrationNumber);
        if (consent != null) {
            entityManager.remove(consent);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean hasConsent(String civicRegistrationNumber) {
        return findConsent(civicRegistrationNumber) != null;
    }

    private Consent findConsent(String civicRegistrationNumber) {
        return entityManager.find(Consent.class, civicRegistrationNumber);
    }
}
