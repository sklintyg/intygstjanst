package se.inera.certificate.migration.processors;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import se.inera.certificate.migration.model.Certificate;
import se.inera.certificate.migration.model.OriginalCertificate;

public class ConvertCertificateXMLToJSONProcessor implements InitializingBean, ItemProcessor<OriginalCertificate, Certificate> {

    private static Logger LOG = LoggerFactory.getLogger(ConvertCertificateXMLToJSONProcessor.class);

    private CloseableHttpClient httpClient;

    private String converterRestServiceUrl;

    public Certificate process(OriginalCertificate orgCert) throws Exception {
        
        LOG.debug("Converting original certificate {} to JSON", orgCert.getOriginalCertificateId());
        
        String convertedJson = null;
                
        try {
            convertedJson = convertOriginalCertificate(orgCert);
            
        } catch (Exception e) {
            LOG.error("An error occured in the ConvertCertificateXMLToJSONProcessor", e);
            throw new CertificateProcessingException("An error occured in the ConvertCertificateXMLToJSONProcessor", e);
        }
        
        return new Certificate(orgCert.getCertificateId(), convertedJson);
    }

    public String convertOriginalCertificate(OriginalCertificate orgCert) throws IOException, CertificateProcessingException {
        
        String returnedContent = null;
        
        CloseableHttpClient client = getHttpClient();
        
        HttpPost post = new HttpPost(converterRestServiceUrl);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "application/xml");
        
        try {
            
            StringEntity xmlPayload = new StringEntity(orgCert.getOrignalCertificateAsString(), "UTF-8");
            xmlPayload.setContentType("application/xml");
            xmlPayload.setContentEncoding("utf-8");

            post.setEntity(xmlPayload);

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == 200) {
                returnedContent = EntityUtils.toString(response.getEntity(), "UTF-8");
            } else {
                throw new CertificateProcessingException("Got http error code: " + response.getStatusLine().getStatusCode());
            }

        } finally {
            client.close();
        }

        return returnedContent;
    }

    public CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClients.createDefault();
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
