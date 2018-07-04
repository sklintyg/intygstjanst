/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import com.google.common.base.Joiner;
import java.util.HashMap;
import java.util.Map;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsUtils;

/**
 * Class for consuming JMS messages sent to statistik (ST). Meant to be used for integration testing purposes.
 */
public class Receiver {

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    private static final String CERTIFICATE_ID = "certificate-id";
    private static final String MESSAGE_ID = "message-id";
    private static final String ACTION = "action";
    private static final String FK_MESSAGE_ACTION = "message-sent";

    private static final long TIMEOUT = 3000;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue destinationQueue;


    public Map<String, String> getMessages() {
        return jmsTemplate.execute(session -> {
            final Map<String, String> map = new HashMap<>();
            final MessageConsumer consumer = session.createConsumer(destinationQueue);
            try {
                Message msg;
                while ((msg = consumer.receive(TIMEOUT)) != null) {
                    final String action = msg.getStringProperty(ACTION);
                    final String id = msg.getStringProperty(FK_MESSAGE_ACTION.equals(action) ? MESSAGE_ID : CERTIFICATE_ID);
                    final String key = generateKey(id, action);
                    map.put(key, ((TextMessage) msg).getText());
                }
                if (session.getTransacted()) {
                    JmsUtils.commitIfNecessary(session);
                }
                LOG.info("Received {} messages", map.size());
                return map;
            } finally {
                JmsUtils.closeMessageConsumer(consumer);
            }
        }, true);
    }

    public static String generateKey(String certificateId, String action) {
        return Joiner.on("-").join(certificateId, action);
    }

}
