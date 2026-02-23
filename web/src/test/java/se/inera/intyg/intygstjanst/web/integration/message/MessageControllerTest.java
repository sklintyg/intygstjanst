/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.message.dto.MessageFromIT;
import se.inera.intyg.intygstjanst.web.service.MessageService;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    @Test
    void testFindMessagesByCertificateIdSuccessful() {
        final var certificateId = "certificateId";

        final var messagesFromITs = new ArrayList<MessageFromIT>();
        messagesFromITs.add(MessageFromIT.create(certificateId, "messageId1", "messageContent1", "subject1",
            "logicalAddress1", LocalDateTime.now()));
        messagesFromITs.add(MessageFromIT.create(certificateId, "messageId2", "messageContent2", "subject2",
            "logicalAddress2", LocalDateTime.now()));

        doReturn(messagesFromITs).when(messageService).findMessagesByCertificateId(certificateId);

        final Response actualResponse = messageController.findMessagesByCertificateId(certificateId);

        assertNotNull(actualResponse);
        assertEquals(200, actualResponse.getStatus());
        assertTrue(actualResponse.hasEntity());
    }

    @Test
    void testFindMessagesByCertificateIdFailedEmptyId() {
        final var certificateId = "";

        final Response actualResponse = messageController.findMessagesByCertificateId(certificateId);

        assertNotNull(actualResponse);
        assertEquals(400, actualResponse.getStatus());
    }

    @Test
    void testFindMessagesByCertificateIdFailedNullId() {
        final String certificateId = null;

        final Response actualResponse = messageController.findMessagesByCertificateId(certificateId);

        assertNotNull(actualResponse);
        assertEquals(400, actualResponse.getStatus());
    }
}
