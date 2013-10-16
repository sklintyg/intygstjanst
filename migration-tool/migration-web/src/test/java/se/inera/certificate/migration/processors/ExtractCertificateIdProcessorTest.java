package se.inera.certificate.migration.processors;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import se.inera.certificate.migration.model.OriginalCertificate;

public class ExtractCertificateIdProcessorTest {

    private ExtractCertificateIdProcessor processor;
    
    @Before
    public void initProcessor() throws Exception {
        this.processor = new ExtractCertificateIdProcessor();
        this.processor.init();
    }
    
    @Test
    public void testExtractIdWithValidXML() throws Exception {
      
        OriginalCertificate orgCert = createOriginalCert();
        assertNotNull(orgCert);
                
        OriginalCertificate newOrgCert = processor.process(orgCert);
        
        assertNotNull(newOrgCert.getCertificateId());
    }
    
    @Test(expected = CertificateProcessingException.class)
    public void testExtractIdWithEmptyInput() throws Exception {
      
        OriginalCertificate orgCert = new OriginalCertificate();
        orgCert.setOriginalCertificateId(123);
        orgCert.setCertificateId("abc123");
                        
        processor.process(orgCert);
    }
    
    @Test(expected = CertificateProcessingException.class)
    public void testExtractIdWithInvalidXMLInput() throws Exception {
      
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<utlatande xmlns=\"urn:intyg:common-model:1\" ");
        sb.append("xmlns:hr=\"urn:riv:insuranceprocess:healthreporting:2\">");
        sb.append("<utlatande-id wrongExtension=\"80832895-5a9c-450a-bd74-08af43750788\"/>");
        sb.append("<typAvUtlatande code=\"fk7263\"/>");
        sb.append("</utlatande>");
                
        OriginalCertificate orgCert = new OriginalCertificate();
        orgCert.setCertificateId("abc123");
        orgCert.setOriginalCertificateId(123);
        orgCert.setOriginalCertificate(sb.toString().getBytes("UTF-8"));
                        
        processor.process(orgCert);
    }
    
    @Test(expected = CertificateProcessingException.class)
    public void testExtractIdWithCompletelyDifferentXMLInput() throws Exception {
      
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<animal xmlns=\"urn:something\">");
        sb.append("<name>Rusty</name>");
        sb.append("<type>dog</type>");
        sb.append("</animal>");
                
        OriginalCertificate orgCert = new OriginalCertificate();
        orgCert.setCertificateId("abc123");
        orgCert.setOriginalCertificateId(123);
        orgCert.setOriginalCertificate(sb.toString().getBytes("UTF-8"));
                        
        processor.process(orgCert);
    }
    
    private OriginalCertificate createOriginalCert() throws IOException {

        OriginalCertificate orgCert = new OriginalCertificate();
        orgCert.setOriginalCertificateId(123);
        orgCert.setCertificateId("abc123");
        
        Resource fileRes = new ClassPathResource("data/maximalt-fk7263.xml");
        orgCert.setOriginalCertificate(FileUtils.readFileToByteArray(fileRes.getFile()));
        
        return orgCert;
    }
    
}
