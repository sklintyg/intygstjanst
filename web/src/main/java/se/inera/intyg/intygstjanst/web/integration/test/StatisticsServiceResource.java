/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.test;

import com.google.common.collect.Lists;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/statisticsresource")
public class StatisticsServiceResource {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsServiceResource.class);

    @Autowired
    private Receiver receiver;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAllMessages() {
        LOG.debug("Fetching all messages");
        return Lists.newArrayList(receiver.getMessages().values());
    }

    @GET
    @Path("/{id}/{action}")
    @Produces(MediaType.APPLICATION_XML)
    public String getMessage(@PathParam("id") String id, @PathParam("action") String action) {
        LOG.debug("Fetching {}-message for id {}", action, id);
        final String msg = receiver.getMessages().get(Receiver.generateKey(id, action));
        LOG.debug("Message: {}", msg);
        return msg;
    }

    @GET
    @Path("/purge")
    @Produces(MediaType.APPLICATION_JSON)
    public String purge() {
        final int n = receiver.consume(msg -> {
        });
        return String.format("{ \"numPurged\": %d }", n);
    }

}
