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
package se.inera.intyg.intygstjanst.web.integration.test;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

public class Receiver implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);
    public static final String CERTIFICATE_ID = "certificate-id";
    private Map<String, String> storage = new HashMap<String, String>();

    @Override
    public void onMessage(Message rawMessage) {
        try {
            String doc = ((TextMessage) rawMessage).getText();
            String certificateId = rawMessage.getStringProperty(CERTIFICATE_ID);
            storage.put(certificateId, doc);
            LOG.info("Received intyg {}", certificateId);
        } catch (JMSException e) {
            Throwables.propagate(e);
            LOG.info("Receiver: Could not receive intyg via JMS.");
        }
    }

    public Map<String, String> getMessages() {
        return storage;
    }

}
