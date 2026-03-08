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

package se.inera.intyg.intygstjanst.testability.stub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.intygstjanst.infrastructure.security.interceptor.ApiBasePath;

@RestController
@ApiBasePath("/api")
@Profile("it-fk-stub")
@RequestMapping("/send-message-to-care")
public class SendMessageToCareResponderStubRestApi {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SendMessageToCareResponderStubRestApi.class);

  @Autowired private SendMessageToCareStorage storage;

  @GetMapping(value = "/ping", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> getPing() {
    String xmlResponse = buildXMLResponse(true, 0, null);
    LOGGER.debug("Pinged Intygstjänsten, got: " + xmlResponse);
    return ResponseEntity.ok(xmlResponse);
  }

  @GetMapping(value = "/count", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> getCount() {
    LOGGER.debug("Got count: " + storage.getCount());
    Map<String, String> result = new HashMap<>();
    result.put("Message count: ", String.valueOf(storage.getCount()));
    String xmlResponse = buildXMLResponse(true, 0, result);
    return ResponseEntity.ok(xmlResponse);
  }

  @PostMapping(value = "/clear", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> clearJson() {
    storage.clear();
    String xmlResponse = buildXMLResponse(true, 0, Collections.singletonMap("result", "ok"));
    return ResponseEntity.ok(xmlResponse);
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> getMessagesForCertificateId(@PathVariable("id") String id) {
    List<String> xmlMessages = storage.getMessagesForCertificateId(id);
    Map<String, String> results = new HashMap<>();
    StringBuilder stringBuilder = new StringBuilder();
    for (String message : xmlMessages) {
      stringBuilder.append(message);
    }
    results.put("message", stringBuilder.toString());
    String xmlResponse = buildXMLResponse(true, 0, results);
    LOGGER.debug("Found messages for id: " + xmlResponse);
    return ResponseEntity.ok(xmlResponse);
  }

  @GetMapping(value = "/byLogicalAddress", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getMessagesForLogicalAddress(
      @RequestParam("address") String address) {
    Map<String, Set<SendMessageToCareStorage.MessageKey>> messageIds =
        ImmutableMap.of("messages", storage.getMessagesIdsForLogicalAddress(address));
    try {
      return ResponseEntity.ok(new ObjectMapper().writeValueAsString(messageIds));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping(value = "/messages-all", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> getAllMessages() {
    Map<SendMessageToCareStorage.MessageKey, String> xmlMessages = storage.getAllMessages();
    Map<String, String> results = new HashMap<>();
    StringBuilder stringBuilder = new StringBuilder();
    for (String message : xmlMessages.values()) {
      stringBuilder.append(message);
    }
    results.put("messages", stringBuilder.toString());
    String xmlResponse = buildXMLResponse(true, 0, results);
    LOGGER.debug("Found all messages: " + xmlResponse);
    return ResponseEntity.ok(xmlResponse);
  }

  private String buildXMLResponse(boolean ok, long time, Map<String, String> additionalValues) {
    StringBuilder sb = new StringBuilder();
    sb.append("<pingdom_http_custom_check>");
    sb.append("<status>" + (ok ? "OK" : "FAIL") + "</status>");
    sb.append("<response_time>" + time + "</response_time>");
    if (additionalValues != null) {
      sb.append("<additional_data>");
      additionalValues.forEach((k, v) -> sb.append("<" + k + ">" + v + "</" + k + ">"));
      sb.append("</additional_data>");
    }
    sb.append("</pingdom_http_custom_check>");
    return sb.toString();
  }
}
