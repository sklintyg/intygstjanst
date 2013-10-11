package se.inera.certificate.migration.testutils.dao;

import java.io.UnsupportedEncodingException;

public class Cert {
    
    private String certId;
    
    private String certType = "fk7263";
    
    private String civicRegNbr;
    
    private String careUnitName = "Vardenheten";
    
    private String signingDoctorName = "Dr Dengroth";
    
    private String signedDate = "2013-01-01";
    
    private String document = "VGhlIGpzb24gZ29lcyBoZXJlIQ==";
    
    public Cert(String certId, String civicRegNbr) {
        super();
        this.certId = certId;
        this.civicRegNbr = civicRegNbr;
    }

    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
        this.certId = certId;
    }

    public String getCertType() {
        return certType;
    }

    public void setCertType(String certType) {
        this.certType = certType;
    }

    public String getCivicRegNbr() {
        return civicRegNbr;
    }

    public void setCivicRegNbr(String civicRegNbr) {
        this.civicRegNbr = civicRegNbr;
    }

    public String getCareUnitName() {
        return careUnitName;
    }

    public void setCareUnitName(String careUnitName) {
        this.careUnitName = careUnitName;
    }

    public String getSigningDoctorName() {
        return signingDoctorName;
    }

    public void setSigningDoctorName(String signingDoctorName) {
        this.signingDoctorName = signingDoctorName;
    }

    public String getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(String signedDate) {
        this.signedDate = signedDate;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }
   
    public byte[] getDocumentAsBytes() {
        
        if (this.document == null) {
            return new byte[0];
        }

        try {
            return this.document.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to convert document String to bytes!", e);
        }
        
    }
}
