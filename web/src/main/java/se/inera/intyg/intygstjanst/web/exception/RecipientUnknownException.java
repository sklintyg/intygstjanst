package se.inera.intyg.intygstjanst.web.exception;

public class RecipientUnknownException extends ClientException {

    private static final long serialVersionUID = -2850911343263575085L;

    public RecipientUnknownException(String message) {
        super(message);
    }
}
