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
package se.inera.intyg.intygstjanst.web.integration.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v2.SendMessageToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;

public class ArendeConverterTest {

    private static final long FIXED_TIME_NANO = 1456329300599000L;
    private static final Instant FIXED_TIME_INSTANT = Instant.ofEpochSecond(FIXED_TIME_NANO / 1_000_000, FIXED_TIME_NANO % 1_000_000);
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_TIME_INSTANT, ZoneId.systemDefault());

    private static final String SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML = "SendMessageToCareTest/sendmessagetocare.xml";

    private static URL getResource(String href) {
        return Thread.currentThread().getContextClassLoader().getResource(href);
    }

    @Before
    public void setup() {
        ArendeConverter.setMockSystemClock(FIXED_CLOCK);
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testConvertToXmlString() throws Exception {
        String xmlResult = ArendeConverter
            .convertToXmlString(getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML));
        String fileXml = loadXmlMessageFromFile();
        Diff diff = XMLUnit.compareXML(fileXml, xmlResult);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testConvertToSendMessageToCare() throws Exception {
        SendMessageToCareType sendMessageToCareType = getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        Arende sendMessageToCare = ArendeConverter.convertSendMessageToCare(sendMessageToCareType);
        assertEquals(sendMessageToCareType.getIntygsId().getExtension(), sendMessageToCare.getIntygsId());
        assertEquals(sendMessageToCareType.getLogiskAdressMottagare(), sendMessageToCare.getLogiskAdressmottagare());
        assertEquals(sendMessageToCareType.getMeddelandeId(), sendMessageToCare.getMeddelandeId());
        assertEquals(sendMessageToCareType.getReferensId(), sendMessageToCare.getReferens());
        assertEquals(FIXED_TIME_INSTANT,
            sendMessageToCare.getTimestamp().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));
        Diff diff = XMLUnit.compareXML(loadXmlMessageFromFile(), sendMessageToCare.getMeddelande());
        assertTrue(diff.toString(), diff.similar());
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
        assertEquals(FIXED_TIME_INSTANT, arende.getTimestamp().toInstant(ZoneId.systemDefault().getRules().getOffset(FIXED_TIME_INSTANT)));

        // arende.meddelande should be a string representation of original request
        try {
            JAXBContext jaxbContext = JAXBContext
                .newInstance(SendMessageToRecipientType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            @SuppressWarnings("unchecked")
            JAXBElement<SendMessageToRecipientType> jaxbMeddelande = (JAXBElement<SendMessageToRecipientType>) unmarshaller
                .unmarshal(new StringReader(arende.getMeddelande()));

            assertNotNull(jaxbMeddelande.getValue());
            assertEquals(intygsId, jaxbMeddelande.getValue().getIntygsId().getExtension());
            assertEquals(meddelande, jaxbMeddelande.getValue().getMeddelande());
        } catch (JAXBException e) {
            fail("should be valid message");
        }
    }

    private String loadXmlMessageFromFile() throws IOException {
        String fileXml = Resources.toString(getResource(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML), Charsets.UTF_8);
        return fileXml.replaceAll("[\\n\\t]", "");
    }

    private SendMessageToCareType getSendMessageToCareTypeFromFile(String fileName) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(SendMessageToCareType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
            new StreamSource(new ClassPathResource(fileName).getInputStream()),
            SendMessageToCareType.class).getValue();
    }

}
