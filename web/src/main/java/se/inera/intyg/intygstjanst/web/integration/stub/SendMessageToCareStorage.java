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
package se.inera.intyg.intygstjanst.web.integration.stub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
public class SendMessageToCareStorage {
    private Map<Pair<String, String>, String> messages = new ConcurrentHashMap<Pair<String, String>, String>();

    public int getCount() {
        return messages.size();
    }

    public void addMessage(String certificateId, String messageId, String xmlBlob) {
        Pair<String, String> pair = Pair.of(certificateId, messageId);
        messages.put(pair, xmlBlob);
    }

    public void clear() {
        messages.clear();
    }

    public List<String> getAllMessages() {
        return new ArrayList<String>(messages.values());
    }

    public List<String> getMessagesForCertificateId(String certificateId) {
        List<String> messagesList = new ArrayList<>();
        for (Pair<String, String> pair : messages.keySet()) {
            if (pair.getLeft().equals(certificateId)) {
                messagesList.add(messages.get(pair));
            }
        }
        return messagesList;
    }

}
