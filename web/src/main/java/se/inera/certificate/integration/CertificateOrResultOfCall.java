package se.inera.certificate.integration;

import se.inera.certificate.model.dao.Certificate;
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultOfCall;

/**
 * @author andreaskaltenbach
 */
public class CertificateOrResultOfCall {

    private Certificate certificate;
    private ResultOfCall resultOfCall;

    public CertificateOrResultOfCall(Certificate certificate) {
        this.certificate = certificate;
    }

    public CertificateOrResultOfCall(ResultOfCall resultOfCall) {
        this.resultOfCall = resultOfCall;
    }

    public ResultOfCall getResultOfCall() {
        return resultOfCall;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public boolean hasError() {
        return resultOfCall != null;
    }
}
