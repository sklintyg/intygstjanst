package se.inera.certificate.exception;

import java.util.List;

import com.google.common.base.Joiner;

public class CertificateValidationException extends ClientException {

    private static final long serialVersionUID = -5449346862818721648L;

    private static final String VALIDATION_ERROR_PREFIX = "Validation Error(s) found: ";

    public CertificateValidationException(String message) {
        super(VALIDATION_ERROR_PREFIX + message);
    }

    public CertificateValidationException(List<String> messages) {
        super(VALIDATION_ERROR_PREFIX + Joiner.on("\n").join(messages));
    }
}
