package se.inera.certificate.migration.task;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import se.inera.certificate.migration.processors.CertificateProcessingException;
import se.inera.certificate.migration.processors.FatalCertificateProcessingException;

public class PingConverterServiceTasklet implements Tasklet, InitializingBean {
    
    private static final String UTF8 = "UTF-8";

    private static final String TEST_FILE = "ping-test-certificate.xml";

    private static Logger logger = LoggerFactory.getLogger(PingConverterServiceTasklet.class);
    
    private String converterRestServiceUrl;

    public PingConverterServiceTasklet() {
    }
    
    public PingConverterServiceTasklet(String converterRestServiceUrl) {
        this.converterRestServiceUrl = converterRestServiceUrl;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        String testPayload = readTestPayloadFromFile(TEST_FILE);

        pingCoverterService(testPayload);

        return RepeatStatus.FINISHED;
    }

    private int pingCoverterService(String testPayload) throws IOException, FatalCertificateProcessingException {
        
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        HttpPost post = new HttpPost(converterRestServiceUrl);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "application/xml");

        StringEntity xmlPayload = new StringEntity(testPayload, UTF8);
        xmlPayload.setContentType("application/xml");
        xmlPayload.setContentEncoding(UTF8);

        post.setEntity(xmlPayload);
                
        int statusCode = 0;
        
        try {
            
            logger.info("Pinging converter service using URL '{}'", converterRestServiceUrl);
            
            HttpResponse response = httpClient.execute(post);
            statusCode = response.getStatusLine().getStatusCode(); 

            logger.info("Ping of converter service returned HTTP status {}", statusCode);
            
            if (statusCode != HttpStatus.SC_OK) {
                String errMsg = MessageFormat.format("Got HTTP error code {0} when pinging converter service on {1}", 
                        new Object[]{response.getStatusLine().getStatusCode(), converterRestServiceUrl});
                throw new FatalCertificateProcessingException(errMsg);
            }
            
        } catch (Exception e) {
            throw new FatalCertificateProcessingException("Pinging of converter service failed!", e);
        } finally {
            httpClient.close();
        }
        
        return statusCode;
    }

    private String readTestPayloadFromFile(String testFilePath) throws IOException {
        logger.debug("Reading ping test certificate from file '{}'", testFilePath);
        Resource testFileRes = new ClassPathResource(testFilePath);
        return FileUtils.readFileToString(testFileRes.getFile(), UTF8);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(converterRestServiceUrl, "converterRestServiceUrl is not set");
    }

    public void setConverterRestServiceUrl(String converterRestServiceUrl) {
        this.converterRestServiceUrl = converterRestServiceUrl;
    }

}
