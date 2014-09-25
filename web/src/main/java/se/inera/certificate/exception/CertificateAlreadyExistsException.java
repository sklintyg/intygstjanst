package se.inera.certificate.exception;

import javax.ws.rs.client.ClientException;

/**
 * @author andreaskaltenbach
 */
public class CertificateAlreadyExistsException extends ClientException {

    private static final long serialVersionUID = 6746299605626528366L;

    public CertificateAlreadyExistsException(String certificateId) {
        super(String.format("Certificate '%s' already exists", certificateId));
    }
}
