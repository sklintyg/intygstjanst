package se.inera.intyg.intygstjanst.web.integration.reko;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/reko")
public class RekoController {
    private static final Logger LOG = LoggerFactory.getLogger(RekoController.class);
    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final se.inera.intyg.intygstjanst.web.service.SetRekoStatusToSickLeave setRekoStatusToSickLeave;

    public RekoController(se.inera.intyg.intygstjanst.web.service.SetRekoStatusToSickLeave setRekoStatusToSickLeave) {
        this.setRekoStatusToSickLeave = setRekoStatusToSickLeave;
    }

    @PrometheusTimeMethod
    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setRekoStatusToSickLeave(@RequestBody SetRekoStatusToSickLeaveRequestDTO request) {
        setRekoStatusToSickLeave.set(
                request.patientId,
                request.getStatus(),
                request.getCareProviderId(),
                request.getCareUnitId(),
                request.getUnitId()
        );

        return Response.ok().build();
    }
}
