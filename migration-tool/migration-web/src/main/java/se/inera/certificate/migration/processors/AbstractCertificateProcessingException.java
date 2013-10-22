package se.inera.certificate.migration.processors;

/**
 * Base class for certificate processing exceptions.
 * 
 * @author nikpet
 *
 */
public abstract class AbstractCertificateProcessingException extends Exception {

    private static final long serialVersionUID = 6559161321820657170L;

    public AbstractCertificateProcessingException() {
        super();
    }

    public AbstractCertificateProcessingException(String message) {
        super(message);
    }

    public AbstractCertificateProcessingException(Throwable cause) {
        super(cause);
    }

    public AbstractCertificateProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
