package se.inera.certificate.migration.writers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.migration.converter.SendCertificateConverter;
import se.inera.certificate.migration.model.Certificate;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.v1.rivtabp20.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;

public class SendCertificateWriter implements ItemWriter<Certificate> {

	private static Logger LOG = LoggerFactory
			.getLogger(SendCertificateWriter.class);
	
	@Autowired
	private SendCertificateConverter certificateConverter;
	
	@Autowired
	private SendMedicalCertificateResponderInterface sendClient;

	@Override
	public void write(List<? extends Certificate> certItems) throws Exception {
		
		LOG.debug("Performing write on {} certificates", certItems.size());
		
		for (Certificate cert : certItems) {
			send(cert);
		}

	}

	private void send(Certificate cert) {
		
		LOG.debug("Performing SEND on certificate {}", cert.getCertificateId());
		SendMedicalCertificateRequestType request = certificateConverter.convertToSendRequest(cert);
		SendMedicalCertificateResponseType response = sendClient.sendMedicalCertificate(null, request);
		
		LOG.debug("Response ResultCode on SEND is {}", response.getResult().getResultCode());
	}
}
