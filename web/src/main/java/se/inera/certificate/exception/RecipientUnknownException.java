package se.inera.certificate.exception;

public class RecipientUnknownException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -2850911343263575085L;

    public RecipientUnknownException() {
        super();
    }

    public RecipientUnknownException(String message) {
        super(message);
    }

    public RecipientUnknownException(Throwable cause) {
        super(cause);
    }

    public RecipientUnknownException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecipientUnknownException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
