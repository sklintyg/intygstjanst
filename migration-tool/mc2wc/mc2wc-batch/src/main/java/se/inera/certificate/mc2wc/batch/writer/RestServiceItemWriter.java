package se.inera.certificate.mc2wc.batch.writer;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.hibernate.engine.spi.EntityUniqueKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import se.inera.certificate.mc2wc.exception.AbstractCertificateMigrationException;
import se.inera.certificate.mc2wc.exception.CertificateMigrationException;
import se.inera.certificate.mc2wc.exception.FatalCertificateMigrationException;
import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.MigrationReply;
import se.inera.certificate.mc2wc.message.MigrationResultType;
import se.inera.certificate.mc2wc.rest.MigrationReceiver;

public class RestServiceItemWriter implements ItemWriter<MigrationMessage> {
    
    private static Logger log = LoggerFactory.getLogger(RestServiceItemWriter.class);
    
    @Autowired
    @Qualifier("migrationMessageReceiverService")
    private MigrationReceiver migrationReceiver; 
    
    public RestServiceItemWriter() {
        
    }

    @Override
    public void write(List<? extends MigrationMessage> messages) throws Exception {
        
        log.debug("Got list with {} MigrationMessages", messages.size());
                
        for (MigrationMessage migrationMessage : messages) {
            send(migrationMessage);
        }
        
    }
    
    private void send(MigrationMessage migrationMessage) throws AbstractCertificateMigrationException {
        
        log.debug("Sending MigrationMessage");
        
        Response response = migrationReceiver.receive(migrationMessage);
                
        StatusType statusInfo = response.getStatusInfo();
        
        Family responseFamily = statusInfo.getFamily();
        
        MigrationReply reply = response.readEntity(MigrationReply.class);
        
    }
    
}
