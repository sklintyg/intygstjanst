package se.inera.certificate.migration.processors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.migration.model.OriginalCertificate;

/**
 * Processor for sending the certificate XML to a REST web service for
 * conversion into JSON.
 * 
 * @author nikpet
 * 
 */
public class ConvertCertificateXMLToJSONProcessor implements InitializingBean,
		ItemProcessor<Certificate, Certificate> {

	private static final String UTF8 = "UTF-8";

	private static Logger LOG = LoggerFactory
			.getLogger(ConvertCertificateXMLToJSONProcessor.class);

	private static CloseableHttpClient httpClient;

	private String converterRestServiceUrl;

	public Certificate process(Certificate cert) throws Exception {

		if (cert == null) {
			LOG.warn("Supplied OriginalCertificate was null, returning null");
			return null;
		}

		LOG.debug("Converting certificate to JSON: {}", cert);

		try {
			return convertAndUpdateCertificate(cert);
		} catch (IOException e) {
			String errMsg = MessageFormat.format("A fatal error occured when processing OriginalCertificate: {0}",
							cert.getCertificateId());
			LOG.error(errMsg, e);
			throw new FatalCertificateProcessingException(errMsg, e);
		}
	}

	public Certificate convertAndUpdateCertificate(Certificate cert)
			throws IOException, AbstractCertificateProcessingException {

		HttpClient client = getHttpClient();

		HttpPost post = prepareRequest(cert);

		HttpResponse response = client.execute(post);

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode != HttpStatus.SC_OK) {
			String errMsg = MessageFormat
					.format("Got HTTP error code {0} when processing OriginalCertificate: {1}",
							new Object[] { statusCode, cert.getCertificateId() });
			throw new FatalCertificateProcessingException(errMsg);
		}

		String convertedJson = EntityUtils.toString(response.getEntity(), UTF8);
		cert.setCertificateJson(convertedJson);

		return cert;
	}

	private HttpPost prepareRequest(Certificate cert)
			throws UnsupportedEncodingException {
		HttpPost post = new HttpPost(converterRestServiceUrl);
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-Type", "application/xml");

		StringEntity xmlPayload = new StringEntity(
				cert.getCertificateXmlAsString(), UTF8);
		xmlPayload.setContentType("application/xml");
		xmlPayload.setContentEncoding(UTF8);

		post.setEntity(xmlPayload);

		return post;
	}

	public static CloseableHttpClient getHttpClient() {

		if (httpClient == null) {

			PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(50);

			httpClient = HttpClients.custom().setConnectionManager(cm).build();
		}

		return httpClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(converterRestServiceUrl,
				"converterRestServiceUrl is not set!");
	}

	public void setConverterRestServiceUrl(String converterRestServiceUrl) {
		this.converterRestServiceUrl = converterRestServiceUrl;
	}

}
