package se.inera.certificate.migration.model;

import java.io.UnsupportedEncodingException;

import org.joda.time.LocalDateTime;

/**
 * Representation of a medical certificate.
 * 
 * @author nikpet
 * 
 */
public class Certificate {

	private static final String UTF_8 = "UTF-8";

	/**
	 * Id of the certificate.
	 */
	private String certificateId;
	
	private Integer originalCertificateId;

	private LocalDateTime signedDate;

	/**
	 * The certificate as JSON.
	 */
	private byte[] certificateJson;

	/**
	 * The certificate as XML.
	 */
	private byte[] certificateXml;

	public Certificate() {
	}

	public String getCertificateId() {
		return certificateId;
	}

	public void setCertificateId(String certificateId) {
		this.certificateId = certificateId;
	}

	public Integer getOriginalCertificateId() {
		return originalCertificateId;
	}

	public void setOriginalCertificateId(Integer originalCertificateId) {
		this.originalCertificateId = originalCertificateId;
	}

	public LocalDateTime getSignedDate() {
		return signedDate;
	}

	public void setSignedDate(LocalDateTime signedDate) {
		this.signedDate = signedDate;
	}

	public byte[] getCertificateJson() {
		return certificateJson;
	}

	public void setCertificateJson(byte[] certificateJson) {
		this.certificateJson = certificateJson;
	}
	
	public void setCertificateJson(String certificateJson) {
		this.certificateJson = stringAsBytes(certificateJson);
	}

	public String getCertificateJsonAsString() {
		return bytesAsString(this.certificateJson);
	}
	
	public byte[] getCertificateXml() {
		return certificateXml;
	}

	public void setCertificateXml(byte[] certificateXml) {
		this.certificateXml = certificateXml;
	}

	public String getCertificateXmlAsString() {
		return bytesAsString(this.certificateXml);
	}
	
	private byte[] stringAsBytes(String str) {
				
		if (str == null) {
            return new byte[0];
        }

        try {
            return str.getBytes(UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to convert certificateJson String to bytes!", e);
        }
		
	}
	
	private String bytesAsString(byte[] bytes) {
		try {

			if (bytes == null) {
				return new String(new byte[] {}, UTF_8);
			}

			return new String(bytes, UTF_8);

		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Can not convert from bytes to string!");
		}
	}

}
