package se.inera.certificate.model.dao;

import se.inera.certificate.modules.support.api.dto.Personnummer;

/**
 * @author andreaskaltenbach
 */
public interface ConsentDao {

    void setConsent(Personnummer civicRegistrationNumber);

    void revokeConsent(Personnummer civicRegistrationNumber);

    boolean hasConsent(Personnummer civicRegistrationNumber);
}
