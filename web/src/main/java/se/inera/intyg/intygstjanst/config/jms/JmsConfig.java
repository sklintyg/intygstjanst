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
package se.inera.intyg.intygstjanst.config.jms;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import se.inera.intyg.intygstjanst.web.integration.test.Receiver;

/**
 * JMS configuration — connection infrastructure and listener container factory are now provided
 * by Spring Boot ActiveMQ auto-configuration. This class retains only JmsTemplate beans,
 * Queue beans, and the Receiver test helper.
 */
@Configuration
@EnableJms
public class JmsConfig {

    @Value("${activemq.destination.queue.name}")
    private String destinationQueueName;

    @Value("${activemq.internal.notification.queue.name}")
    private String internalNotificationQueue;

    @Value("${certificate.event.queue.name}")
    private String certificateEventQueue;

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        return template(connectionFactory, destinationQueue());
    }

    @Bean(value = "jmsCertificateEventTemplate")
    public JmsTemplate jmsCertificateEventTemplate(ConnectionFactory connectionFactory) {
        return template(connectionFactory, certificateEventQueue());
    }

    @Bean(value = "destinationQueue")
    public Queue destinationQueue() {
        return new ActiveMQQueue(destinationQueueName);
    }

    @Bean(value = "internalNotificationQueue")
    public Queue internalNotificationQueue() {
        return new ActiveMQQueue(internalNotificationQueue);
    }

    @Bean("certificateEventQueue")
    public Queue certificateEventQueue() {
        return new ActiveMQQueue(certificateEventQueue);
    }

    // Only used by test API features
    @Bean
    public Receiver receiver() {
        return new Receiver();
    }

    JmsTemplate template(final ConnectionFactory connectionFactory, final Queue queue) {
        final JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setDefaultDestination(queue);
        jmsTemplate.setConnectionFactory(connectionFactory);
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }
}
