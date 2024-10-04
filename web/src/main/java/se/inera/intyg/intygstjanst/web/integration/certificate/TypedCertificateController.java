/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.web.integration.certificate;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.certificate.dto.DiagnosedCertificate;
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate;
import se.inera.intyg.infra.certificate.dto.TypedCertificateRequest;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.intygstjanst.web.service.TypedCertificateService;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Internal REST endpoint to retrieve certificates
 */
@Controller
@Path("/typedcertificate")
public class TypedCertificateController {

    final TypedCertificateService typedCertificateService;

    @Autowired
    public TypedCertificateController(TypedCertificateService typedCertificateService) {
        this.typedCertificateService = typedCertificateService;
    }

    @PrometheusTimeMethod
    @POST
    @Path("/diagnosed/unit")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<DiagnosedCertificate> listDiagnosedCertificatesForCareUnit(@RequestBody TypedCertificateRequest parameters) {
        var units = parameters.getUnitIds();

        if (units == null || units.isEmpty()) {
            return Collections.emptyList();
        }

        return typedCertificateService.listDiagnosedCertificatesForCareUnits(units,
            parameters.getCertificateTypes(),
            parameters.getFromDate(),
            parameters.getToDate(),
            parameters.getDoctorIds());
    }

    @PrometheusTimeMethod
    @POST
    @Path("/doctors")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<String> listDoctorsForCareUnit(@RequestBody TypedCertificateRequest parameters) {
        var units = parameters.getUnitIds();

        if (units == null || units.isEmpty()) {
            return Collections.emptyList();
        }

        return typedCertificateService.listDoctorsForCareUnits(units,
            parameters.getCertificateTypes(),
            parameters.getFromDate(),
            parameters.getToDate());
    }

    @PrometheusTimeMethod
    @POST
    @Path("/diagnosed/person")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<DiagnosedCertificate> listDiagnosedCertificatesForCitizen(@RequestBody TypedCertificateRequest parameters) {
        var optionalPersonnummer = Personnummer.createPersonnummer(parameters.getPersonId());

        if (optionalPersonnummer.isEmpty()) {
            return Collections.emptyList();
        }

        return typedCertificateService.listDiagnosedCertificatesForPerson(optionalPersonnummer.get(),
            parameters.getCertificateTypes(),
            parameters.getFromDate(),
            parameters.getToDate(),
            parameters.getUnitIds());
    }

    @PrometheusTimeMethod
    @POST
    @Path("/sickleave/person")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<SickLeaveCertificate> listSickLeaveCertificatesForCitizen(@RequestBody TypedCertificateRequest parameters) {
        var optionalPersonnummer = Personnummer.createPersonnummer(parameters.getPersonId());

        if (optionalPersonnummer.isEmpty()) {
            return Collections.emptyList();
        }

        return typedCertificateService.listSickLeaveCertificatesForPerson(optionalPersonnummer.get(),
            parameters.getCertificateTypes(),
            parameters.getFromDate(),
            parameters.getToDate(),
            parameters.getUnitIds());
    }


}
