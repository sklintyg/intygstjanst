package se.inera.certificate.integration.rest.exception;

import javax.ws.rs.core.Response;

/**
 * @author andreaskaltenbach
 */
public class ModuleCallFailedException extends RuntimeException {

    private String message;
    private int statusCode;
    private String reasonPhrase;

    public ModuleCallFailedException(String message, Response response) {
        this.message = message;

        if (response.getStatusInfo() != null) {
            statusCode = response.getStatusInfo().getStatusCode();
            reasonPhrase = response.getStatusInfo().getReasonPhrase();
        }
    }

    @Override
    public String getMessage() {
        return message + " (module returned status code " + statusCode + " and reason " + reasonPhrase;
    }
}
