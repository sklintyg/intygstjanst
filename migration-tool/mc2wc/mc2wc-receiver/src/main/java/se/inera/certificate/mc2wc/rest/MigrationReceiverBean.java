package se.inera.certificate.mc2wc.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.MigrationReply;
import se.inera.certificate.mc2wc.message.MigrationResultType;
import se.inera.certificate.mc2wc.service.MigrationService;
import se.inera.certificate.mc2wc.service.MigrationServiceException;

public class MigrationReceiverBean implements MigrationReceiver {
    
    @Autowired
    private MigrationService migrationService;
    
    public MigrationReceiverBean() {

    }

    public Response receive(MigrationMessage message) {
        
        MigrationReply reply = null;
        
        try {
            
            MigrationResultType result = migrationService.processMigrationMessage(message);
            
            reply = new MigrationReply();
            reply.setResult(result);
            
            return Response.ok().entity(reply).build();
            
        } catch (MigrationServiceException e) {
            
            reply = new MigrationReply();
            reply.setResult(MigrationResultType.ERROR);
            reply.setMessage(e.getMessage());
            
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        
    }

}
