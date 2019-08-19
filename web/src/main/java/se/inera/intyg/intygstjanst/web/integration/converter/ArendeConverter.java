/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import java.time.LocalDateTime;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.ObjectFactory;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;

public final class ArendeConverter {

    private static Clock systemClock = Clock.systemDefaultZone();

    private ArendeConverter() {
    }

    @VisibleForTesting
    static void setMockSystemClock(Clock systemClock) {
        ArendeConverter.systemClock = systemClock;
    }

    public static Arende convertSendMessageToCare(SendMessageToCareType sendMessageToCareType) throws JAXBException {
        Arende arende = new Arende();
        arende.setIntygsId(sendMessageToCareType.getIntygsId().getExtension());
        arende.setMeddelandeId(sendMessageToCareType.getMeddelandeId());
        arende.setReferens(sendMessageToCareType.getReferensId());
        arende.setTimeStamp(LocalDateTime.now(systemClock));
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
        arende.setTimeStamp(LocalDateTime.now(systemClock));
        arende.setLogiskAdressmottagare(source.getLogiskAdressMottagare());
        arende.setAmne(source.getAmne().getCode());
        arende.setMeddelande(convertToXmlString(source));
        return arende;
    }

    public static String convertToXmlString(SendMessageToCareType sendMessageToCareType) {
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<SendMessageToCareType> jaxbElement = objectFactory.createSendMessageToCare(sendMessageToCareType);
        return XmlMarshallerHelper.marshal(jaxbElement).replaceAll("[\\n\\t]", "");
    }

    public static String convertToXmlString(SendMessageToRecipientType source) {
        se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.ObjectFactory objectFactory =
                new se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.ObjectFactory();
        JAXBElement<SendMessageToRecipientType> jaxbElement = objectFactory.createSendMessageToRecipient(source);
        return XmlMarshallerHelper.marshal(jaxbElement).replaceAll("[\\n\\t]", "");
    }
}
