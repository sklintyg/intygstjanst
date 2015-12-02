package se.inera.intyg.intygstjanst.persistence.exception;


import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public class PersistenceException extends Exception {
    public PersistenceException(String certificateId, Personnummer civicRegistrationNumber) {
        super(String.format("Certificate '%s' does not exist for user '%s'.", certificateId, Personnummer.getPnrHashSafe(civicRegistrationNumber)));
    }
}
