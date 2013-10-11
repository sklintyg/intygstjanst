package se.inera.certificate.migration.rest;

import java.io.IOException;

import se.inera.certificate.migration.model.OriginalCertificate;

public interface FK7263ConverterClient {

	public abstract String convertOriginalCertificate(
			OriginalCertificate orgCert) throws IOException;

}