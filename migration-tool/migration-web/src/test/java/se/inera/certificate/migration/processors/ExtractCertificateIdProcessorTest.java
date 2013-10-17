package se.inera.certificate.migration.processors;

import static org.junit.Assert.assertNotNull;

import java.io.File;
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
    public void testExtractId() throws Exception {
      
        OriginalCertificate orgCert = createOriginalCert();
        assertNotNull(orgCert);
                
        OriginalCertificate newOrgCert = processor.process(orgCert);
        
        assertNotNull(newOrgCert.getCertificateId());
    }

    private OriginalCertificate createOriginalCert() throws IOException {

        OriginalCertificate orgCert = new OriginalCertificate();
        
        Resource fileRes = new ClassPathResource("/data/legacy-maximalt-fk7263-transport.xml");
        orgCert.setOriginalCertificate(FileUtils.readFileToByteArray(fileRes.getFile()));
        
        return orgCert;
    }
    
}
