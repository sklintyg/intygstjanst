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
package se.inera.intyg.intygstjanst.web.integration.test;

import com.google.common.base.Joiner;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsUtils;

/**
 * Class for consuming JMS messages sent to statistik (ST). Meant to be used for integration testing purposes.
 */
public class Receiver {

    private static final String CERTIFICATE_ID = "certificate-id";
    private static final String MESSAGE_ID = "message-id";
    private static final String ACTION = "action";
    private static final String FK_MESSAGE_ACTION = "message-sent";
    private static final long TIMEOUT = 3000;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue destinationQueue;

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    public Map<String, String> getMessages() {
        final Map<String, String> map = new HashMap<>();
        this.consume(TIMEOUT, msg -> {
            try {
                final String action = msg.getStringProperty(ACTION);
                final String id = msg.getStringProperty(FK_MESSAGE_ACTION.equals(action) ? MESSAGE_ID : CERTIFICATE_ID);
                final String key = generateKey(id, action);
                map.put(key, ((TextMessage) msg).getText());
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });
        return map;
    }

    /**
     * Returns number of consumed messages, wait for max timeout.
     *
     * @param timeout wait for max timeout in millis, 0 is forever and less than 0 is no wait at all.
     * @param consumer the consumer.
     * @return number of consumed messages.
     */
    public int consume(final long timeout, final Consumer<Message> consumer) {
        return jmsTemplate.execute(session -> {
            final MessageConsumer messageConsumer = session.createConsumer(destinationQueue);
            try {
                Message msg;
                int n = 0;
                while ((msg = next(timeout, messageConsumer)) != null) {
                    consumer.accept(msg);
                    n++;
                }
                if (session.getTransacted()) {
                    JmsUtils.commitIfNecessary(session);
                }
                LOG.info("Received {} messages", n);
                return n;
            } finally {
                JmsUtils.closeMessageConsumer(messageConsumer);
            }
        }, true);
    }

    /**
     * Returns number of consumed messages (no wait).
     *
     * @param consumer the consumer.
     * @return number of consumed messages.
     */
    public int consume(final Consumer<Message> consumer) {
        return consume(-1L, consumer);
    }

    //
    Message next(final long timeout, final MessageConsumer consumer) throws JMSException {
        return (timeout < 0L) ? consumer.receiveNoWait() : consumer.receive(timeout);
    }

    //
    static String generateKey(String certificateId, String action) {
        return Joiner.on("-").join(certificateId, action);
    }

}
