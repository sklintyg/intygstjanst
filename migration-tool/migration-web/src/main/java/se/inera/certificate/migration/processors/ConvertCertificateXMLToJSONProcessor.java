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
        ItemProcessor<OriginalCertificate, Certificate> {

    private static final String UTF8 = "UTF-8";

    private static Logger LOG = LoggerFactory.getLogger(ConvertCertificateXMLToJSONProcessor.class);

    private static CloseableHttpClient httpClient;

    private String converterRestServiceUrl;

    public Certificate process(OriginalCertificate orgCert) throws Exception {

        if (orgCert == null) {
            LOG.warn("Supplied OriginalCertificate was null, returning null");
            return null;
        }
        
        LOG.debug("Converting OriginalCertificate to JSON: {}", orgCert);

        try {
            String convertedJson = convertOriginalCertificate(orgCert);

            if (convertedJson == null) {
                LOG.error("Conversion failed for OriginalCertificate {}, this certificate will not be updated", orgCert);
                return null;
            }
            
            return new Certificate(orgCert, convertedJson);
            
        } catch (IOException e) {
            String errMsg = MessageFormat.format("A fatal error occured when processing OriginalCertificate: {0}",
                    orgCert);
            LOG.error(errMsg, e);
            throw new FatalCertificateProcessingException(errMsg, e);
        }
    }

    public String convertOriginalCertificate(OriginalCertificate orgCert) throws IOException,
            AbstractCertificateProcessingException {

        HttpClient client = getHttpClient();

        HttpPost post = new HttpPost(converterRestServiceUrl);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "application/xml");

        StringEntity xmlPayload = new StringEntity(orgCert.getOrignalCertificateAsString(), UTF8);
        xmlPayload.setContentType("application/xml");
        xmlPayload.setContentEncoding(UTF8);

        post.setEntity(xmlPayload);

        HttpResponse response = client.execute(post);

        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode != HttpStatus.SC_OK) {

            if (statusCode == HttpStatus.SC_BAD_REQUEST) {
                
                LOG.error("HTTP 400: Bad Request when converting OriginalCertificate {}, ", orgCert);
                LOG.error("Error from conversion service: {}", EntityUtils.toString(response.getEntity(), UTF8));
                LOG.error(orgCert.getOrignalCertificateAsString());
                
                return null;
                
            } else {
                String errMsg = MessageFormat.format(
                        "Got HTTP error code {0} when processing OriginalCertificate: {1}", new Object[] {
                                statusCode, orgCert });
                throw new FatalCertificateProcessingException(errMsg);
            }
        }

        return EntityUtils.toString(response.getEntity(), UTF8);
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
        Assert.notNull(converterRestServiceUrl, "converterRestServiceUrl is not set!");
    }

    public void setConverterRestServiceUrl(String converterRestServiceUrl) {
        this.converterRestServiceUrl = converterRestServiceUrl;
    }

}
