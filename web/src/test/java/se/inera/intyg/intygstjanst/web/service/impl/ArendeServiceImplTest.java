/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;
import se.inera.intyg.intygstjanst.web.integration.converter.ArendeConverter;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;

@RunWith(MockitoJUnitRunner.class)
public class ArendeServiceImplTest {

    private static final String SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML = "SendMessageToCareTest/sendmessagetocare.xml";

    @InjectMocks
    private ArendeServiceImpl service;

    @Mock
    private ArendeRepository repository;

    @Test
    public void testProcessIncomingSendMessageToCare() throws Exception {
        Arende message = loadFromFile(SEND_MESSAGE_TO_CARE_TEST_SENDMESSAGETOCARE_XML);
        service.processIncomingMessage(message);
        verify(repository, times(1)).save(any(Arende.class));
    }

    private Arende loadFromFile(String fileName) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(SendMessageToCareType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        SendMessageToCareType sendMessageToCareType = unmarshaller.unmarshal(
            new StreamSource(new ClassPathResource(fileName).getInputStream()),
            SendMessageToCareType.class).getValue();
        return ArendeConverter.convertSendMessageToCare(sendMessageToCareType);
    }

}
