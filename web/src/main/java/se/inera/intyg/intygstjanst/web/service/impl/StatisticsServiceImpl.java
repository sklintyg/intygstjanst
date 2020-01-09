/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import static java.lang.invoke.MethodHandles.lookup;

import javax.jms.Queue;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final String ACTION = "action";
    private static final String CREATED = "created";
    private static final String REVOKED = "revoked";
    private static final String SENT = "sent";
    private static final String MESSAGE_SENT = "message-sent";

    private static final String MESSAGE_ID = "message-id";
    private static final String CERTIFICATE_ID = "certificate-id";
    private static final String CERTIFICATE_TYPE = "certificate-type";
    private static final String CERTIFICATE_RECIPIENT = "certificate-recipient";

    private static final Logger LOG = LoggerFactory.getLogger(lookup().getClass());

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private Queue destinationQueue;

    @Value("${statistics.enabled}")
    private boolean enabled;

    @Override
    public boolean created(String certificateXml, String certificateId, String certificateType, String careUnitId) {
        boolean rc = true;
        if (enabled) {
            rc = sendIntygDataPointToStatistik(CREATED, certificateXml, certificateId, certificateType);
            if (rc) {
                monitoringLogService.logStatisticsCreated(certificateId, certificateType, careUnitId);
            }
        }
        return rc;
    }

    @Override
    public boolean revoked(String certificateXml, String certificateId, String certificateType, String careUnitId) {
        boolean rc = true;
        if (enabled) {
            rc = sendIntygDataPointToStatistik(REVOKED, certificateXml, certificateId, certificateType);
            if (rc) {
                monitoringLogService.logStatisticsRevoked(certificateId, certificateType, careUnitId);
            }
        }
        return rc;
    }

    @Override
    public boolean sent(
        final String certificateId,
        final String certificateType,
        final String careUnitId,
        final String recipientId) {

        boolean rc = true;
        if (enabled) {
            rc = sendIntygDataPointToStatistik(SENT, null, certificateId, certificateType, recipientId);
        }
        if (rc) {
            monitoringLogService.logStatisticsSent(certificateId, certificateType, careUnitId, recipientId);
        }
        return rc;
    }

    @Override
    public boolean messageSent(String xml, String messageId, String topic) {
        boolean rc = true;
        if (enabled) {
            rc = sendFkMessageDataPointToStatistik(MESSAGE_SENT, xml, messageId);
        }
        if (rc) {
            monitoringLogService.logStatisticsMessageSent(messageId, topic);
        }
        return rc;
    }

    private boolean sendIntygDataPointToStatistik(
        final String actionType,
        final String certificateXml,
        final String certificateId,
        final String certificateType) {

        return sendIntygDataPointToStatistik(
            actionType,
            certificateXml,
            certificateId,
            certificateType,
            null);
    }

    private boolean sendIntygDataPointToStatistik(
        final String actionType,
        final String certificateXml,
        final String certificateId,
        final String certificateType,
        final String certificateRecipientId) {

        try {
            return send(session -> {
                TextMessage message = session.createTextMessage(certificateXml);
                message.setStringProperty(ACTION, actionType);
                message.setStringProperty(CERTIFICATE_ID, certificateId);
                message.setStringProperty(CERTIFICATE_TYPE, certificateType);
                if (certificateRecipientId != null) {
                    message.setStringProperty(CERTIFICATE_RECIPIENT, certificateRecipientId);
                }
                return message;
            });
        } catch (JmsException e) {
            LOG.error("Failure sending '{}' type with certificate id '{}'to statistics", actionType, certificateId, e);
            return false;
        }
    }

    private boolean sendFkMessageDataPointToStatistik(String actionType, String messageXml, String messageId) {
        try {
            return send(session -> {
                final TextMessage message = session.createTextMessage(messageXml);
                message.setStringProperty(ACTION, actionType);
                message.setStringProperty(MESSAGE_ID, messageId);
                return message;
            });
        } catch (JmsException e) {
            LOG.error("Failure sending '{}' type with message id '{}'to statistics", actionType, messageId, e);
            return false;
        }
    }

    //
    boolean send(final MessageCreator messageCreator) {
        jmsTemplate.send(destinationQueue, messageCreator);
        return true;
    }
}
