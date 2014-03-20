package se.inera.certificate.service.util;

import com.fasterxml.jackson.annotation.JsonRawValue;

import se.inera.certificate.model.dao.Certificate;

public class BootstrapCertificate extends Certificate {

    public BootstrapCertificate() {
        super();
        // TODO Auto-generated constructor stub
    }

    @JsonRawValue
    public void setDocumentJson(String s) {
        setDocument(s);
    }
}
