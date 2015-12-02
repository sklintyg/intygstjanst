package se.inera.certificate.integration;

import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

/**
 * @author andreaskaltenbach
 */
public class CertificateOrResultType {

    private Certificate certificate;
    private ResultType resultType;

    public CertificateOrResultType(Certificate certificate) {
        this.certificate = certificate;
    }

    public CertificateOrResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public boolean hasError() {
        return resultType != null;
    }
}
