package se.inera.certificate.migration.model;

import java.io.UnsupportedEncodingException;

public class OriginalCertificate {
    
    private static final String UTF_8 = "UTF-8";

    private Integer originalCertificateId;
    
    private String certificateId;

    private byte[] originalCertificate;
    
    public OriginalCertificate() {

    }

    public String getOrignalCertificateAsString() {
        try {
            
            if (this.originalCertificate == null) {
                return new String(new byte[]{}, UTF_8);
            }
            
            return new String(this.originalCertificate, UTF_8);
            
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Can not convert original certificate from bytes to string!");
        }
    }

    public Integer getOriginalCertificateId() {
        return originalCertificateId;
    }

    public void setOriginalCertificateId(Integer originalCertificateId) {
        this.originalCertificateId = originalCertificateId;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public byte[] getOriginalCertificate() {
        return originalCertificate;
    }

    public void setOriginalCertificate(byte[] originalCertificate) {
        this.originalCertificate = originalCertificate;
    }
}
