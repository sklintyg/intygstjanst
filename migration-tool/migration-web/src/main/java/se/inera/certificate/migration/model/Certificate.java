package se.inera.certificate.migration.model;

public class Certificate {
    
    private String certificateId;
    
    private String certificateJson;
    
    public Certificate(String certificateId) {
        this.certificateId = certificateId;
    }
    
    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getCertificateJson() {
        return certificateJson;
    }

    public void setCertificateJson(String certificateJson) {
        this.certificateJson = certificateJson;
    }
    
}
