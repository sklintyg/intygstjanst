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
import java.util.Objects;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import se.inera.intyg.intygstjanst.web.integration.test.Receiver;

/**
 * Creates connection factory and JMS templates for communicating with ActiveMQ. Note that this JmsConfig creates its
 * {@link ActiveMQConnectionFactory} directly rather than looking up a container-provided ConnectionFactory through
 * JNDI.
 * Created by Magnus Ekstrand 2018-06-13
 */
@Configuration
@EnableJms
public class JmsConfig {

    @Value("${activemq.broker.url}")
    private String brokerUrl;

    @Value("${activemq.broker.username}")
    private String brokerUsername;

    @Value("${activemq.broker.password}")
    private String brokerPassword;

    @Value("${activemq.destination.queue.name}")
    private String destinationQueueName;

    @Value("${activemq.internal.notification.queue.name}")
    private String internalNotificationQueue;

    @Value("${certificate.event.queue.name}")
    private String certificateEventQueue;

    @Bean
    public JmsListenerContainerFactory jmsListenerContainerFactory(JmsTransactionManager jmsTransactionManager) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(Objects.requireNonNull(jmsTransactionManager.getConnectionFactory()));
        factory.setDestinationResolver(destinationResolver());
        factory.setSessionTransacted(true);
        factory.setTransactionManager(jmsTransactionManager);
        factory.setCacheLevelName("CACHE_CONSUMER");
        factory.setConcurrency("1-10");
        return factory;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new PooledConnectionFactory(new ActiveMQConnectionFactory(brokerUsername, brokerPassword, brokerUrl));
    }

    @Bean
    public JmsTransactionManager jmsTransactionManager() {
        return new JmsTransactionManager(connectionFactory());
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        return template(connectionFactory(), destinationQueue());
    }

    @Bean(value = "jmsCertificateEventTemplate")
    public JmsTemplate jmsCertificateEventTemplate(ConnectionFactory jmsConnectionFactory) {
        return template(jmsConnectionFactory, certificateEventQueue());
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

    @Bean
    public DestinationResolver destinationResolver() {
        return new DynamicDestinationResolver();
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
