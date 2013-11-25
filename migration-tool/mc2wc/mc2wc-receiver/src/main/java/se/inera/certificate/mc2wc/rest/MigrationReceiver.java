package se.inera.certificate.mc2wc.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import se.inera.certificate.mc2wc.message.MigrationMessage;

@Path("/migration")
public interface MigrationReceiver {
    
    @POST
    @Path("/receive")
    @Consumes("application/xml")
    @Produces("application/xml")
    public Response receive(MigrationMessage message);
    
}
