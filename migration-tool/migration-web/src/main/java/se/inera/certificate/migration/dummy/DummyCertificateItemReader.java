package se.inera.certificate.migration.dummy;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import se.inera.certificate.migration.model.OriginalCertificate;

public class DummyCertificateItemReader implements ItemReader<OriginalCertificate> {

    private static Logger log = LoggerFactory.getLogger(DummyCertificateItemReader.class);
    
    private int nbrOfCerts = 1;
    
    private int counter = 0;
    
    public OriginalCertificate read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        
        if (counter >= nbrOfCerts) {
            log.info("Done generating!");
            return null;
        }
        
        OriginalCertificate orgCert = createOriginalCert(counter++);
                
        if (counter % 100 == 0) {
            log.info("Counter @ " + counter);
        }
        
        return orgCert;
    }
    
    private OriginalCertificate createOriginalCert(int count) throws IOException {

        OriginalCertificate orgCert = new OriginalCertificate();
        
        Resource fileRes = new ClassPathResource("data/maximalt-fk7263.xml");
        orgCert.setOriginalCertificate(FileUtils.readFileToByteArray(fileRes.getFile()));
        orgCert.setOriginalCertificateId(Integer.valueOf(count));
        
        return orgCert;
    }

    public int getNbrOfCerts() {
        return nbrOfCerts;
    }

    public void setNbrOfCerts(int nbrOfCerts) {
        this.nbrOfCerts = nbrOfCerts;
    }
    
}
