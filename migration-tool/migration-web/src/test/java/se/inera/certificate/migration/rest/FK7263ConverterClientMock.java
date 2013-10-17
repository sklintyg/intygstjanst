package se.inera.certificate.migration.rest;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import se.inera.certificate.migration.model.OriginalCertificate;

public class FK7263ConverterClientMock implements FK7263ConverterClient {
	private static Logger LOG = LoggerFactory.getLogger(FK7263ConverterClientMock.class);
	
	@Override
	public String convertOriginalCertificate(OriginalCertificate orgCert) throws IOException {
		LOG.debug("Running convertOriginalCertificate in FK263ConverterClientMock");
		String json = null;
		Resource fileRes = new ClassPathResource("data/legacy-maximalt-fk7263-external.json");
		json = FileUtils.readFileToString(fileRes.getFile());
		return json;
	}

}
