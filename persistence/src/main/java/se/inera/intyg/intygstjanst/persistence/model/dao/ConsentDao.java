package se.inera.intyg.intygstjanst.persistence.model.dao;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

/**
 * @author andreaskaltenbach
 */
public interface ConsentDao {

    void setConsent(Personnummer civicRegistrationNumber);

    void revokeConsent(Personnummer civicRegistrationNumber);

    boolean hasConsent(Personnummer civicRegistrationNumber);
}
