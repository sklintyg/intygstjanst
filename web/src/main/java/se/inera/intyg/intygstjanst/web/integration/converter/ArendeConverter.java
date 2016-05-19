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

import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.SendMessageToRecipientType;

public final class ArendeConverter {

    private ArendeConverter() {
    }

    public static String convertToXmlString(SendMessageToCareType sendMessageToCareType) throws JAXBException {
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBContext jaxbContext = JAXBContext
                .newInstance(SendMessageToCareType.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(objectFactory.createSendMessageToCare(sendMessageToCareType), stringWriter);
        return stringWriter.toString().replaceAll("[\\n\\t ]", "");
    }

    public static Arende convertSendMessageToCare(SendMessageToCareType sendMessageToCareType) throws JAXBException {
        Arende arende = new Arende();
        arende.setIntygsId(sendMessageToCareType.getIntygsId().getExtension());
        arende.setMeddelandeId(sendMessageToCareType.getMeddelandeId());
        if (sendMessageToCareType.getReferensId() != null) {
            arende.setReferens(sendMessageToCareType.getReferensId());
        }
        arende.setTimeStamp(LocalDateTime.now());
        arende.setLogiskAdressmottagare(sendMessageToCareType.getLogiskAdressMottagare());
        arende.setAmne(sendMessageToCareType.getAmne().getCode());
        arende.setMeddelande(convertToXmlString(sendMessageToCareType));
        return arende;
    }

    public static Arende convertSendMessageToRecipient(SendMessageToRecipientType source) throws JAXBException {
        Arende arende = new Arende();
        arende.setIntygsId(source.getIntygsId().getExtension());
        arende.setMeddelandeId(source.getMeddelandeId());
        arende.setReferens(source.getReferensId());
        arende.setTimeStamp(LocalDateTime.now());
        arende.setLogiskAdressmottagare(source.getLogiskAdressMottagare());
        arende.setAmne(source.getAmne().getCode());
        arende.setMeddelande(convertToXmlString(source));
        return arende;
    }

    public static String convertToXmlString(SendMessageToRecipientType source) throws JAXBException {
        se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.ObjectFactory objectFactory = new se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.ObjectFactory();
        JAXBContext jaxbContext = JAXBContext
                .newInstance(SendMessageToRecipientType.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(objectFactory.createSendMessageToRecipient(source), stringWriter);
        return stringWriter.toString().replaceAll("[\\n\\t]", "");
    }
}
