package se.inera.certificate.migration.dummy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import se.inera.certificate.migration.model.OriginalCertificate;

public class DummyCertificateItemWriter implements ItemWriter<OriginalCertificate> {
    
    private static Logger log = LoggerFactory.getLogger(DummyCertificateItemWriter.class);
    
    public void write(List<? extends OriginalCertificate> certificateList) throws Exception {
        
        log.info("Writing " + certificateList.size() + " nbr of certificates");
        
    }

}
