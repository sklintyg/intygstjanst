package se.inera.intyg.intygstjanst.web.integration.citizen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.service.ListCitizenCertificatesService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/citizen")
public class CitizenCertificateController {
    private static final Logger LOG = LoggerFactory.getLogger(CitizenCertificateController.class);
    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final ListCitizenCertificatesService listCitizenCertificatesService;

    public CitizenCertificateController(ListCitizenCertificatesService listCitizenCertificatesService) {
        this.listCitizenCertificatesService = listCitizenCertificatesService;
    }

    @PrometheusTimeMethod
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getActiveSickLeavesForCareUnit(@RequestBody ListCitizenCertificatesRequestDTO listCitizenCertificatesRequestDTO) {

        LOG.debug("Getting list of citizen certificates");
        listCitizenCertificatesService.get(
                listCitizenCertificatesRequestDTO.getPatientId(),
                listCitizenCertificatesRequestDTO.getCertificateTypes(),
                listCitizenCertificatesRequestDTO.getUnits(),
                listCitizenCertificatesRequestDTO.getStatuses(),
                listCitizenCertificatesRequestDTO.getYears()
        );

        return Response.ok().build();
    }
}