package se.inera.certificate.migration.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.migration.model.OriginalCertificate;
import se.inera.certificate.migration.rest.FK7263ConverterClient;

public class ConvertCertificateXMLToJSONProcessor implements
		ItemProcessor<OriginalCertificate, Certificate> {
	
	private static Logger LOG = LoggerFactory.getLogger(ConvertCertificateXMLToJSONProcessor.class);
	
	@Autowired
	private FK7263ConverterClient converter;

	public Certificate process(OriginalCertificate orgCert) throws Exception {
		LOG.debug("Starting FK7263ConverterClient");
		
		Certificate cert = new Certificate(orgCert.getCertificateId());
		String convertedJson = converter.convertOriginalCertificate(orgCert);
		
		cert.setCertificateJson(convertedJson);
		return cert;
	}

}
