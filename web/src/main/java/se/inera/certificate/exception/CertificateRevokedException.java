package se.inera.certificate.exception;

/**
 * Exception thrown when performing an operation on a revoked certificate.
 *
 * @author andreaskaltenbach
 */
public class CertificateRevokedException extends ClientException {

    private static final long serialVersionUID = 6346409606829031979L;

    public CertificateRevokedException(String certificateId) {
        super(String.format("Certificate '%s' has been revoked.", certificateId));
    }
}
