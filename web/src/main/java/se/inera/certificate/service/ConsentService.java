package se.inera.certificate.service;

import se.inera.certificate.modules.support.api.dto.Personnummer;

public interface ConsentService {

    /**
     * Consent status for given civicRegistrationNumber.
     *
     * @param civicRegistrationNumber
     * @return true if consent is registered for or civicRegistrationNumer is null
     */
    boolean isConsent(Personnummer civicRegistrationNumber);

    /**
     * Set consent status for given cuvucRegistrationNumber. True means consent is given and false means no consent is given.
     *
     * @param civicRegistrationNumber
     * @param consentGiven
     */
    void setConsent(Personnummer civicRegistrationNumber, boolean consentGiven);
}
