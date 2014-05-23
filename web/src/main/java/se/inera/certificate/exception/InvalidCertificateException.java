package se.inera.certificate.exception;

/**
 * Exception thrown whenever a certificate with unknown certificate ID is tried to access, or the civic registration
 * number doesn't match the one in the certificate.
 * 
 * @author andreaskaltenbach
 */
public class InvalidCertificateException extends ClientException {

    private static final long serialVersionUID = 9207157337550587128L;

    public InvalidCertificateException(String certificateId, String civicRegistrationNumber) {
        super(String.format("Certificate '%s' does not exist for user + '%s'.", certificateId, civicRegistrationNumber));
    }
}
