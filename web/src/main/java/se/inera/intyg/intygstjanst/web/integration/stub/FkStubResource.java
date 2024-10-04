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
package se.inera.intyg.intygstjanst.web.integration.stub;

import static se.inera.intyg.common.support.stub.MedicalCertificatesStore.MAKULERAD;
import static se.inera.intyg.common.support.stub.MedicalCertificatesStore.MEDDELANDE;
import static se.inera.intyg.common.support.stub.MedicalCertificatesStore.PERSONNUMMER;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.stub.MedicalCertificatesStore;

@Path("/fk")
@Transactional
public class FkStubResource {

    private static final String[] KEYS = {PERSONNUMMER, MAKULERAD, MEDDELANDE};

    @Autowired
    private MedicalCertificatesStore fkMedicalCertificatesStore;

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    public int count() {
        return fkMedicalCertificatesStore.getCount();
    }

    @GET
    @Path("/certificates")
    @Produces(MediaType.TEXT_HTML)
    public String certificates() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head>");
        sb.append("<link href='//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-combined.min.css' rel='stylesheet'>");
        sb.append("</head><body><div class='container'>");
        sb.append("<form method='POST' action='clear'><input type='submit' value='Clear'></form>");
        sb.append("<table class='table table-striped'>");
        sb.append("<thead><tr>");
        sb.append("<td>Id</td>");
        for (String key : KEYS) {
            sb.append("<td>").append(key).append("</td>");
        }
        sb.append("</tr></thead>");
        for (Entry<String, Map<String, String>> e : fkMedicalCertificatesStore.getAll().entrySet()) {
            sb.append("<tr>");
            sb.append("<td>").append(e.getKey()).append("</td>");
            for (String key : KEYS) {
                sb.append("<td>").append(e.getValue().get(key)).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        sb.append("</div></body>");
        return sb.toString();
    }

    @GET
    @Path("/certificates")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Map<String, String>> certificatesJson() {
        return new HashMap<>(fkMedicalCertificatesStore.getAll());
    }

    @POST
    @Path("/clear")
    @Produces(MediaType.TEXT_HTML)
    public String clear() {
        fkMedicalCertificatesStore.clear();
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>");
        sb.append("<meta http-equiv='refresh' content='0;url=certificates'>");
        sb.append("</head></html>");
        return sb.toString();
    }

    @POST
    @Path("/clear")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> clearJson() {
        fkMedicalCertificatesStore.clear();
        Collections.singletonMap("result", "ok");
        return Collections.singletonMap("result", "ok");
    }
}
