package se.inera.intyg.intygstjanst.web.integration.stub;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendMessageToCareResponderStubRestApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageToCareResponderStubRestApi.class);

    @Autowired
    private SendMessageToCareStorage storage;

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_XML)
    public Response getPing() {
        String xmlResponse = buildXMLResponse(true, 0, null);
        LOGGER.debug("Pinged Intygstjänsten, got: " + xmlResponse);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_XML)
    public Response getCount() {
        LOGGER.debug("Got count: " + storage.getCount());
        Map<String, String> result = new HashMap<>();
        result.put("Message count: ", String.valueOf(storage.getCount()));
        String xmlResponse = buildXMLResponse(true, 0, result);
        return Response.ok(xmlResponse).build();
    }

    @POST
    @Path("/clear")
    @Produces(MediaType.APPLICATION_XML)
    public Response clearJson() {
        storage.clear();
        String xmlResponse = buildXMLResponse(true, 0, Collections.singletonMap("result", "ok"));
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getMessagesForCertificateId(@PathParam("id") String id) {
        List<String> xmlMessages = storage.getMessagesForCertificateId(id);
        Map<String, String> results = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (String message : xmlMessages) {
            stringBuilder.append(message);
        }
        results.put("message", stringBuilder.toString());
        String xmlResponse = buildXMLResponse(true, 0, results);
        LOGGER.debug("Found messages for id: " + xmlResponse);
        return Response.ok(xmlResponse).build();
    }

    @GET
    @Path("/messages-all")
    @Produces(MediaType.APPLICATION_XML)
    public Response getAllMessages() {
        Map<Pair<String, String>, String> xmlMessages = storage.getAllMessages();
        Map<String, String> results = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (String message : xmlMessages.values()) {
            stringBuilder.append(message);
        }
        results.put("messages", stringBuilder.toString());
        String xmlResponse = buildXMLResponse(true, 0, results);
        LOGGER.debug("Found all messages: " + xmlResponse);
        return Response.ok(xmlResponse).build();
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
