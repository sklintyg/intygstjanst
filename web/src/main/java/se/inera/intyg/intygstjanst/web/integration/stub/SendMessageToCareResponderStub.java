/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.intygstjanst.web.integration.converter.SendMessageToCareConverter;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultType;

@Transactional
@SchemaValidation
@Path("/send-message-to-care")
public class SendMessageToCareResponderStub implements SendMessageToCareResponderInterface {
    private Logger logger = LoggerFactory.getLogger(SendMessageToCareResponderStub.class);

    @Autowired
    private SendMessageToCareConverter converter;

    @Autowired
    private SendMessageToCareStorage storage;

    @Override
    public SendMessageToCareResponseType sendMessageToCare(String logicalAddress, SendMessageToCareType parameters) {
        SendMessageToCareResponseType response = new SendMessageToCareResponseType();
        ResultType resultType = new ResultType();
        try {
            storeMessage(parameters);
            logger.info("STUB Received question concerning certificate with id: " + parameters.getIntygsId().getExtension());
            resultType.setResultCode(ResultCodeType.OK);
        } catch (JAXBException e) {
            resultType.setResultCode(ResultCodeType.ERROR);
            response.setResult(resultType);
            return response;
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
        response.setResult(resultType);
        return response;
    }

    private String marshalCertificate(SendMessageToCareType parameters) throws JAXBException {
        return converter.convertToXmlString(parameters);
    }

    public void storeMessage(SendMessageToCareType sendMessageToCareType) throws JAXBException {
        String certificateId = sendMessageToCareType.getIntygsId().getExtension();
        String messageId = sendMessageToCareType.getMeddelandeId();
        String xmlBlob = marshalCertificate(sendMessageToCareType);
        storage.addMessage(certificateId, messageId, xmlBlob);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getMessagesForCertificateId(@PathParam("id") String id) {
        return storage.getMessagesForCertificateId(id);
    }

    @GET
    @Path("/messages-all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAllMessages() {
        return storage.getAllMessages();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    public int getCount() {
        return storage.getCount();
    }

    @POST
    @Path("/clear")
    public void clear() {
        storage.clear();
    }

}
