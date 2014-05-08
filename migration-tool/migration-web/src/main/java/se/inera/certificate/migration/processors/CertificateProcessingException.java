package se.inera.certificate.migration.processors;

/**
 * A non-fatal certifcate processing exception that does not fail the whole batch when thrown.
 * 
 * @author nikpet
 *
 */
public class CertificateProcessingException extends AbstractCertificateProcessingException {

    private static final long serialVersionUID = -2631845978168019323L;

    public CertificateProcessingException() {
        super();
    }

    public CertificateProcessingException(String message) {
        super(message);
    }

    public CertificateProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
