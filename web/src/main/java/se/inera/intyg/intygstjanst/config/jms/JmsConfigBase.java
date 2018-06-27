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
package se.inera.intyg.intygstjanst.config.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.jms.core.JmsTemplate;
import se.inera.intyg.intygstjanst.web.integration.test.Receiver;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

/**
 * Creates connection factory and JMS templates for communicating with ActiveMQ. Note that this JmsConfig creates its
 * {@link ActiveMQConnectionFactory} directly rather than looking up a container-provided ConnectionFactory through
 * JNDI.
 *
 * Created by Magnus Ekstrand 2018-06-13
 */
public abstract class JmsConfigBase {

    @Value("${jms.broker.url}")
    private String brokerUrl;

    @Value("${jms.broker.username}")
    private String brokerUsername;

    @Value("${jms.broker.password}")
    private String brokerPassword;

    @Value("${jms.destination.queue.name}")
    private String destinationQueueName;


    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        return new CachingConnectionFactory(connectionFactory());
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(brokerPassword, brokerUsername, brokerUrl);
    }

    @Bean
    public JmsTransactionManager jmsTransactionManager() {
        return new JmsTransactionManager(connectionFactory());
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory());
        jmsTemplate.setDefaultDestination(destinationQueue());
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

    @Bean
    public Queue destinationQueue() {
        return new ActiveMQQueue(destinationQueueName);
    }

    @Bean
    public Receiver receiver() {
        return new Receiver();
    }

}
