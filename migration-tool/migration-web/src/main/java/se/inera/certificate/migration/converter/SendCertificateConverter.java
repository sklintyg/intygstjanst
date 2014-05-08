package se.inera.certificate.migration.converter;

import se.inera.certificate.migration.model.Certificate;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;

public interface SendCertificateConverter {

	public abstract SendMedicalCertificateRequestType convertToSendRequest(
			Certificate cert);

}