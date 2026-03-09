/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.testability;

import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;
import se.inera.intyg.intygstjanst.testability.service.Receiver;

@RestController
@ApiBasePath("/resources")
@RequestMapping("/statisticsresource")
@Profile({"dev", "testability-api"})
public class StatisticsServiceResource {

  private static final Logger LOG = LoggerFactory.getLogger(StatisticsServiceResource.class);

  @Autowired private Receiver receiver;

  @GetMapping()
  public List<String> getAllMessages() {
    LOG.debug("Fetching all messages");
    return Lists.newArrayList(receiver.getMessages().values());
  }

  @GetMapping(path = "/{id}/{action}", produces = MediaType.APPLICATION_XML_VALUE)
  public String getMessage(@PathVariable("id") String id, @PathVariable("action") String action) {
    LOG.debug("Fetching {}-message for id {}", action, id);
    final String msg = receiver.getMessages().get(Receiver.generateKey(id, action));
    LOG.debug("Message: {}", msg);
    return msg;
  }

  @GetMapping("/purge")
  public String purge() {
    final int n = receiver.consume(msg -> {});
    return String.format("{ \"numPurged\": %d }", n);
  }
}
