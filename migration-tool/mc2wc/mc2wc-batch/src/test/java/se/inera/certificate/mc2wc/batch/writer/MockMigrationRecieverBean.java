package se.inera.certificate.mc2wc.batch.writer;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import se.inera.certificate.mc2wc.message.MigrationMessage;
import se.inera.certificate.mc2wc.message.MigrationReply;
import se.inera.certificate.mc2wc.message.MigrationResultType;
import se.inera.certificate.mc2wc.rest.MigrationReceiver;

public class MockMigrationRecieverBean implements MigrationReceiver {

    public static final String HTTP_500 = "badCertificateHttp500";
    public static final String HTTP_400 = "badCertificateHttp400";
    
    public MockMigrationRecieverBean() {
        
    }

    public Response receive(MigrationMessage message) {
        
        Response response = null;
        
        String certificateId = message.getCertificate().getCertificateId();
        
        if (HTTP_500.equals(certificateId)) {
            throw new ServerErrorException(Status.INTERNAL_SERVER_ERROR);
        } else if (HTTP_400.equals(certificateId)) {
            MigrationReply reply = new MigrationReply();
            reply.setResult(MigrationResultType.ERROR);
            reply.setMessage("Something was bad in the request");
            response = Response.status(Status.BAD_REQUEST).entity(reply).build();
            throw new BadRequestException(response);
        } else {            
            MigrationReply reply = new MigrationReply();
            reply.setResult(MigrationResultType.OK);
            response = Response.ok(reply).build(); 
        }
        
        return response;
    }

}
