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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCare;
import se.inera.intyg.intygstjanst.web.integration.converter.SendMessageToCareConverter;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class SendMessageToCareConverterTest {

    private static final String SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML = "SendMessageToCareTest/sendmessagetocare.xml";
    private SendMessageToCareConverter converter = new SendMessageToCareConverter();

    @Test
    public void testConvertToXmlString() throws Exception{
        String xmlResult = converter.convertToXmlString(getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML));
        String fileXml = loadXmlMessageFromFile();
        assertEquals(fileXml, xmlResult);
    }

    @Test
    public void testConvertToSendMessageToCare() throws Exception{
        SendMessageToCareType sendMessageToCareType = getSendMessageToCareTypeFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        SendMessageToCare sendMessageToCare = converter.convertSendMessageToCare(sendMessageToCareType);
        assertEquals(sendMessageToCareType.getIntygsId().getExtension(), sendMessageToCare.getIntygsId());
        assertEquals(sendMessageToCareType.getLogiskAdressMottagare(), sendMessageToCare.getLogiskAdressmottagare());
        assertEquals(sendMessageToCareType.getMeddelandeId(), sendMessageToCare.getMeddelandeId());
        assertEquals(sendMessageToCareType.getReferensId(), sendMessageToCare.getReferens());
        assertTrue(new LocalDateTime().toLocalDate().equals(sendMessageToCare.getTimestamp().toLocalDate()));
        assertEquals(loadXmlMessageFromFile(), sendMessageToCare.getMeddelande());
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
