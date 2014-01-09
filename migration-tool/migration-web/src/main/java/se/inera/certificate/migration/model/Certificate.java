package se.inera.certificate.migration.model;

import java.io.UnsupportedEncodingException;

/**
 * Representation of a medical certificate.
 * 
 * @author nikpet
 *
 */
public class Certificate {

    /**
     * Id of the certificate.
     */
    private String certificateId;
    
    /**
     * The certificate as JSON.
     */
    private String certificateJson;
    
    private Integer originalCertificateId;
    
    private boolean isRevoked = false;
    
    public Certificate() {
    }
    
    public Certificate(OriginalCertificate orgCert, String certificateJson) {
        this.originalCertificateId = orgCert.getOriginalCertificateId();
        this.certificateId = orgCert.getCertificateId();
        this.certificateJson = certificateJson;
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
    
    public boolean isCertificateJsonEmpty() {
        return (getCertificateJsonSize() == 0);
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

    public Integer getOriginalCertificateId() {
        return originalCertificateId;
    }

    public void setOriginalCertificateId(Integer originalCertificateId) {
        this.originalCertificateId = originalCertificateId;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean isRevoked) {
        this.isRevoked = isRevoked;
    }

    @Override
    public String toString() {
        return "Certificate [certificateId=" + certificateId + ", originalCertificateId,"
                + originalCertificateId + " certificateJson=" + getCertificateJsonSize() + " chars]";
    }

}
