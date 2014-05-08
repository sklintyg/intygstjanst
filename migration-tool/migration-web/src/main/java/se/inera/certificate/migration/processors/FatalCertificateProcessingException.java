package se.inera.certificate.migration.processors;

/**
 * A processing exception that causes the whole batch job step to be terminated.
 * 
 * @author nikpet
 *
 */
public class FatalCertificateProcessingException extends AbstractCertificateProcessingException {

    private static final long serialVersionUID = -2631845978168019323L;

    public FatalCertificateProcessingException() {
        super();
    }

    public FatalCertificateProcessingException(String message) {
        super(message);
    }

    public FatalCertificateProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
