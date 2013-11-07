package se.inera.certificate.exception;

/**
 * @author andreaskaltenbach
 */
public class CertificateAlreadyExistsException extends RuntimeException {

    public CertificateAlreadyExistsException() {
    }

    @Override
    public String getMessage() {
        return "Certificate already exists";
    }
}
