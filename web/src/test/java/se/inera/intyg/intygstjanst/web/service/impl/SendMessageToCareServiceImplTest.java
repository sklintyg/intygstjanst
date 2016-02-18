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
package se.inera.intyg.intygstjanst.web.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCare;
import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCareRepository;
import se.inera.intyg.intygstjanst.web.integration.converter.SendMessageToCareConverter;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;

@RunWith(MockitoJUnitRunner.class)
public class SendMessageToCareServiceImplTest {
    private static final String SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML = "SendMessageToCareTest/sendmessagetocare.xml";

    @InjectMocks
    private SendMessageToCareServiceImpl service = new SendMessageToCareServiceImpl();

    @Mock
    private SendMessageToCareRepository repository = mock(SendMessageToCareRepository.class);

    @Test
    public void testProcessIncomingSendMessageToCare() throws Exception {
        SendMessageToCare message = loadFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        service.processIncomingSendMessageToCare(message);
        verify(repository, times(1)).save(any(SendMessageToCare.class));
    }

    private SendMessageToCare loadFromFile(String fileName) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(SendMessageToCareType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        SendMessageToCareType sendMessageToCareType = unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource(fileName).getInputStream()),
                SendMessageToCareType.class).getValue();
        return new SendMessageToCareConverter().convertSendMessageToCare(sendMessageToCareType);
    }

}
