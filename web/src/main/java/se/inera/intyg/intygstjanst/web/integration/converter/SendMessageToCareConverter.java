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
package se.inera.intyg.intygstjanst.web.integration.converter;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Component;

import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCare;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;

@Component
public class SendMessageToCareConverter {

    public String convertToXmlString(SendMessageToCareType sendMessageToCareType) throws JAXBException {
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBContext jaxbContext = JAXBContext
                .newInstance(SendMessageToCareType.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(objectFactory.createSendMessageToCare(sendMessageToCareType), stringWriter);
        return stringWriter.toString().replaceAll("[\\n\\t ]", "");
    }

    public SendMessageToCare convertSendMessageToCare(SendMessageToCareType sendMessageToCareType) throws JAXBException {
        SendMessageToCare sendMessageToCare = new SendMessageToCare();
        sendMessageToCare.setIntygsId(sendMessageToCareType.getIntygsId().getExtension());
        sendMessageToCare.setMeddelandeId(sendMessageToCareType.getMeddelandeId());
        if (sendMessageToCareType.getReferensId() != null) {
            sendMessageToCare.setReferens(sendMessageToCareType.getReferensId());
        }
        sendMessageToCare.setTimeStamp(LocalDateTime.now());
        sendMessageToCare.setLogiskAdressmottagare(sendMessageToCareType.getLogiskAdressMottagare().getExtension());
        sendMessageToCare.setMeddelande(convertToXmlString(sendMessageToCareType));
        sendMessageToCare.setAmne(sendMessageToCare.getAmne());
        return sendMessageToCare;
    }

}
