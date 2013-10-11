package se.inera.certificate.migration.rest;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.certificate.migration.model.OriginalCertificate;

public class FK7263ConverterClientImpl implements FK7263ConverterClient {

	private static Logger LOG = LoggerFactory
			.getLogger(FK7263ConverterClientImpl.class);

	private String restUrl;

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.inera.certificate.migration.rest.FK7263ConverterClient#
	 * convertOriginalCertificate
	 * (se.inera.certificate.migration.model.OriginalCertificate)
	 */
	@Override
	public String convertOriginalCertificate(OriginalCertificate orgCert)
			throws IOException {
		LOG.debug("starting conversion");

		CloseableHttpClient client = HttpClients.createDefault();

		HttpPost post = new HttpPost(restUrl);
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-Type", "application/xml");

		StringEntity xmlPayload = new StringEntity(
				orgCert.getOrignalCertificateAsString());
		xmlPayload.setContentType("application/xml");

		post.setEntity(xmlPayload);
		
		String content = null;
		
		try {
			HttpResponse response = client.execute(post);
			
			if (response.getStatusLine().getStatusCode() == 200) {
				
				content = EntityUtils.toString(response.getEntity(), "UTF-8");
			} else {
				LOG.error("Got http error code: " + response.getStatusLine().getStatusCode());
			}
				
		} finally {
			client.close();
		}
		
		return content;
		
	}

	public String getRestUrl() {
		return restUrl;
	}

	public void setRestUrl(String restUrl) {
		this.restUrl = restUrl;
	}

}
