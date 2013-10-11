package se.inera.certificate.migration.model;

import java.io.UnsupportedEncodingException;

public class Certificate {

    private String certificateId;
    
    private String certificateJson;
    
    public Certificate() {
    }
    
    public Certificate(String certificateId) {
        this.certificateId = certificateId;
    }

    public byte[] getCertificateJsonAsBytes() {

        if (this.certificateJson == null) {
            return new byte[0];
        }

        try {
            return this.certificateJson.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to convert certificateJson String to bytes!", e);
        }

    }
    
    public int getCertificateJsonSize() {
        return (this.certificateJson != null) ? this.certificateJson.length() : 0;
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
