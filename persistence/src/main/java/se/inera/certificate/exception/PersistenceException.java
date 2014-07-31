package se.inera.certificate.exception;

public class PersistenceException extends Exception {
    public PersistenceException(String certificateId, String civicRegistrationNumber) {
        super(String.format("Certificate '%s' does not exist for user '%s'.", certificateId, civicRegistrationNumber));
    }
}
