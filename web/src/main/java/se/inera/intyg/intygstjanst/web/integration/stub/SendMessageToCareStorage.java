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
package se.inera.intyg.intygstjanst.web.integration.stub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SendMessageToCareStorage {

    private Map<MessageKey, String> messages = new ConcurrentHashMap<>();

    public int getCount() {
        return messages.size();
    }

    public void addMessage(String certificateId, String messageId, String logicalAddress, String xmlBlob) {
        messages.put(new MessageKey(certificateId, messageId, logicalAddress), xmlBlob);
    }

    public void clear() {
        messages.clear();
    }

    public Map<MessageKey, String> getAllMessages() {
        return messages;
    }

    public Set<MessageKey> getMessagesIdsForLogicalAddress(String logicalAddress) {
        return messages.keySet()
            .stream().filter(k -> k.logicalAddress
                .equals(logicalAddress))
            .collect(Collectors.toSet());
    }

    public List<String> getMessagesForCertificateId(String certificateId) {
        List<String> messagesList = new ArrayList<>();
        for (MessageKey key : messages.keySet()) {
            if (key.certificateId.equals(certificateId)) {
                messagesList.add(messages.get(key));
            }
        }
        return messagesList;
    }

    public static final class MessageKey {

        public final String certificateId;
        public final String messageId;
        public final String logicalAddress;

        public MessageKey(String certificateId, String messageId, String logicalAddress) {
            this.certificateId = certificateId;
            this.messageId = messageId;
            this.logicalAddress = logicalAddress;
        }
    }

}
