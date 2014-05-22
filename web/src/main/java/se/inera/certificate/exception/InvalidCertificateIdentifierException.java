package se.inera.certificate.exception;

/**
 *
 * Exception which is thrown whenever there is a mismatch in a pair of civic registration number and certificate ID.
 *
 * @author andreaskaltenbach
 */
public class InvalidCertificateIdentifierException extends ClientException {

    private static final long serialVersionUID = -6404197601599517806L;

    public InvalidCertificateIdentifierException(String certificateId, String civicRegistrationNumber) {
        super(String.format("There is no certificate '%s' for user '%s'.", certificateId, civicRegistrationNumber));
    }
}
