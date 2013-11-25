package se.inera.certificate.mc2wc.batch.processors;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.mc2wc.converter.MigrationMessageConverter;
import se.inera.certificate.mc2wc.jpa.model.Certificate;
import se.inera.certificate.mc2wc.message.MigrationMessage;

public class CertificateConverterProcessor implements ItemProcessor<Certificate, MigrationMessage> {

    @Autowired
    MigrationMessageConverter converter;
    
    public CertificateConverterProcessor() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public MigrationMessage process(Certificate cert) throws Exception {
        
        MigrationMessage message = converter.toMigrationMessage(cert, true);
        
        return message;
    }

}
