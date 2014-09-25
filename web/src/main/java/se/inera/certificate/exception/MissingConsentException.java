package se.inera.certificate.exception;

import javax.ws.rs.client.ClientException;

/**
 * @author andreaskaltenbach
 */
public class MissingConsentException extends ClientException {

    private static final long serialVersionUID = -2935854410295967047L;

    public MissingConsentException(String civicRegistrationNumber) {
        super(String.format("Consent required from user %s.", civicRegistrationNumber));
    }
}
