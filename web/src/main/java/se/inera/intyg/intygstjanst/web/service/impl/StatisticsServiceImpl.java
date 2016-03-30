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

package se.inera.intyg.intygstjanst.web.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;

@Component
public class StatisticsServiceImpl implements StatisticsService {

    private static final String CREATED = "created";
    private static final String REVOKED = "revoked";
    private static final String CERTIFICATE_ID = "certificate-id";
    private static final String ACTION = "action";

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Autowired(required = false)
    private JmsTemplate jmsTemplate;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Value("${statistics.enabled}")
    private boolean enabled;

    @Override
    public boolean created(String certificateXml, String certificateId, String certificateType, String careUnitId) {
        boolean rc = true;
        if (enabled) {
            rc = doSend(CREATED, certificateXml, certificateId, certificateType, careUnitId);
            if (rc) {
                monitoringLogService.logStatisticsSent(certificateId, certificateType, careUnitId);
            }
        }
        return rc;
    }

    @Override
    public boolean revoked(Certificate certificate) {
        boolean rc = true;
        if (enabled) {
            /** TODO INTYG-2042: uncomment below when statistics service has been updated. **/
            // rc = doSend(REVOKED, certificate.getDocument(), certificate.getId(), certificate.getType(),
            // certificate.getCareUnitName());
            rc = doSend(REVOKED, certificate);
            if (rc) {
                monitoringLogService.logStatisticsRevoked(certificate.getId(), certificate.getType(), certificate.getCareUnitId());
            }
        }
        return rc;
    }

    private boolean doSend(String type, String certificateXml, String certificateId, String certificateType, String careUnitId) {
        try {
            if (jmsTemplate == null) {
                LOG.error("Failure sending certificate '{}' type '{}' to statistics, no JmsTemplate configured", certificateId, type);
                return false;
            }

            MessageCreator messageCreator = new MC(type, certificateXml, certificateId);
            jmsTemplate.send(messageCreator);
            return true;
        } catch (JmsException e) {
            LOG.error("Failure sending certificate '{}' type '{}'to statistics", certificateId, type, e);
            return false;
        }
    }

    private static final class MC implements MessageCreator {
        private final String certificate;
        private final String type;
        private final String certificateId;

        MC(String type, String certificateXml, String certificateId) {
            this.type = type;
            this.certificate = certificateXml;
            this.certificateId = certificateId;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            TextMessage message = session.createTextMessage(certificate);
            message.setStringProperty(ACTION, type);
            message.setStringProperty(CERTIFICATE_ID, certificateId);
            return message;
        }
    }

    /**
     * TODO INTYG-2042 temporary solution below for 4.1 release, remove and use solution above when statistics service
     * has been updated.
     **/

    @Override
    public boolean created(Certificate certificate) {
        boolean rc = true;
        if (enabled) {
            rc = doSend(CREATED, certificate);
            if (rc) {
                monitoringLogService.logStatisticsSent(certificate.getId(), certificate.getType(), certificate.getCareUnitId());
            }
        }
        return rc;
    }

    private boolean doSend(String type, Certificate certificate) {
        try {
            if (jmsTemplate == null) {
                LOG.error("Failure sending certificate '{}' type '{}' to statistics, no JmsTemplate configured", certificate.getId(), type);
                return false;
            }

            MessageCreator messageCreator = new VeryTemporaryMC(type, certificate);
            jmsTemplate.send(messageCreator);
            return true;
        } catch (JmsException e) {
            LOG.error("Failure sending certificate '{}' type '{}'to statistics", certificate.getId(), type, e);
            return false;
        }
    }

    private static final class VeryTemporaryMC implements MessageCreator {
        private final Certificate certificate;
        private final String type;

        VeryTemporaryMC(String type, Certificate certificate) {
            this.type = type;
            this.certificate = certificate;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            TextMessage message = session.createTextMessage(certificate.getDocument());
            message.setStringProperty(ACTION, type);
            message.setStringProperty(CERTIFICATE_ID, certificate.getId());
            return message;
        }
    }

}
