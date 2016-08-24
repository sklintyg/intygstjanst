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
package se.inera.intyg.intygstjanst.web.service.converter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.web.integration.converter.ArendeConverter;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;

public class ArendeConverterTest {

    private static final String SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML = "SendMessageToCareTest/sendmessagetocare.xml";

    @Test
    public void testConvertToXmlString() throws Exception{
        String xmlResult = ArendeConverter.convertToXmlString(getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML));
        String fileXml = loadXmlMessageFromFile();
        assertEquals(fileXml, xmlResult);
    }

    @Test
    public void testConvertToSendMessageToCare() throws Exception{
        SendMessageToCareType sendMessageToCareType = getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        Arende sendMessageToCare = ArendeConverter.convertSendMessageToCare(sendMessageToCareType);
        assertEquals(sendMessageToCareType.getIntygsId().getExtension(), sendMessageToCare.getIntygsId());
        assertEquals(sendMessageToCareType.getLogiskAdressMottagare(), sendMessageToCare.getLogiskAdressmottagare());
        assertEquals(sendMessageToCareType.getMeddelandeId(), sendMessageToCare.getMeddelandeId());
        assertEquals(sendMessageToCareType.getReferensId(), sendMessageToCare.getReferens());
        assertNotNull(sendMessageToCare.getTimestamp());
        assertEquals(loadXmlMessageFromFile(), sendMessageToCare.getMeddelande());
    }

    @Test
    public void convertSendMessageToRecipientTest() throws JAXBException {
        final String intygsId = "intygsid";
        final String amne = "KOMPLT";
        final String logiskAdressMottagare = "mottagare";
        final String meddelande = "meddelande";
        final String meddelandeId = "meddelandeId";
        final String referensId = "referensid";
        SendMessageToRecipientType message = new SendMessageToRecipientType();
        message.setIntygsId(new IntygId());
        message.getIntygsId().setExtension(intygsId);
        message.setAmne(new Amneskod());
        message.getAmne().setCode(amne);
        message.setLogiskAdressMottagare(logiskAdressMottagare);
        message.setMeddelande(meddelande);
        message.setMeddelandeId(meddelandeId);
        message.setReferensId(referensId);

        Arende arende = ArendeConverter.convertSendMessageToRecipient(message);
        assertEquals(intygsId, arende.getIntygsId());
        assertEquals(amne, arende.getAmne());
        assertEquals(logiskAdressMottagare, arende.getLogiskAdressmottagare());
        assertEquals(meddelandeId, arende.getMeddelandeId());
        assertEquals(referensId, arende.getReferens());
        assertNotNull(arende.getTimestamp());

        // arende.meddelande should be a string representation of original request
        try {
            JAXBContext jaxbContext = JAXBContext
                    .newInstance(SendMessageToRecipientType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            @SuppressWarnings("unchecked")
            JAXBElement<SendMessageToRecipientType> jaxbMeddelande = (JAXBElement<SendMessageToRecipientType>) unmarshaller.unmarshal(new StringReader(arende.getMeddelande()));

            assertNotNull(jaxbMeddelande.getValue());
            assertEquals(intygsId, jaxbMeddelande.getValue().getIntygsId().getExtension());
            assertEquals(meddelande, jaxbMeddelande.getValue().getMeddelande());
        } catch (JAXBException e) {
            fail("should be valid message");
        }
    }

    private String loadXmlMessageFromFile() throws IOException {
        String fileXml = Resources.toString(getResource(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML), Charsets.UTF_8);
        return fileXml.replaceAll("[\\r\\n\\t ]", "");
    }

    private SendMessageToCareType getSendMessageToCareTypeFromFile(String fileName) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(SendMessageToCareType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource(fileName).getInputStream()),
                SendMessageToCareType.class).getValue();
    }

    private static URL getResource(String href) {
        return Thread.currentThread().getContextClassLoader().getResource(href);
    }

}
