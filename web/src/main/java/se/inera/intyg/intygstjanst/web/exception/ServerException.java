package se.inera.intyg.intygstjanst.web.exception;

public class ServerException extends RuntimeException {

    private static final long serialVersionUID = -582116076029565657L;

    public ServerException() {
        super();
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerException(String message) {
        super(message);
    }

    public ServerException(Throwable cause) {
        super(cause);
    }
}
