package se.inera.certificate.exception;

public class MissingModuleException extends ServerException {

    private static final long serialVersionUID = 2123077268951651241L;

    public MissingModuleException() {
        super();
    }

    public MissingModuleException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingModuleException(String message) {
        super(message);
    }

    public MissingModuleException(Throwable cause) {
        super(cause);
    }
}
