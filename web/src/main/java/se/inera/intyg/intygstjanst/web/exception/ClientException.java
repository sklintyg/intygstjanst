package se.inera.certificate.exception;

public class ClientException extends Exception {

    private static final long serialVersionUID = -239449104469100203L;

    public ClientException() {
        super();
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientException(String message) {
        super(message);
    }

    public ClientException(Throwable cause) {
        super(cause);
    }
}
