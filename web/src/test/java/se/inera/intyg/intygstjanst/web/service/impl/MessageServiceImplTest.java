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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;

@RunWith(MockitoJUnitRunner.class)
public class MessageServiceImplTest {
    @Mock
    private ArendeRepository messageRepository;

    @InjectMocks
    private MessageServiceImpl messageServiceImpl;

    @Test
    public void testFindMessagesForCertificateIdFoundOne() {
        final var certificateId = "certificateId";
        final var messageId = "messageId";
        final var messageContent = "messageContent";
        final var subject = "subject";
        final var logicalAddress = "logicalAddress";
        final var timestamp = LocalDateTime.now();

        final var messages = new ArrayList<Arende>();
        final var message = new Arende();
        message.setIntygsId(certificateId);
        message.setMeddelandeId(messageId);
        message.setMeddelande(messageContent);
        message.setAmne(subject);
        message.setLogiskAdressmottagare(logicalAddress);
        message.setTimeStamp(timestamp);
        messages.add(message);

        doReturn(messages).when(messageRepository).findByIntygsId(certificateId);

        final var actualMessages = messageServiceImpl.findMessagesByCertificateId(certificateId);

        assertNotNull(actualMessages);
        assertEquals(messages.size(), actualMessages.size());
        assertEquals(certificateId, actualMessages.get(0).getCertificateId());
        assertEquals(messageId, actualMessages.get(0).getMessageId());
        assertEquals(messageContent, actualMessages.get(0).getMessageContent());
        assertEquals(subject, actualMessages.get(0).getSubject());
        assertEquals(logicalAddress, actualMessages.get(0).getLogicalAddress());
        assertEquals(timestamp, actualMessages.get(0).getTimestamp());
    }

    @Test
    public void testFindMessagesForCertificateIdFoundNone() {
        final var certificateId = "certificateId";
        final var messages = new ArrayList<Arende>();

        doReturn(messages).when(messageRepository).findByIntygsId(certificateId);

        final var actualMessages = messageServiceImpl.findMessagesByCertificateId(certificateId);

        assertNotNull(actualMessages);
        assertEquals(messages.size(), actualMessages.size());
    }

    @Test
    public void testFindMessagesForCertificateIdFoundMany() {
        final var certificateId = "certificateId";
        final var messageId = "messageId";
        final var messageContent = "messageContent";
        final var subject = "subject";
        final var logicalAddress = "logicalAddress";
        final var timestamp = LocalDateTime.now();

        final var messages = new ArrayList<Arende>();
        for (var i = 0; i < 5; i++) {
            final var message = new Arende();
            message.setIntygsId(certificateId);
            message.setMeddelandeId(messageId);
            message.setMeddelande(messageContent);
            message.setAmne(subject);
            message.setLogiskAdressmottagare(logicalAddress);
            message.setTimeStamp(timestamp);
            messages.add(message);
        }

        doReturn(messages).when(messageRepository).findByIntygsId(certificateId);

        final var actualMessages = messageServiceImpl.findMessagesByCertificateId(certificateId);

        assertNotNull(actualMessages);
        assertEquals(messages.size(), actualMessages.size());
    }
}
